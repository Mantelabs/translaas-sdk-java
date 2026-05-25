package io.translaas.client;

import io.translaas.caching.MemoryTranslaasCacheProvider;
import io.translaas.caching.TranslaasCacheEntry;
import io.translaas.caching.TranslaasCacheProvider;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * L1 response cache for {@link TranslaasClient}: keys use {@link
 * io.translaas.caching.CacheKeyBuilder} (parity with .NET).
 */
final class TranslationResponseCache {

  private final TranslaasOptions options;
  private final SdkTranslationPaths paths;
  private final TranslaasCacheProvider provider;

  private TranslationResponseCache(
      TranslaasOptions options, SdkTranslationPaths paths, TranslaasCacheProvider provider) {
    this.options = Objects.requireNonNull(options, "options");
    this.paths = Objects.requireNonNull(paths, "paths");
    this.provider = Objects.requireNonNull(provider, "provider");
  }

  static TranslationResponseCache maybeCreate(TranslaasOptions options) {
    Objects.requireNonNull(options, "options");
    if (options.getCacheMode() == CacheMode.NONE) {
      return null;
    }
    SdkTranslationPaths paths = new SdkTranslationPaths(options);
    TranslaasCacheProvider p =
        options.getCacheProvider().orElseGet(MemoryTranslaasCacheProvider::new);
    return new TranslationResponseCache(options, paths, p);
  }

  boolean cachesPath(String path) {
    switch (options.getCacheMode()) {
      case NONE:
        return false;
      case ENTRY:
        return paths.text().equals(path);
      case GROUP:
        return paths.text().equals(path) || paths.group().equals(path);
      case PROJECT:
        return paths.text().equals(path)
            || paths.group().equals(path)
            || paths.project().equals(path)
            || paths.locales().equals(path)
            || paths.offlineCache().equals(path);
      default:
        return false;
    }
  }

  Optional<TranslaasCacheEntry> tryGet(
      String path, LinkedHashMap<String, String> mergedQuery, TranslaasRequestContext context) {
    if (!cachesPath(path) || bypassReadForValidation(context)) {
      return Optional.empty();
    }
    String key = cacheKey(path, mergedQuery);
    Optional<TranslaasCacheEntry> hit = provider.get(key);
    if (hit.isEmpty()) {
      return Optional.empty();
    }
    TranslaasCacheEntry e = hit.get();
    touchSlidingExpiration(key, e);
    applyCachedEtag(context, e);
    return Optional.of(e);
  }

  void putIfApplicable(
      String path,
      LinkedHashMap<String, String> mergedQuery,
      byte[] body,
      Optional<String> responseEtag) {
    if (!cachesPath(path)) {
      return;
    }
    String key = cacheKey(path, mergedQuery);
    Instant expiresAt = computeExpiresFromNow(Instant.now());
    String etag =
        options.isUseConditionalRequests() ? responseEtag.orElse(null) : null;
    provider.put(key, new TranslaasCacheEntry(body, etag, expiresAt));
  }

  Optional<TranslaasCacheEntry> getIgnoringValidationBypass(
      String path, LinkedHashMap<String, String> mergedQuery) {
    if (!cachesPath(path)) {
      return Optional.empty();
    }
    return provider.get(cacheKey(path, mergedQuery));
  }

  private void touchSlidingExpiration(String key, TranslaasCacheEntry entry) {
    if (options.getCacheAbsoluteExpiration().isPresent()) {
      return;
    }
    if (options.getCacheSlidingExpiration().isEmpty()) {
      return;
    }
    Instant now = Instant.now();
    Instant newExpiry = now.plus(options.getCacheSlidingExpiration().get());
    String etag =
        options.isUseConditionalRequests()
            ? entry.getEtag().orElse(null)
            : null;
    provider.put(key, new TranslaasCacheEntry(entry.getValue(), etag, newExpiry));
  }

  private static boolean bypassReadForValidation(TranslaasRequestContext context) {
    return context != null && context.getIfNoneMatch().isPresent();
  }

  private void applyCachedEtag(TranslaasRequestContext context, TranslaasCacheEntry entry) {
    if (context == null || !options.isUseConditionalRequests()) {
      return;
    }
    entry.getEtag().ifPresent(context::setResponseETag);
  }

  private Instant computeExpiresFromNow(Instant now) {
    if (options.getCacheAbsoluteExpiration().isPresent()) {
      return now.plus(options.getCacheAbsoluteExpiration().get());
    }
    if (options.getCacheSlidingExpiration().isPresent()) {
      return now.plus(options.getCacheSlidingExpiration().get());
    }
    return null;
  }

  static String cacheKey(String path, LinkedHashMap<String, String> mergedQuery) {
    if (path.endsWith("/text")) {
      return TranslationCacheKeys.forTextPath(mergedQuery);
    }
    if (path.endsWith("/group")) {
      return TranslationCacheKeys.forGroupPath(mergedQuery);
    }
    if (path.endsWith("/project")) {
      return TranslationCacheKeys.forProjectPath(mergedQuery);
    }
    if (path.endsWith("/locales")) {
      return TranslationCacheKeys.forLocalesPath(mergedQuery);
    }
    if (path.endsWith("/offline-cache")) {
      return TranslationCacheKeys.forOfflinePath(mergedQuery);
    }
    throw new IllegalArgumentException("Unsupported cache path: " + path);
  }

  static byte[] utf8Bytes(String s) {
    return s == null ? new byte[0] : s.getBytes(StandardCharsets.UTF_8);
  }

  static String utf8String(byte[] b) {
    return new String(b, StandardCharsets.UTF_8);
  }
}
