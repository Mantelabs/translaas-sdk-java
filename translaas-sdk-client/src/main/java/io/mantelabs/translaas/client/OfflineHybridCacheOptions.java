package io.mantelabs.translaas.client;

import java.time.Duration;
import java.util.Objects;

/** Hybrid memory + file cache settings for offline mode. */
public final class OfflineHybridCacheOptions {

  private final boolean enabled;
  private final Duration memoryCacheExpiration;
  private final Integer maxMemoryCacheEntries;
  private final boolean warmupOnStartup;

  private OfflineHybridCacheOptions(Builder builder) {
    this.enabled = builder.enabled;
    this.memoryCacheExpiration = builder.memoryCacheExpiration;
    this.maxMemoryCacheEntries = builder.maxMemoryCacheEntries;
    this.warmupOnStartup = builder.warmupOnStartup;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public Duration getMemoryCacheExpiration() {
    return memoryCacheExpiration;
  }

  public Integer getMaxMemoryCacheEntries() {
    return maxMemoryCacheEntries;
  }

  public boolean isWarmupOnStartup() {
    return warmupOnStartup;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private boolean enabled = true;
    private Duration memoryCacheExpiration;
    private Integer maxMemoryCacheEntries = 1000;
    private boolean warmupOnStartup;

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder memoryCacheExpiration(Duration memoryCacheExpiration) {
      this.memoryCacheExpiration = memoryCacheExpiration;
      return this;
    }

    public Builder maxMemoryCacheEntries(Integer maxMemoryCacheEntries) {
      this.maxMemoryCacheEntries = maxMemoryCacheEntries;
      return this;
    }

    public Builder warmupOnStartup(boolean warmupOnStartup) {
      this.warmupOnStartup = warmupOnStartup;
      return this;
    }

    public OfflineHybridCacheOptions build() {
      return new OfflineHybridCacheOptions(this);
    }
  }

  public static OfflineHybridCacheOptions defaults() {
    return builder().build();
  }
}
