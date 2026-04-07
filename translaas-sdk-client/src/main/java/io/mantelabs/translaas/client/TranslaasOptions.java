package io.mantelabs.translaas.client;

import io.mantelabs.translaas.client.http.TranslaasUris;
import io.mantelabs.translaas.models.exception.TranslaasConfigurationException;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * SDK configuration: API origin, authentication, timeouts, caching defaults, and shared request
 * defaults (channel, snapshot version, project, conditional requests).
 */
public final class TranslaasOptions {

  /** Default API key header name (OpenAPI / other SDK parity). */
  public static final String DEFAULT_API_KEY_HEADER = "X-Api-Key";

  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

  private final String apiKey;
  private final URI baseUrl;
  private final String defaultLanguage;
  private final CacheMode cacheMode;
  private final Duration cacheAbsoluteExpiration;
  private final Duration cacheSlidingExpiration;
  private final Duration timeout;
  private final String defaultProject;
  private final String channel;
  private final String snapshotVersion;
  private final Boolean includeContextDefault;
  private final boolean useConditionalRequests;
  private final String apiKeyHeader;

  public String getApiKey() {
    return apiKey;
  }

  /**
   * Normalized API origin (scheme, host, port only), without trailing path segments from user
   * input.
   */
  public URI getBaseUrl() {
    return baseUrl;
  }

  public Optional<String> getDefaultLanguage() {
    return Optional.ofNullable(defaultLanguage);
  }

  public CacheMode getCacheMode() {
    return cacheMode;
  }

  public Optional<Duration> getCacheAbsoluteExpiration() {
    return Optional.ofNullable(cacheAbsoluteExpiration);
  }

  public Optional<Duration> getCacheSlidingExpiration() {
    return Optional.ofNullable(cacheSlidingExpiration);
  }

  public Duration getTimeout() {
    return timeout;
  }

  public Optional<String> getDefaultProject() {
    return Optional.ofNullable(defaultProject);
  }

  public Optional<String> getChannel() {
    return Optional.ofNullable(channel);
  }

  /**
   * Default snapshot / bundle version, sent as query {@code v} when not overridden per request.
   */
  public Optional<String> getSnapshotVersion() {
    return Optional.ofNullable(snapshotVersion);
  }

  public Optional<Boolean> getIncludeContextDefault() {
    return Optional.ofNullable(includeContextDefault);
  }

  public boolean isUseConditionalRequests() {
    return useConditionalRequests;
  }

  public String getApiKeyHeader() {
    return apiKeyHeader;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Mutable builder for {@link TranslaasOptions}. */
  public static final class Builder {

    private String apiKey;
    private URI baseUrl;
    private String defaultLanguage;
    private CacheMode cacheMode = CacheMode.NONE;
    private Duration cacheAbsoluteExpiration;
    private Duration cacheSlidingExpiration;
    private Duration timeout;
    private String defaultProject;
    private String channel;
    private String snapshotVersion;
    private Boolean includeContextDefault;
    private boolean useConditionalRequests = true;
    private String apiKeyHeader;

    private Builder() {}

    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public Builder baseUrl(URI baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl != null ? URI.create(baseUrl) : null;
      return this;
    }

    public Builder defaultLanguage(String defaultLanguage) {
      this.defaultLanguage = defaultLanguage;
      return this;
    }

    public Builder cacheMode(CacheMode cacheMode) {
      this.cacheMode = cacheMode != null ? cacheMode : CacheMode.NONE;
      return this;
    }

    public Builder cacheAbsoluteExpiration(Duration cacheAbsoluteExpiration) {
      this.cacheAbsoluteExpiration = cacheAbsoluteExpiration;
      return this;
    }

    public Builder cacheSlidingExpiration(Duration cacheSlidingExpiration) {
      this.cacheSlidingExpiration = cacheSlidingExpiration;
      return this;
    }

    public Builder timeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    public Builder defaultProject(String defaultProject) {
      this.defaultProject = defaultProject;
      return this;
    }

    public Builder channel(String channel) {
      this.channel = channel;
      return this;
    }

    public Builder snapshotVersion(String snapshotVersion) {
      this.snapshotVersion = snapshotVersion;
      return this;
    }

    public Builder includeContextDefault(Boolean includeContextDefault) {
      this.includeContextDefault = includeContextDefault;
      return this;
    }

    public Builder useConditionalRequests(boolean useConditionalRequests) {
      this.useConditionalRequests = useConditionalRequests;
      return this;
    }

    public Builder apiKeyHeader(String apiKeyHeader) {
      this.apiKeyHeader = apiKeyHeader;
      return this;
    }

    public TranslaasOptions build() {
      if (apiKey == null || apiKey.isBlank()) {
        throw new TranslaasConfigurationException("apiKey is required");
      }
      if (baseUrl == null) {
        throw new TranslaasConfigurationException("baseUrl is required");
      }
      URI normalized = TranslaasUris.normalizeApiOrigin(baseUrl);
      return new TranslaasOptions(this, normalized);
    }
  }

  private TranslaasOptions(Builder builder, URI normalizedBaseUrl) {
    this.apiKey = builder.apiKey;
    this.baseUrl = Objects.requireNonNull(normalizedBaseUrl);
    this.defaultLanguage = builder.defaultLanguage;
    this.cacheMode = builder.cacheMode;
    this.cacheAbsoluteExpiration = builder.cacheAbsoluteExpiration;
    this.cacheSlidingExpiration = builder.cacheSlidingExpiration;
    this.timeout = builder.timeout != null ? builder.timeout : DEFAULT_TIMEOUT;
    this.defaultProject = builder.defaultProject;
    this.channel = builder.channel;
    this.snapshotVersion = builder.snapshotVersion;
    this.includeContextDefault = builder.includeContextDefault;
    this.useConditionalRequests = builder.useConditionalRequests;
    this.apiKeyHeader = builder.apiKeyHeader != null ? builder.apiKeyHeader : DEFAULT_API_KEY_HEADER;
  }
}
