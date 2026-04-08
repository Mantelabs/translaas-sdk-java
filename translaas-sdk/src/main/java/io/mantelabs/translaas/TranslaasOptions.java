package io.mantelabs.translaas;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/**
 * Convenience configuration type for the {@code translaas-sdk} aggregator (see root {@code
 * README.md}). Delegates to {@link io.mantelabs.translaas.client.TranslaasOptions}.
 */
public final class TranslaasOptions {

  public static final String DEFAULT_API_KEY_HEADER =
      io.mantelabs.translaas.client.TranslaasOptions.DEFAULT_API_KEY_HEADER;

  private final io.mantelabs.translaas.client.TranslaasOptions clientOptions;

  private TranslaasOptions(io.mantelabs.translaas.client.TranslaasOptions clientOptions) {
    this.clientOptions = Objects.requireNonNull(clientOptions, "clientOptions");
  }

  /**
   * Underlying client configuration used by {@link TranslaasService} and {@link
   * io.mantelabs.translaas.client.TranslaasClient}.
   */
  public io.mantelabs.translaas.client.TranslaasOptions asClientOptions() {
    return clientOptions;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Builder mirroring {@link io.mantelabs.translaas.client.TranslaasOptions.Builder} with facade {@link CacheMode}. */
  public static final class Builder {

    private final io.mantelabs.translaas.client.TranslaasOptions.Builder inner =
        io.mantelabs.translaas.client.TranslaasOptions.builder();

    private Builder() {}

    public Builder apiKey(String apiKey) {
      inner.apiKey(apiKey);
      return this;
    }

    public Builder baseUrl(URI baseUrl) {
      inner.baseUrl(baseUrl);
      return this;
    }

    public Builder baseUrl(String baseUrl) {
      inner.baseUrl(baseUrl);
      return this;
    }

    public Builder defaultLanguage(String defaultLanguage) {
      inner.defaultLanguage(defaultLanguage);
      return this;
    }

    public Builder cacheMode(CacheMode cacheMode) {
      inner.cacheMode(cacheMode != null ? cacheMode.toClient() : io.mantelabs.translaas.client.CacheMode.NONE);
      return this;
    }

    public Builder cacheAbsoluteExpiration(Duration cacheAbsoluteExpiration) {
      inner.cacheAbsoluteExpiration(cacheAbsoluteExpiration);
      return this;
    }

    public Builder cacheSlidingExpiration(Duration cacheSlidingExpiration) {
      inner.cacheSlidingExpiration(cacheSlidingExpiration);
      return this;
    }

    public Builder timeout(Duration timeout) {
      inner.timeout(timeout);
      return this;
    }

    public Builder defaultProject(String defaultProject) {
      inner.defaultProject(defaultProject);
      return this;
    }

    public Builder channel(String channel) {
      inner.channel(channel);
      return this;
    }

    public Builder snapshotVersion(String snapshotVersion) {
      inner.snapshotVersion(snapshotVersion);
      return this;
    }

    public Builder includeContextDefault(Boolean includeContextDefault) {
      inner.includeContextDefault(includeContextDefault);
      return this;
    }

    public Builder useConditionalRequests(boolean useConditionalRequests) {
      inner.useConditionalRequests(useConditionalRequests);
      return this;
    }

    public Builder skipApiValidation(boolean skipApiValidation) {
      inner.skipApiValidation(skipApiValidation);
      return this;
    }

    public Builder apiKeyHeader(String apiKeyHeader) {
      inner.apiKeyHeader(apiKeyHeader);
      return this;
    }

    public TranslaasOptions build() {
      return new TranslaasOptions(inner.build());
    }
  }
}
