package io.mantelabs.translaas.caching.file;

import io.mantelabs.translaas.client.TranslaasOptions;

/**
 * Helpers for wiring file / hybrid caching with {@link TranslaasOptions}.
 */
public final class TranslaasCachingFile {

  private TranslaasCachingFile() {}

  /**
   * When {@code true}, application bootstrap may omit {@link io.mantelabs.translaas.client.TranslaasClient#validateApiKey()}
   * (offline or cache-only scenarios). Mirrors optional validation bypass flags in other SDKs.
   */
  public static boolean skipApiKeyValidation(TranslaasOptions options) {
    return options != null && options.isSkipApiValidation();
  }
}
