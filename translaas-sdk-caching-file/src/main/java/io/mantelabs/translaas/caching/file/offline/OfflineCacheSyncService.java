package io.mantelabs.translaas.caching.file.offline;

import io.mantelabs.translaas.client.OfflineCacheOptions;
import io.mantelabs.translaas.client.TranslaasTranslationClient;
import io.mantelabs.translaas.models.OfflineCacheDownloadResult;
import io.mantelabs.translaas.models.ProjectLocalesResponse;
import io.mantelabs.translaas.models.ProjectTranslationsResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Synchronizes on-disk offline cache with the Translaas API. */
public final class OfflineCacheSyncService {

  private final TranslaasTranslationClient client;
  private final IOfflineCacheProvider cache;
  private final OfflineCacheOptions options;
  private final Object lock = new Object();
  private ScheduledExecutorService scheduler;
  private ScheduledFuture<?> backgroundTask;
  private final AtomicBoolean stopRequested = new AtomicBoolean();

  public OfflineCacheSyncService(
      TranslaasTranslationClient client,
      IOfflineCacheProvider cache,
      OfflineCacheOptions options) {
    this.client = client;
    this.cache = cache;
    this.options = options;
  }

  public boolean isBackgroundSyncRunning() {
    return backgroundTask != null && !backgroundTask.isDone();
  }

  public CompletableFuture<Void> syncProject(String project, String lang) {
    return CompletableFuture.runAsync(
        () -> {
          synchronized (lock) {
            ProjectTranslationsResponse data =
                client.getProjectTranslations(project, lang).join();
            cache.saveProject(project, lang, data);
          }
        });
  }

  public CompletableFuture<Void> syncProjectAllLanguages(String project) {
    return CompletableFuture.runAsync(
        () -> {
          synchronized (lock) {
            ProjectLocalesResponse locales = client.getProjectLocales(project).join();
            cache.saveProjectLocales(project, locales);
            List<String> languages = filterLanguages(locales, options.getLanguages());
            for (String lang : languages) {
              try {
                ProjectTranslationsResponse data =
                    client.getProjectTranslations(project, lang).join();
                cache.saveProject(project, lang, data);
              } catch (RuntimeException ignored) {
                // continue other languages
              }
            }
          }
        });
  }

  public CompletableFuture<Void> syncFromOfflineZip(String project) {
    return client
        .getOfflineCache(project)
        .thenApply(
            result -> {
              if (result.isNotModified()
                  || result.getZipBytes() == null
                  || result.getZipBytes().length == 0) {
                return null;
              }
              try {
                OfflineBundle bundle = OfflineZipBundle.parseOfflineZip(result.getZipBytes());
                String key = OfflineZipBundle.resolveProjectKey(bundle, project);
                cache.applyOfflineBundle(
                    project,
                    bundle.getLocalesByProject().get(key),
                    bundle.getProjectsByProjectLang().get(key));
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
              return null;
            });
  }

  public CompletableFuture<Void> syncAll() {
    List<CompletableFuture<Void>> tasks = new ArrayList<>();
    for (String project : options.getProjects()) {
      tasks.add(
          syncProjectAllLanguages(project)
              .exceptionally(ex -> null));
    }
    return CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
  }

  public void startBackgroundSync() {
    if (isBackgroundSyncRunning()) {
      return;
    }
    if (!options.isAutoSync() || options.getAutoSyncInterval() == null) {
      return;
    }
    stopRequested.set(false);
    scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "translaas-offline-sync");
      t.setDaemon(true);
      return t;
    });
    long seconds = options.getAutoSyncInterval().getSeconds();
    backgroundTask =
        scheduler.scheduleWithFixedDelay(
            () -> {
              if (!stopRequested.get()) {
                syncAll().exceptionally(ex -> null);
              }
            },
            0,
            Math.max(1, seconds),
            TimeUnit.SECONDS);
  }

  public void stopBackgroundSync() {
    stopRequested.set(true);
    if (backgroundTask != null) {
      backgroundTask.cancel(true);
      backgroundTask = null;
    }
    if (scheduler != null) {
      scheduler.shutdownNow();
      scheduler = null;
    }
  }

  private static List<String> filterLanguages(
      ProjectLocalesResponse locales, List<String> configured) {
    if (locales == null || locales.getLocales() == null) {
      return List.of();
    }
    if (configured == null || configured.isEmpty()) {
      return locales.getLocales();
    }
    List<String> out = new ArrayList<>();
    for (String lang : locales.getLocales()) {
      for (String cfg : configured) {
        if (cfg != null && cfg.equalsIgnoreCase(lang)) {
          out.add(lang);
          break;
        }
      }
    }
    return out;
  }
}
