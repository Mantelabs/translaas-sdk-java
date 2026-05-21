package io.mantelabs.translaas.spring.boot.autoconfigure;

import io.mantelabs.translaas.CacheMode;
import io.mantelabs.translaas.TranslaasOptions;
import io.mantelabs.translaas.caching.MemoryTranslaasCacheOptions;
import io.mantelabs.translaas.caching.MemoryTranslaasCacheProvider;
import io.mantelabs.translaas.caching.TranslaasCacheProvider;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code translaas.*} keys from Spring {@code Environment} into {@link TranslaasOptions}.
 */
@ConfigurationProperties(prefix = "translaas")
public class TranslaasProperties {

  private boolean enabled = true;

  private String apiKey;
  private String baseUrl;
  private String defaultLanguage;
  /** One of {@link CacheMode} names, for example {@code GROUP}. */
  private String cacheMode = CacheMode.NONE.name();

  private Duration cacheAbsoluteExpiration;
  private Duration cacheSlidingExpiration;
  private Duration timeout;

  private String defaultProject;
  private String channel;
  private String snapshotVersion;
  private Boolean includeContextDefault;
  private boolean useConditionalRequests = true;
  private boolean skipApiValidation;
  private String apiKeyHeader;
  private String sdkTranslationsPathPrefix;

  private final Caching caching = new Caching();
  private final LocaleSettings locale = new LocaleSettings();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getDefaultLanguage() {
    return defaultLanguage;
  }

  public void setDefaultLanguage(String defaultLanguage) {
    this.defaultLanguage = defaultLanguage;
  }

  public String getCacheMode() {
    return cacheMode;
  }

  public void setCacheMode(String cacheMode) {
    this.cacheMode = cacheMode;
  }

  public Duration getCacheAbsoluteExpiration() {
    return cacheAbsoluteExpiration;
  }

  public void setCacheAbsoluteExpiration(Duration cacheAbsoluteExpiration) {
    this.cacheAbsoluteExpiration = cacheAbsoluteExpiration;
  }

  public Duration getCacheSlidingExpiration() {
    return cacheSlidingExpiration;
  }

  public void setCacheSlidingExpiration(Duration cacheSlidingExpiration) {
    this.cacheSlidingExpiration = cacheSlidingExpiration;
  }

  public Duration getTimeout() {
    return timeout;
  }

  public void setTimeout(Duration timeout) {
    this.timeout = timeout;
  }

  public String getDefaultProject() {
    return defaultProject;
  }

  public void setDefaultProject(String defaultProject) {
    this.defaultProject = defaultProject;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getSnapshotVersion() {
    return snapshotVersion;
  }

  public void setSnapshotVersion(String snapshotVersion) {
    this.snapshotVersion = snapshotVersion;
  }

  public Boolean getIncludeContextDefault() {
    return includeContextDefault;
  }

  public void setIncludeContextDefault(Boolean includeContextDefault) {
    this.includeContextDefault = includeContextDefault;
  }

  public boolean isUseConditionalRequests() {
    return useConditionalRequests;
  }

  public void setUseConditionalRequests(boolean useConditionalRequests) {
    this.useConditionalRequests = useConditionalRequests;
  }

  public boolean isSkipApiValidation() {
    return skipApiValidation;
  }

  public void setSkipApiValidation(boolean skipApiValidation) {
    this.skipApiValidation = skipApiValidation;
  }

  public String getApiKeyHeader() {
    return apiKeyHeader;
  }

  public void setApiKeyHeader(String apiKeyHeader) {
    this.apiKeyHeader = apiKeyHeader;
  }

  public String getSdkTranslationsPathPrefix() {
    return sdkTranslationsPathPrefix;
  }

  public void setSdkTranslationsPathPrefix(String sdkTranslationsPathPrefix) {
    this.sdkTranslationsPathPrefix = sdkTranslationsPathPrefix;
  }

  public Caching getCaching() {
    return caching;
  }

  public LocaleSettings getLocale() {
    return locale;
  }

  /**
   * Builds the facade {@link TranslaasOptions} used by {@link io.mantelabs.translaas.TranslaasService}.
   *
   * @param cacheProvider optional bean (for example {@link MemoryTranslaasCacheProvider})
   */
  public TranslaasOptions toFacadeOptions(TranslaasCacheProvider cacheProvider) {
    TranslaasOptions.Builder b = TranslaasOptions.builder();
    b.apiKey(apiKey).baseUrl(baseUrl);
    if (defaultLanguage != null && !defaultLanguage.isBlank()) {
      b.defaultLanguage(defaultLanguage);
    }
    if (cacheMode != null && !cacheMode.isBlank()) {
      b.cacheMode(CacheMode.valueOf(cacheMode.trim().toUpperCase(java.util.Locale.ROOT)));
    }
    if (cacheAbsoluteExpiration != null) {
      b.cacheAbsoluteExpiration(cacheAbsoluteExpiration);
    }
    if (cacheSlidingExpiration != null) {
      b.cacheSlidingExpiration(cacheSlidingExpiration);
    }
    if (timeout != null) {
      b.timeout(timeout);
    }
    if (defaultProject != null && !defaultProject.isBlank()) {
      b.defaultProject(defaultProject);
    }
    if (channel != null && !channel.isBlank()) {
      b.channel(channel);
    }
    if (snapshotVersion != null && !snapshotVersion.isBlank()) {
      b.snapshotVersion(snapshotVersion);
    }
    if (includeContextDefault != null) {
      b.includeContextDefault(includeContextDefault);
    }
    b.useConditionalRequests(useConditionalRequests);
    b.skipApiValidation(skipApiValidation);
    if (apiKeyHeader != null && !apiKeyHeader.isBlank()) {
      b.apiKeyHeader(apiKeyHeader);
    }
    if (cacheProvider != null) {
      b.cacheProvider(cacheProvider);
    }
    if (sdkTranslationsPathPrefix != null && !sdkTranslationsPathPrefix.isBlank()) {
      b.sdkTranslationsPathPrefix(sdkTranslationsPathPrefix);
    }
    return b.build();
  }

  MemoryTranslaasCacheProvider newMemoryCacheProvider() {
    Caching.Memory m = caching.getMemory();
    if (m.getLruMaxEntries() != null) {
      return new MemoryTranslaasCacheProvider(MemoryTranslaasCacheOptions.lru(m.getLruMaxEntries()));
    }
    return new MemoryTranslaasCacheProvider();
  }

  public static final class Caching {

    private final Memory memory = new Memory();

    public Memory getMemory() {
      return memory;
    }

    public static final class Memory {

      /** When {@code true}, registers a {@link MemoryTranslaasCacheProvider} bean (if none declared). */
      private boolean enabled;

      /** When set, the memory provider uses an LRU cap. */
      private Integer lruMaxEntries;

      public boolean isEnabled() {
        return enabled;
      }

      public void setEnabled(boolean enabled) {
        this.enabled = enabled;
      }

      public Integer getLruMaxEntries() {
        return lruMaxEntries;
      }

      public void setLruMaxEntries(Integer lruMaxEntries) {
        this.lruMaxEntries = lruMaxEntries;
      }
    }
  }

  public static final class LocaleSettings {

    /**
     * When {@code true}, registers a {@link io.mantelabs.translaas.LanguageResolver} that reads the locale from Spring's
     * {@link org.springframework.context.i18n.LocaleContextHolder} (populated from the servlet request in
     * Spring MVC).
     */
    private boolean useSpringLocaleContext;

    public boolean isUseSpringLocaleContext() {
      return useSpringLocaleContext;
    }

    public void setUseSpringLocaleContext(boolean useSpringLocaleContext) {
      this.useSpringLocaleContext = useSpringLocaleContext;
    }
  }
}
