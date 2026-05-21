package io.mantelabs.translaas.client;

import io.mantelabs.translaas.caching.TranslaasCacheEntry;
import io.mantelabs.translaas.client.http.TranslaasHttp;
import io.mantelabs.translaas.client.http.TranslaasUris;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.mantelabs.translaas.models.GroupTranslationsResponse;
import io.mantelabs.translaas.models.OfflineCacheDownloadResult;
import io.mantelabs.translaas.models.ProjectLocalesResponse;
import io.mantelabs.translaas.models.ProjectTranslationsResponse;
import io.mantelabs.translaas.models.TranslationResponses;
import io.mantelabs.translaas.models.ReportMissingKeysRequest;
import io.mantelabs.translaas.models.ValidateApiKeyResponse;
import io.mantelabs.translaas.models.exception.TranslaasApiException;
import io.mantelabs.translaas.models.json.TranslaasJson;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Async HTTP client for the Translaas SDK API.
 *
 * <p>Single translation text: {@link #getEntry(String, String, String, BigDecimal, Map,
 * TranslaasRequestContext, Executor)} and overloads. On {@code 304 Not Modified}, the future
 * completes with an empty string and {@link TranslaasRequestContext#isNotModified()} is {@code
 * true} when a context instance was provided (parity with .NET).
 *
 * <p>Project locales JSON: {@link #getProjectLocales(String, TranslaasRequestContext, Executor)}.
 * On {@code 304 Not Modified}, the future completes with {@code null} and {@link
 * TranslaasRequestContext#isNotModified()} is {@code true} when a context instance was provided.
 *
 * <p>Group and project translation bundles: {@link #getGroupTranslations(String, String, String,
 * TranslaasRequestContext, Executor)} and {@link #getProjectTranslations(String, String, String,
 * TranslaasRequestContext, Executor)}. On {@code 304 Not Modified}, JSON bundle methods complete
 * with {@code null} and {@link TranslaasRequestContext#isNotModified()} is {@code true} when a
 * context instance was provided.
 *
 * <p>Validate API key (connectivity / bootstrap): {@link #validateApiKey()} — {@code GET}
 * {@value #API_KEYS_VALIDATE_PATH} ({@code application/json}).
 *
 * <p>Missing keys: {@link #reportMissingKeys(ReportMissingKeysRequest, TranslaasRequestContext,
 * Executor)} — {@code POST} JSON; expects {@code 202 Accepted}. Requires a project-scoped API key
 * (see {@link #reportMissingKeys(ReportMissingKeysRequest, TranslaasRequestContext, Executor)}).
 *
 * <p>Offline cache ZIP: {@link #getOfflineCache(String, TranslaasRequestContext, Executor)} —
 * {@code GET} {@value #paths.offlineCache()} ({@code application/zip}). On {@code 304
 * Not Modified}, completes with {@code null} and {@link TranslaasRequestContext#isNotModified()} is
 * {@code true} when a context instance was provided.
 *
 * <p><strong>Caching:</strong> when {@link TranslaasOptions#getCacheMode()} is not {@link
 * CacheMode#NONE}, responses are stored in an L1 {@link io.mantelabs.translaas.caching.TranslaasCacheProvider}
 * (custom or in-memory). See {@link TranslationResponseCache} for {@code ETag} / {@code 304}
 * behavior.
 */
public final class TranslaasClient implements TranslaasTranslationClient {

  private static final int BODY_SNIPPET_MAX = 2048;

  /** Default-path alias for {@code GET} single translation ({@code text/plain}). */
  public static final String TRANSLATIONS_TEXT_PATH =
      SdkTranslationPaths.DEFAULT_PREFIX + "/text";

  /** Default-path alias for {@code GET} project locales. */
  public static final String TRANSLATIONS_LOCALES_PATH =
      SdkTranslationPaths.DEFAULT_PREFIX + "/locales";

  /** Default-path alias for {@code GET} group translations. */
  public static final String TRANSLATIONS_GROUP_PATH =
      SdkTranslationPaths.DEFAULT_PREFIX + "/group";

  /** Default-path alias for {@code GET} project translations. */
  public static final String TRANSLATIONS_PROJECT_PATH =
      SdkTranslationPaths.DEFAULT_PREFIX + "/project";

  /** Default-path alias for {@code POST} report missing keys. */
  public static final String TRANSLATIONS_REPORT_MISSING_PATH =
      SdkTranslationPaths.DEFAULT_PREFIX + "/report-missing";

  /** Default-path alias for {@code GET} offline translation cache ZIP. */
  public static final String TRANSLATIONS_OFFLINE_CACHE_PATH =
      SdkTranslationPaths.DEFAULT_PREFIX + "/offline-cache";

  /** Path for {@code GET} validate API key as {@code application/json} (not under {@code /sdk}). */
  public static final String API_KEYS_VALIDATE_PATH = "/api/v1/api-keys/validate";

  /**
   * Value for the {@code format} query parameter to request composite-key flat bundles ({@code
   * group.entry} keys on project; plain entry keys within the group).
   */
  public static final String FORMAT_FLAT_JSON = "flat-json";

  private final TranslaasHttp http;
  private final SdkTranslationPaths paths;
  private final TranslationResponseCache responseCache;

  public TranslaasClient(TranslaasOptions options) {
    this(new TranslaasHttp(Objects.requireNonNull(options, "options")));
  }

  TranslaasClient(TranslaasHttp http) {
    this.http = Objects.requireNonNull(http, "http");
    this.paths = new SdkTranslationPaths(http.getOptions());
    this.responseCache = TranslationResponseCache.maybeCreate(http.getOptions());
  }

  public CompletableFuture<String> getEntry(String group, String entry, String lang) {
    return getEntry(group, entry, lang, null, null, null, null);
  }

  public CompletableFuture<String> getEntry(
      String group, String entry, String lang, TranslaasRequestContext context) {
    return getEntry(group, entry, lang, null, null, context, null);
  }

  public CompletableFuture<String> getEntry(
      String group,
      String entry,
      String lang,
      BigDecimal n,
      Map<String, String> parameters,
      TranslaasRequestContext context) {
    return getEntry(group, entry, lang, n, parameters, context, null);
  }

  /**
   * Fetches a single translation string as {@code text/plain; charset=utf-8}.
   *
   * @param n optional plural / decimal selector for pluralized entries
   * @param parameters optional interpolation parameters (query keys must not appear in {@link
   *     TranslationTextQueries#RESERVED_QUERY_KEYS})
   * @param context optional per-request overrides and response metadata; cleared via {@link
   *     TranslaasRequestContext#clearResponseMetadata()} before the request
   * @param executor optional executor for the async task; defaults to the common pool
   * @return response body text, or empty string when the server returns {@code 304} (see {@link
   *     TranslaasRequestContext#isNotModified()})
   */
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
        () ->
            getEntryBlocking(
                TranslationTextQueries.withExplicitEntry(group, lang, entry, n, parameters),
                context),
        exec);
  }

  /**
   * Same as {@link #getEntry(String, String, String, BigDecimal, Map, TranslaasRequestContext,
   * Executor)} but sends the entry key using <strong>shorthand</strong> (see {@link
   * TranslationTextQueries#withShorthandEntryKey}).
   */
  public CompletableFuture<String> getEntryUsingShorthand(
      String group,
      String entryKey,
      String lang,
      BigDecimal n,
      Map<String, String> interpolationParameters,
      TranslaasRequestContext context,
      Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(
        () ->
            getEntryBlocking(
                TranslationTextQueries.withShorthandEntryKey(
                    group, lang, entryKey, n, interpolationParameters),
                context),
        exec);
  }

  private String getEntryBlocking(Map<String, String> query, TranslaasRequestContext context)
      throws TranslaasApiException {
    if (context != null) {
      context.clearResponseMetadata();
    }
    LinkedHashMap<String, String> merged =
        TranslaasUris.mergeQueryParams(http.getOptions(), context, query);
    if (responseCache != null) {
      Optional<TranslaasCacheEntry> cached =
          responseCache.tryGet(paths.text(), merged, context);
      if (cached.isPresent()) {
        return TranslationResponseCache.utf8String(cached.get().getValue());
      }
    }
    HttpResponse<String> response = http.get(paths.text(), query, context);
    if (response.statusCode() == 204) {
      return TranslationCacheKeys.resolveEntryKey(merged);
    }
    if (response.statusCode() == 304) {
      if (responseCache != null) {
        Optional<TranslaasCacheEntry> fromCache =
            responseCache.getIgnoringValidationBypass(paths.text(), merged);
        if (fromCache.isPresent()) {
          copyEtagFromCacheToContext(context, fromCache.get());
          return TranslationResponseCache.utf8String(fromCache.get().getValue());
        }
      }
      return "";
    }
    String body = response.body();
    if (responseCache != null) {
      responseCache.putIfApplicable(
          paths.text(),
          merged,
          TranslationResponseCache.utf8Bytes(body != null ? body : ""),
          response.headers().firstValue("ETag"));
    }
    return body != null ? body : "";
  }

  private void copyEtagFromCacheToContext(
      TranslaasRequestContext context, TranslaasCacheEntry entry) {
    if (context == null || !http.getOptions().isUseConditionalRequests()) {
      return;
    }
    entry.getEtag().ifPresent(context::setResponseETag);
  }

  /**
   * Fetches supported locale codes for a project ({@code application/json}).
   *
   * <p>Query parameters: required {@code project}; optional {@code channel} and {@code v} from
   * {@link TranslaasOptions} defaults and {@link TranslaasRequestContext} overrides.
   *
   * @param project required project key
   * @return deserialized body, or {@code null} when the server returns {@code 304} (see {@link
   *     TranslaasRequestContext#isNotModified()})
   */
  public CompletableFuture<ProjectLocalesResponse> getProjectLocales(String project) {
    return getProjectLocales(project, null, null);
  }

  /**
   * Same as {@link #getProjectLocales(String)} with optional per-request context (conditional GET
   * via {@link TranslaasRequestContext#setIfNoneMatch(String)} when {@link
   * TranslaasOptions#isUseConditionalRequests()} is true).
   */
  public CompletableFuture<ProjectLocalesResponse> getProjectLocales(
      String project, TranslaasRequestContext context) {
    return getProjectLocales(project, context, null);
  }

  /**
   * @param executor optional executor for the async task; defaults to the common pool
   */
  public CompletableFuture<ProjectLocalesResponse> getProjectLocales(
      String project, TranslaasRequestContext context, Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(
        () -> getProjectLocalesBlocking(project, context), exec);
  }

  private ProjectLocalesResponse getProjectLocalesBlocking(
      String project, TranslaasRequestContext context) throws TranslaasApiException {
    if (project == null || project.isBlank()) {
      throw new IllegalArgumentException("project is required");
    }
    if (context != null) {
      context.clearResponseMetadata();
    }
    LinkedHashMap<String, String> query = new LinkedHashMap<>();
    query.put("project", project);
    LinkedHashMap<String, String> merged =
        TranslaasUris.mergeQueryParams(http.getOptions(), context, query);
    if (responseCache != null) {
      Optional<TranslaasCacheEntry> cached =
          responseCache.tryGet(paths.locales(), merged, context);
      if (cached.isPresent()) {
        return readProjectLocales(
            TranslationResponseCache.utf8String(cached.get().getValue()), 200, null);
      }
    }
    HttpResponse<String> response = http.get(paths.locales(), query, context);
    if (response.statusCode() == 204) {
      return TranslationResponses.emptyLocales(project);
    }
    if (response.statusCode() == 304) {
      if (responseCache != null) {
        Optional<TranslaasCacheEntry> fromCache =
            responseCache.getIgnoringValidationBypass(paths.locales(), merged);
        if (fromCache.isPresent()) {
          copyEtagFromCacheToContext(context, fromCache.get());
          return readProjectLocales(
              TranslationResponseCache.utf8String(fromCache.get().getValue()),
              304,
              response.uri().toString());
        }
      }
      return TranslationResponses.emptyLocales(project);
    }
    String body = response.body();
    if (responseCache != null) {
      responseCache.putIfApplicable(
          paths.locales(),
          merged,
          TranslationResponseCache.utf8Bytes(body != null ? body : ""),
          response.headers().firstValue("ETag"));
    }
    return readProjectLocales(body != null ? body : "", response.statusCode(), response.uri().toString());
  }

  private ProjectLocalesResponse readProjectLocales(String body, int statusCode, String uriForError)
      throws TranslaasApiException {
    try {
      return TranslaasJson.mapper().readValue(body, ProjectLocalesResponse.class);
    } catch (IOException e) {
      throw new TranslaasApiException(
          statusCode,
          snippet(body),
          "Failed to parse project locales JSON: " + (uriForError != null ? uriForError : "cache"),
          e);
    }
  }

  /**
   * Fetches all entries for a single group ({@code application/json}).
   *
   * <p>Query parameters: required {@code project}, {@code group}, {@code lang}; optional {@code
   * channel}, {@code v}, {@code includeContext} from {@link TranslaasOptions} and {@link
   * TranslaasRequestContext}.
   *
   * @return deserialized body, or {@code null} when the server returns {@code 304} (see {@link
   *     TranslaasRequestContext#isNotModified()})
   */
  public CompletableFuture<GroupTranslationsResponse> getGroupTranslations(
      String project, String group, String lang) {
    return getGroupTranslations(project, group, lang, null, null, null);
  }

  /**
   * Same as {@link #getGroupTranslations(String, String, String)} with optional per-request context
   * (conditional GET when {@link TranslaasOptions#isUseConditionalRequests()} is true).
   */
  public CompletableFuture<GroupTranslationsResponse> getGroupTranslations(
      String project, String group, String lang, TranslaasRequestContext context) {
    return getGroupTranslations(project, group, lang, null, context, null);
  }

  /**
   * @param executor optional executor for the async task; defaults to the common pool
   */
  public CompletableFuture<GroupTranslationsResponse> getGroupTranslations(
      String project, String group, String lang, TranslaasRequestContext context, Executor executor) {
    return getGroupTranslations(project, group, lang, null, context, executor);
  }

  /**
   * @param format optional response shape ({@link #FORMAT_FLAT_JSON}); {@code null} omits the query
   *     parameter
   * @param executor optional executor for the async task; defaults to the common pool
   */
  public CompletableFuture<GroupTranslationsResponse> getGroupTranslations(
      String project,
      String group,
      String lang,
      String format,
      TranslaasRequestContext context,
      Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(
        () -> getGroupTranslationsBlocking(project, group, lang, format, context), exec);
  }

  private GroupTranslationsResponse getGroupTranslationsBlocking(
      String project, String group, String lang, String format, TranslaasRequestContext context)
      throws TranslaasApiException {
    requireNonBlank(project, "project");
    requireNonBlank(group, "group");
    requireNonBlank(lang, "lang");
    if (context != null) {
      context.clearResponseMetadata();
    }
    LinkedHashMap<String, String> query = new LinkedHashMap<>();
    query.put("project", project);
    query.put("group", group);
    query.put("lang", lang);
    if (format != null && !format.isBlank()) {
      query.put("format", format);
    }
    LinkedHashMap<String, String> merged =
        TranslaasUris.mergeQueryParams(http.getOptions(), context, query);
    if (responseCache != null) {
      Optional<TranslaasCacheEntry> cached =
          responseCache.tryGet(paths.group(), merged, context);
      if (cached.isPresent()) {
        return readGroupTranslations(
            TranslationResponseCache.utf8String(cached.get().getValue()), format, 200, null);
      }
    }
    HttpResponse<String> response = http.get(paths.group(), query, context);
    if (response.statusCode() == 204) {
      return TranslationResponses.emptyGroup(project, group, lang);
    }
    if (response.statusCode() == 304) {
      if (responseCache != null) {
        Optional<TranslaasCacheEntry> fromCache =
            responseCache.getIgnoringValidationBypass(paths.group(), merged);
        if (fromCache.isPresent()) {
          copyEtagFromCacheToContext(context, fromCache.get());
          return readGroupTranslations(
              TranslationResponseCache.utf8String(fromCache.get().getValue()),
              format,
              304,
              response.uri().toString());
        }
      }
      return TranslationResponses.emptyGroup(project, group, lang);
    }
    String body = response.body();
    if (responseCache != null) {
      responseCache.putIfApplicable(
          paths.group(),
          merged,
          TranslationResponseCache.utf8Bytes(body != null ? body : ""),
          response.headers().firstValue("ETag"));
    }
    return readGroupTranslations(
        body != null ? body : "", format, response.statusCode(), response.uri().toString());
  }

  private GroupTranslationsResponse readGroupTranslations(
      String body, String format, int statusCode, String uriForError) throws TranslaasApiException {
    try {
      return TranslationResponseParsing.parseGroupResponse(body, format);
    } catch (TranslaasApiException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new TranslaasApiException(
          statusCode,
          snippet(body),
          "Failed to parse group translations JSON: " + (uriForError != null ? uriForError : "cache"),
          e);
    }
  }

  /**
   * Fetches all translations for a project ({@code application/json}), nested or flat-json per
   * {@code format}.
   *
   * <p>Query parameters: required {@code project}, {@code lang}; optional {@code format}, {@code
   * channel}, {@code v}, {@code includeContext} from options and context.
   *
   * @return deserialized body, or {@code null} when the server returns {@code 304}
   */
  public CompletableFuture<ProjectTranslationsResponse> getProjectTranslations(
      String project, String lang) {
    return getProjectTranslations(project, lang, null, null, null);
  }

  public CompletableFuture<ProjectTranslationsResponse> getProjectTranslations(
      String project, String lang, TranslaasRequestContext context) {
    return getProjectTranslations(project, lang, null, context, null);
  }

  /**
   * @param executor optional executor for the async task; defaults to the common pool
   */
  public CompletableFuture<ProjectTranslationsResponse> getProjectTranslations(
      String project, String lang, TranslaasRequestContext context, Executor executor) {
    return getProjectTranslations(project, lang, null, context, executor);
  }

  /**
   * @param format optional {@link #FORMAT_FLAT_JSON}; {@code null} uses the API default (nested
   *     groups)
   * @param executor optional executor for the async task; defaults to the common pool
   */
  public CompletableFuture<ProjectTranslationsResponse> getProjectTranslations(
      String project,
      String lang,
      String format,
      TranslaasRequestContext context,
      Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(
        () -> getProjectTranslationsBlocking(project, lang, format, context), exec);
  }

  private ProjectTranslationsResponse getProjectTranslationsBlocking(
      String project, String lang, String format, TranslaasRequestContext context)
      throws TranslaasApiException {
    requireNonBlank(project, "project");
    requireNonBlank(lang, "lang");
    if (context != null) {
      context.clearResponseMetadata();
    }
    LinkedHashMap<String, String> query = new LinkedHashMap<>();
    query.put("project", project);
    query.put("lang", lang);
    if (format != null && !format.isBlank()) {
      query.put("format", format);
    }
    LinkedHashMap<String, String> merged =
        TranslaasUris.mergeQueryParams(http.getOptions(), context, query);
    if (responseCache != null) {
      Optional<TranslaasCacheEntry> cached =
          responseCache.tryGet(paths.project(), merged, context);
      if (cached.isPresent()) {
        return readProjectTranslations(
            TranslationResponseCache.utf8String(cached.get().getValue()), format, 200, null);
      }
    }
    HttpResponse<String> response = http.get(paths.project(), query, context);
    if (response.statusCode() == 204) {
      return TranslationResponses.emptyProject(project, lang);
    }
    if (response.statusCode() == 304) {
      if (responseCache != null) {
        Optional<TranslaasCacheEntry> fromCache =
            responseCache.getIgnoringValidationBypass(paths.project(), merged);
        if (fromCache.isPresent()) {
          copyEtagFromCacheToContext(context, fromCache.get());
          return readProjectTranslations(
              TranslationResponseCache.utf8String(fromCache.get().getValue()),
              format,
              304,
              response.uri().toString());
        }
      }
      return TranslationResponses.emptyProject(project, lang);
    }
    String body = response.body();
    if (responseCache != null) {
      responseCache.putIfApplicable(
          paths.project(),
          merged,
          TranslationResponseCache.utf8Bytes(body != null ? body : ""),
          response.headers().firstValue("ETag"));
    }
    return readProjectTranslations(
        body != null ? body : "", format, response.statusCode(), response.uri().toString());
  }

  private ProjectTranslationsResponse readProjectTranslations(
      String body, String format, int statusCode, String uriForError)
      throws TranslaasApiException {
    try {
      return TranslationResponseParsing.parseProjectResponse(body, format);
    } catch (TranslaasApiException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new TranslaasApiException(
          statusCode,
          snippet(body),
          "Failed to parse project translations JSON: "
              + (uriForError != null ? uriForError : "cache"),
          e);
    }
  }

  /**
   * Validates the configured API key ({@code GET} {@value #API_KEYS_VALIDATE_PATH},
   * {@code application/json}). Used for connectivity and bootstrap checks.
   *
   * <p>Uses the same {@link TranslaasOptions#getApiKeyHeader() API key header} as other calls.
   */
  public CompletableFuture<ValidateApiKeyResponse> validateApiKey() {
    return validateApiKey(null, null);
  }

  /**
   * Same as {@link #validateApiKey()} with optional per-request context (e.g. conditional headers
   * when {@link TranslaasOptions#isUseConditionalRequests()} is true).
   */
  public CompletableFuture<ValidateApiKeyResponse> validateApiKey(TranslaasRequestContext context) {
    return validateApiKey(context, null);
  }

  /**
   * @param executor optional executor for the async task; defaults to the common pool
   */
  public CompletableFuture<ValidateApiKeyResponse> validateApiKey(
      TranslaasRequestContext context, Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(() -> validateApiKeyBlocking(context), exec);
  }

  private ValidateApiKeyResponse validateApiKeyBlocking(TranslaasRequestContext context)
      throws TranslaasApiException {
    if (context != null) {
      context.clearResponseMetadata();
    }
    HttpResponse<String> response = http.get(API_KEYS_VALIDATE_PATH, null, context);
    if (response.statusCode() == 304) {
      return null;
    }
    String body = response.body();
    try {
      return TranslaasJson.mapper()
          .readValue(body != null ? body : "", ValidateApiKeyResponse.class);
    } catch (IOException e) {
      throw new TranslaasApiException(
          response.statusCode(),
          snippet(body),
          "Failed to parse validate API key JSON: " + response.uri(),
          e);
    }
  }

  /**
   * Reports missing translation keys ({@code Content-Type: application/json}). The server responds
   * with {@code 202 Accepted} on success.
   *
   * <p><strong>Authentication:</strong> requires a <em>project-scoped</em> API key; otherwise the
   * server typically returns {@code 401 Unauthorized}.
   *
   * @param request body with {@code keys} (see {@link ReportMissingKeysRequest})
   */
  public CompletableFuture<Void> reportMissingKeys(ReportMissingKeysRequest request) {
    return reportMissingKeys(request, null, null);
  }

  /**
   * Same as {@link #reportMissingKeys(ReportMissingKeysRequest)} with optional per-request query
   * overrides (e.g. {@link TranslaasRequestContext#setProject(String)} for project scope).
   */
  public CompletableFuture<Void> reportMissingKeys(
      ReportMissingKeysRequest request, TranslaasRequestContext context) {
    return reportMissingKeys(request, context, null);
  }

  /**
   * @param executor optional executor for the async task; defaults to the common pool
   */
  public CompletableFuture<Void> reportMissingKeys(
      ReportMissingKeysRequest request, TranslaasRequestContext context, Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.runAsync(() -> reportMissingKeysBlocking(request, context), exec);
  }

  private void reportMissingKeysBlocking(ReportMissingKeysRequest request, TranslaasRequestContext context)
      throws TranslaasApiException {
    Objects.requireNonNull(request, "request");
    if (request.getKeys() == null || request.getKeys().isEmpty()) {
      return;
    }
    if (context != null) {
      context.clearResponseMetadata();
    }
    String json;
    try {
      json = TranslaasJson.mapper().writeValueAsString(request);
    } catch (JsonProcessingException e) {
      throw new TranslaasApiException(0, null, "Failed to serialize report-missing request body", e);
    }
    http.post(paths.reportMissing(), null, json, null, context);
  }

  /**
   * Downloads the offline translation cache as a ZIP ({@code application/zip}).
   *
   * <p>Query parameters: required {@code project}; optional {@code channel}, {@code v}, {@code
   * includeContext} from {@link TranslaasOptions} defaults and {@link TranslaasRequestContext}
   * overrides.
   *
   * <p>The returned {@link OfflineCacheDownloadResult#getZipBytes()} array is owned by the result;
   * callers may retain it as long as they retain the result instance.
   *
   * @param project required project key
   * @return ZIP bytes and optional {@code Content-Disposition} filename, or {@code null} when the
   *     server returns {@code 304} (see {@link TranslaasRequestContext#isNotModified()})
   */
  public CompletableFuture<OfflineCacheDownloadResult> getOfflineCache(String project) {
    return getOfflineCache(project, null, null);
  }

  /**
   * Same as {@link #getOfflineCache(String)} with optional per-request context (conditional GET via
   * {@link TranslaasRequestContext#setIfNoneMatch(String)} when {@link
   * TranslaasOptions#isUseConditionalRequests()} is true).
   */
  public CompletableFuture<OfflineCacheDownloadResult> getOfflineCache(
      String project, TranslaasRequestContext context) {
    return getOfflineCache(project, context, null);
  }

  /**
   * @param executor optional executor for the async task; defaults to the common pool
   */
  public CompletableFuture<OfflineCacheDownloadResult> getOfflineCache(
      String project, TranslaasRequestContext context, Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(() -> getOfflineCacheBlocking(project, context), exec);
  }

  private OfflineCacheDownloadResult getOfflineCacheBlocking(
      String project, TranslaasRequestContext context) throws TranslaasApiException {
    requireNonBlank(project, "project");
    if (context != null) {
      context.clearResponseMetadata();
    }
    LinkedHashMap<String, String> query = new LinkedHashMap<>();
    query.put("project", project);
    LinkedHashMap<String, String> merged =
        TranslaasUris.mergeQueryParams(http.getOptions(), context, query);
    if (responseCache != null) {
      Optional<TranslaasCacheEntry> cached =
          responseCache.tryGet(paths.offlineCache(), merged, context);
      if (cached.isPresent()) {
        byte[] b = cached.get().getValue();
        return new OfflineCacheDownloadResult(b != null ? b : new byte[0], null);
      }
    }
    HttpResponse<byte[]> response =
        http.getBytes(paths.offlineCache(), query, context);
    if (response.statusCode() == 304) {
      if (responseCache != null) {
        Optional<TranslaasCacheEntry> fromCache =
            responseCache.getIgnoringValidationBypass(paths.offlineCache(), merged);
        if (fromCache.isPresent()) {
          copyEtagFromCacheToContext(context, fromCache.get());
          byte[] b = fromCache.get().getValue();
          return new OfflineCacheDownloadResult(b != null ? b : new byte[0], null);
        }
      }
      return OfflineCacheDownloadResult.notModified();
    }
    byte[] body = response.body();
    if (responseCache != null) {
      responseCache.putIfApplicable(
          paths.offlineCache(),
          merged,
          body != null ? body : new byte[0],
          response.headers().firstValue("ETag"));
    }
    String filename =
        ContentDispositionFilenames.parseFilename(
            response.headers().firstValue("Content-Disposition").orElse(null));
    return new OfflineCacheDownloadResult(body != null ? body : new byte[0], filename);
  }

  private static void requireNonBlank(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(name + " is required");
    }
  }

  private static String snippet(String body) {
    if (body == null) {
      return null;
    }
    return body.length() > BODY_SNIPPET_MAX ? body.substring(0, BODY_SNIPPET_MAX) : body;
  }
}
