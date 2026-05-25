package io.translaas.client;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Configuration for offline file-based translation caching. */
public final class OfflineCacheOptions {

  public static final String DEFAULT_CACHE_DIRECTORY = ".translaas-cache";

  private final boolean enabled;
  private final String cacheDirectory;
  private final OfflineFallbackMode fallbackMode;
  private final boolean autoSync;
  private final Duration autoSyncInterval;
  private final List<String> projects;
  private final List<String> languages;
  private final String defaultProjectId;
  private final OfflineHybridCacheOptions hybridCache;

  private OfflineCacheOptions(Builder builder) {
    this.enabled = builder.enabled;
    this.cacheDirectory =
        builder.cacheDirectory != null ? builder.cacheDirectory : DEFAULT_CACHE_DIRECTORY;
    this.fallbackMode =
        builder.fallbackMode != null ? builder.fallbackMode : OfflineFallbackMode.CACHE_FIRST;
    this.autoSync = builder.autoSync;
    this.autoSyncInterval = builder.autoSyncInterval;
    this.projects = Collections.unmodifiableList(new ArrayList<>(builder.projects));
    this.languages = Collections.unmodifiableList(new ArrayList<>(builder.languages));
    this.defaultProjectId = builder.defaultProjectId;
    this.hybridCache =
        builder.hybridCache != null ? builder.hybridCache : OfflineHybridCacheOptions.defaults();
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getCacheDirectory() {
    return cacheDirectory;
  }

  public OfflineFallbackMode getFallbackMode() {
    return fallbackMode;
  }

  public boolean isAutoSync() {
    return autoSync;
  }

  public Duration getAutoSyncInterval() {
    return autoSyncInterval;
  }

  public List<String> getProjects() {
    return projects;
  }

  public List<String> getLanguages() {
    return languages;
  }

  public String getDefaultProjectId() {
    return defaultProjectId;
  }

  public OfflineHybridCacheOptions getHybridCache() {
    return hybridCache;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private boolean enabled;
    private String cacheDirectory = DEFAULT_CACHE_DIRECTORY;
    private OfflineFallbackMode fallbackMode = OfflineFallbackMode.CACHE_FIRST;
    private boolean autoSync = true;
    private Duration autoSyncInterval;
    private final List<String> projects = new ArrayList<>();
    private final List<String> languages = new ArrayList<>();
    private String defaultProjectId;
    private OfflineHybridCacheOptions hybridCache;

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder cacheDirectory(String cacheDirectory) {
      this.cacheDirectory = cacheDirectory;
      return this;
    }

    public Builder fallbackMode(OfflineFallbackMode fallbackMode) {
      this.fallbackMode = Objects.requireNonNull(fallbackMode);
      return this;
    }

    public Builder autoSync(boolean autoSync) {
      this.autoSync = autoSync;
      return this;
    }

    public Builder autoSyncInterval(Duration autoSyncInterval) {
      this.autoSyncInterval = autoSyncInterval;
      return this;
    }

    public Builder projects(List<String> projects) {
      this.projects.clear();
      if (projects != null) {
        this.projects.addAll(projects);
      }
      return this;
    }

    public Builder languages(List<String> languages) {
      this.languages.clear();
      if (languages != null) {
        this.languages.addAll(languages);
      }
      return this;
    }

    public Builder defaultProjectId(String defaultProjectId) {
      this.defaultProjectId = defaultProjectId;
      return this;
    }

    public Builder hybridCache(OfflineHybridCacheOptions hybridCache) {
      this.hybridCache = hybridCache;
      return this;
    }

    public OfflineCacheOptions build() {
      return new OfflineCacheOptions(this);
    }
  }
}
