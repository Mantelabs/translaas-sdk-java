package io.translaas.client;

/** Offline cache fallback behavior when serving translation reads. */
public enum OfflineFallbackMode {
  CACHE_FIRST,
  API_FIRST,
  CACHE_ONLY
}
