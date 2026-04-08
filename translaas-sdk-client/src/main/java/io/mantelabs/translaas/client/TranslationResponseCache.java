package io.mantelabs.translaas.client;

import io.mantelabs.translaas.caching.MemoryTranslaasCacheProvider;
import io.mantelabs.translaas.caching.TranslaasCacheEntry;
import io.mantelabs.translaas.caching.TranslaasCacheProvider;
import io.mantelabs.translaas.client.http.TranslaasUris;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * L1 response cache for {@link TranslaasClient}: keys match merged query parameters (project,
 * group, entry, lang, channel, version, includeContext, etc.) plus API path.
 *
 * <p><strong>304 Not Modified:</strong> when the transport returns {@code 304}, the client uses a
 * cached body for the same logical key if present; otherwise it keeps the existing empty / {@code
 * null} result semantics. Cached entries store the {@code ETag} from prior {@code 200} responses
 * when {@link TranslaasOptions#isUseConditionalRequests()} is enabled.
 *
 * <p><strong>Conditional requests:</strong> if {@link TranslaasRequestContext#getIfNoneMatch()} is
 * set, cache reads are skipped so the server can validate or return {@code 304}; a cached body may
 * still be used to satisfy a {@code 304} response as above.
 */
final class TranslationResponseCache {

  private final TranslaasOptions options;
  private final TranslaasCacheProvider provider;

  private TranslationResponseCache(TranslaasOptions options, TranslaasCacheProvider provider) {
    this.options = Objects.requireNonNull(options, "options");
    this.provider = Objects.requireNonNull(provider, "provider");
  }

  static TranslationResponseCache maybeCreate(TranslaasOptions options) {
    Objects.requireNonNull(options, "options");
    if (options.getCacheMode() == CacheMode.NONE) {
      return null;
    }
    TranslaasCacheProvider p =
        options.getCacheProvider().orElseGet(MemoryTranslaasCacheProvider::new);
    return new TranslationResponseCache(options, p);
  }

  boolean cachesPath(String path) {
    switch (options.getCacheMode()) {
      case NONE:
        return false;
      case ENTRY:
        return TranslaasClient.TRANSLATIONS_TEXT_PATH.equals(path);
      case GROUP:
        return TranslaasClient.TRANSLATIONS_TEXT_PATH.equals(path)
            || TranslaasClient.TRANSLATIONS_GROUP_PATH.equals(path);
      case PROJECT:
        return TranslaasClient.TRANSLATIONS_TEXT_PATH.equals(path)
            || TranslaasClient.TRANSLATIONS_GROUP_PATH.equals(path)
            || TranslaasClient.TRANSLATIONS_PROJECT_PATH.equals(path)
            || TranslaasClient.TRANSLATIONS_LOCALES_PATH.equals(path)
            || TranslaasClient.TRANSLATIONS_OFFLINE_CACHE_PATH.equals(path);
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
    String q = TranslaasUris.buildQueryString(mergedQuery);
    return path + "\u0000" + q;
  }

  static byte[] utf8Bytes(String s) {
    return s == null ? new byte[0] : s.getBytes(StandardCharsets.UTF_8);
  }

  static String utf8String(byte[] b) {
    return new String(b, StandardCharsets.UTF_8);
  }
}
