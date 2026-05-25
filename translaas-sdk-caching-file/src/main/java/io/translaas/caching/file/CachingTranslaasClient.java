package io.translaas.caching.file;

import io.translaas.client.OfflineCacheOptions;
import io.translaas.client.OfflineFallbackMode;
import io.translaas.client.TranslaasClient;
import io.translaas.client.TranslaasRequestContext;
import io.translaas.client.TranslaasTranslationClient;
import io.translaas.caching.file.offline.IOfflineCacheProvider;
import io.translaas.i18n.TranslationEntries;
import io.translaas.models.GroupTranslationsResponse;
import io.translaas.models.OfflineCacheDownloadResult;
import io.translaas.models.ProjectGroupPayload;
import io.translaas.models.ProjectLocalesResponse;
import io.translaas.models.ProjectTranslationsResponse;
import io.translaas.models.ReportMissingKeysRequest;
import io.translaas.models.ValidateApiKeyResponse;
import io.translaas.models.exception.TranslaasApiException;
import io.translaas.models.exception.TranslaasOfflineCacheMissException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpTimeoutException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/** Decorator that adds offline cache fallback modes around a {@link TranslaasClient}. */
public final class CachingTranslaasClient implements TranslaasTranslationClient {

  private final TranslaasTranslationClient inner;
  private final IOfflineCacheProvider cache;
  private final OfflineCacheOptions options;
  private final String projectId;

  public CachingTranslaasClient(
      TranslaasTranslationClient inner,
      IOfflineCacheProvider cache,
      OfflineCacheOptions options,
      String projectId) {
    this.inner = inner;
    this.cache = cache;
    this.options = options;
    this.projectId = projectId;
  }

  @Override
  public CompletableFuture<String> getEntry(String group, String entry, String lang) {
    return getEntry(group, entry, lang, null, null, null, null);
  }

  @Override
  public CompletableFuture<String> getEntry(
      String group, String entry, String lang, TranslaasRequestContext context) {
    return getEntry(group, entry, lang, null, null, context, null);
  }

  @Override
  public CompletableFuture<String> getEntry(
      String group,
      String entry,
      String lang,
      BigDecimal n,
      Map<String, String> parameters,
      TranslaasRequestContext context,
      Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(
        () -> getEntryBlocking(group, entry, lang, n, parameters, context), exec);
  }

  @Override
  public CompletableFuture<ProjectLocalesResponse> getProjectLocales(String project) {
    return getProjectLocales(project, null, null);
  }

  @Override
  public CompletableFuture<ProjectLocalesResponse> getProjectLocales(
      String project, TranslaasRequestContext context, Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(() -> getLocalesBlocking(project, context), exec);
  }

  @Override
  public CompletableFuture<GroupTranslationsResponse> getGroupTranslations(
      String project, String group, String lang) {
    return getGroupTranslations(project, group, lang, null, null, null);
  }

  @Override
  public CompletableFuture<GroupTranslationsResponse> getGroupTranslations(
      String project,
      String group,
      String lang,
      String format,
      TranslaasRequestContext context,
      Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(
        () -> getGroupBlocking(project, group, lang, format, context), exec);
  }

  @Override
  public CompletableFuture<ProjectTranslationsResponse> getProjectTranslations(
      String project, String lang) {
    return getProjectTranslations(project, lang, null, null, null);
  }

  @Override
  public CompletableFuture<ProjectTranslationsResponse> getProjectTranslations(
      String project,
      String lang,
      String format,
      TranslaasRequestContext context,
      Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(
        () -> getProjectBlocking(project, lang, format, context), exec);
  }

  @Override
  public CompletableFuture<Void> reportMissingKeys(ReportMissingKeysRequest request) {
    return inner.reportMissingKeys(request);
  }

  @Override
  public CompletableFuture<OfflineCacheDownloadResult> getOfflineCache(String project) {
    return inner.getOfflineCache(project);
  }

  @Override
  public CompletableFuture<OfflineCacheDownloadResult> getOfflineCache(
      String project, TranslaasRequestContext context, Executor executor) {
    return inner.getOfflineCache(project, context, executor);
  }

  @Override
  public CompletableFuture<ValidateApiKeyResponse> validateApiKey() {
    return inner.validateApiKey();
  }

  private String getEntryBlocking(
      String group,
      String entry,
      String lang,
      BigDecimal n,
      Map<String, String> parameters,
      TranslaasRequestContext context) {
    OfflineFallbackMode mode = options.getFallbackMode();
    if (mode == OfflineFallbackMode.CACHE_FIRST) {
      return getEntryCacheFirst(group, entry, lang, n, parameters, context);
    }
    if (mode == OfflineFallbackMode.API_FIRST) {
      return getEntryApiFirst(group, entry, lang, n, parameters, context);
    }
    return getEntryCacheOnly(group, entry, lang, n, parameters);
  }

  private String getEntryCacheFirst(
      String group,
      String entry,
      String lang,
      BigDecimal n,
      Map<String, String> parameters,
      TranslaasRequestContext context) {
    String resolved = resolveFromCache(group, entry, lang, n, parameters);
    if (resolved != null) {
      return resolved;
    }
    try {
      String result = inner.getEntry(group, entry, lang, n, parameters, context, null).join();
      updateGroupCacheAsync(group, lang);
      return result;
    } catch (RuntimeException ex) {
      if (isNetworkOrApiError(ex)) {
        throw TranslaasOfflineCacheMissException.forEntry(projectId, lang, group, entry);
      }
      throw ex;
    }
  }

  private String getEntryApiFirst(
      String group,
      String entry,
      String lang,
      BigDecimal n,
      Map<String, String> parameters,
      TranslaasRequestContext context) {
    try {
      String result = inner.getEntry(group, entry, lang, n, parameters, context, null).join();
      updateGroupCacheAsync(group, lang);
      return result;
    } catch (RuntimeException ex) {
      if (!isNetworkOrApiError(ex)) {
        throw ex;
      }
      String resolved = resolveFromCache(group, entry, lang, n, parameters);
      if (resolved != null) {
        return resolved;
      }
      throw TranslaasOfflineCacheMissException.forEntry(projectId, lang, group, entry);
    }
  }

  private String getEntryCacheOnly(
      String group, String entry, String lang, BigDecimal n, Map<String, String> parameters) {
    String resolved = resolveFromCache(group, entry, lang, n, parameters);
    if (resolved != null) {
      return resolved;
    }
    throw TranslaasOfflineCacheMissException.forEntry(projectId, lang, group, entry);
  }

  private String resolveFromCache(
      String group,
      String entry,
      String lang,
      BigDecimal n,
      Map<String, String> parameters) {
    return cache
        .getGroup(projectId, group, lang)
        .map(g -> TranslationEntries.resolveEntryText(g.getEntries(), entry, lang, n, parameters))
        .orElse(null);
  }

  private GroupTranslationsResponse getGroupBlocking(
      String project, String group, String lang, String format, TranslaasRequestContext context) {
    OfflineFallbackMode mode = options.getFallbackMode();
    if (mode == OfflineFallbackMode.CACHE_FIRST) {
      return getGroupCacheFirst(project, group, lang, format, context);
    }
    if (mode == OfflineFallbackMode.API_FIRST) {
      return getGroupApiFirst(project, group, lang, format, context);
    }
    return getGroupCacheOnly(project, group, lang);
  }

  private GroupTranslationsResponse getGroupCacheFirst(
      String project, String group, String lang, String format, TranslaasRequestContext context) {
    return cache
        .getGroup(project, group, lang)
        .orElseGet(
            () -> {
              try {
                GroupTranslationsResponse result =
                    inner.getGroupTranslations(project, group, lang, format, context, null).join();
                updateGroupCacheAsync(group, lang);
                return result;
              } catch (RuntimeException ex) {
                if (isNetworkOrApiError(ex)) {
                  throw TranslaasOfflineCacheMissException.forGroup(project, lang, group);
                }
                throw ex;
              }
            });
  }

  private GroupTranslationsResponse getGroupApiFirst(
      String project, String group, String lang, String format, TranslaasRequestContext context) {
    try {
      GroupTranslationsResponse result =
          inner.getGroupTranslations(project, group, lang, format, context, null).join();
      updateGroupCacheAsync(group, lang);
      return result;
    } catch (RuntimeException ex) {
      if (!isNetworkOrApiError(ex)) {
        throw ex;
      }
      return cache
          .getGroup(project, group, lang)
          .orElseThrow(() -> TranslaasOfflineCacheMissException.forGroup(project, lang, group));
    }
  }

  private GroupTranslationsResponse getGroupCacheOnly(String project, String group, String lang) {
    return cache
        .getGroup(project, group, lang)
        .orElseThrow(() -> TranslaasOfflineCacheMissException.forGroup(project, lang, group));
  }

  private ProjectTranslationsResponse getProjectBlocking(
      String project, String lang, String format, TranslaasRequestContext context) {
    OfflineFallbackMode mode = options.getFallbackMode();
    if (mode == OfflineFallbackMode.CACHE_FIRST) {
      return getProjectCacheFirst(project, lang, format, context);
    }
    if (mode == OfflineFallbackMode.API_FIRST) {
      return getProjectApiFirst(project, lang, format, context);
    }
    return getProjectCacheOnly(project, lang);
  }

  private ProjectTranslationsResponse getProjectCacheFirst(
      String project, String lang, String format, TranslaasRequestContext context) {
    return cache
        .getProject(project, lang)
        .orElseGet(
            () -> {
              try {
                ProjectTranslationsResponse result =
                    inner.getProjectTranslations(project, lang, format, context, null).join();
                cache.saveProject(project, lang, result);
                return result;
              } catch (RuntimeException ex) {
                if (isNetworkOrApiError(ex)) {
                  throw TranslaasOfflineCacheMissException.forProject(project, lang);
                }
                throw ex;
              }
            });
  }

  private ProjectTranslationsResponse getProjectApiFirst(
      String project, String lang, String format, TranslaasRequestContext context) {
    try {
      ProjectTranslationsResponse result =
          inner.getProjectTranslations(project, lang, format, context, null).join();
      cache.saveProject(project, lang, result);
      return result;
    } catch (RuntimeException ex) {
      if (!isNetworkOrApiError(ex)) {
        throw ex;
      }
      return cache
          .getProject(project, lang)
          .orElseThrow(() -> TranslaasOfflineCacheMissException.forProject(project, lang));
    }
  }

  private ProjectTranslationsResponse getProjectCacheOnly(String project, String lang) {
    return cache
        .getProject(project, lang)
        .orElseThrow(() -> TranslaasOfflineCacheMissException.forProject(project, lang));
  }

  private ProjectLocalesResponse getLocalesBlocking(String project, TranslaasRequestContext context) {
    OfflineFallbackMode mode = options.getFallbackMode();
    if (mode == OfflineFallbackMode.CACHE_FIRST) {
      return getLocalesCacheFirst(project, context);
    }
    if (mode == OfflineFallbackMode.API_FIRST) {
      return getLocalesApiFirst(project, context);
    }
    return getLocalesCacheOnly(project);
  }

  private ProjectLocalesResponse getLocalesCacheFirst(
      String project, TranslaasRequestContext context) {
    return cache
        .getProjectLocales(project)
        .orElseGet(
            () -> {
              try {
                ProjectLocalesResponse result =
                    inner.getProjectLocales(project, context, null).join();
                cache.saveProjectLocales(project, result);
                return result;
              } catch (RuntimeException ex) {
                if (isNetworkOrApiError(ex)) {
                  throw TranslaasOfflineCacheMissException.forProject(project, "*");
                }
                throw ex;
              }
            });
  }

  private ProjectLocalesResponse getLocalesApiFirst(
      String project, TranslaasRequestContext context) {
    try {
      ProjectLocalesResponse result = inner.getProjectLocales(project, context, null).join();
      cache.saveProjectLocales(project, result);
      return result;
    } catch (RuntimeException ex) {
      if (!isNetworkOrApiError(ex)) {
        throw ex;
      }
      return cache
          .getProjectLocales(project)
          .orElseThrow(() -> TranslaasOfflineCacheMissException.forProject(project, "*"));
    }
  }

  private ProjectLocalesResponse getLocalesCacheOnly(String project) {
    return cache
        .getProjectLocales(project)
        .orElseThrow(() -> TranslaasOfflineCacheMissException.forProject(project, "*"));
  }

  private void updateGroupCacheAsync(String group, String lang) {
    ForkJoinPool.commonPool()
        .execute(
            () -> {
              try {
                GroupTranslationsResponse groupData =
                    inner.getGroupTranslations(projectId, group, lang).join();
                ProjectTranslationsResponse existing =
                    cache.getProject(projectId, lang).orElse(null);
                Map<String, ProjectGroupPayload> groups = new java.util.LinkedHashMap<>();
                if (existing != null && existing.getGroups() != null) {
                  groups.putAll(existing.getGroups());
                }
                groups.put(
                    group,
                    new ProjectGroupPayload(
                        groupData.getEntries(),
                        groupData.getEntryContext(),
                        groupData.getGroupEntryContext()));
                cache.saveProject(
                    projectId,
                    lang,
                    new ProjectTranslationsResponse(
                        projectId,
                        lang,
                        groupData.getVersion(),
                        groupData.getGeneratedAt(),
                        groups,
                        null,
                        null,
                        null));
              } catch (RuntimeException ignored) {
                // best effort background update
              }
            });
  }

  private static boolean isNetworkOrApiError(Throwable ex) {
    Throwable t = ex;
    while (t != null) {
      if (t instanceof TranslaasApiException
          || t instanceof HttpTimeoutException
          || t instanceof IOException) {
        return true;
      }
      t = t.getCause();
    }
    return false;
  }
}
