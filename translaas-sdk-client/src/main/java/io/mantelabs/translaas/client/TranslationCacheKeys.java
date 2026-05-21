package io.mantelabs.translaas.client;

import io.mantelabs.translaas.caching.CacheKeyBuilder;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Maps merged HTTP query maps to {@link CacheKeyBuilder} cache keys. */
final class TranslationCacheKeys {

  private TranslationCacheKeys() {}

  static String forTextPath(LinkedHashMap<String, String> merged) {
    String group = merged.get("group");
    String lang = merged.get("lang");
    String entry = resolveEntryKey(merged);
    BigDecimal n = parseDecimal(merged.get("n"));
    Map<String, String> interpolation = interpolationParams(merged);
    return CacheKeyBuilder.buildEntryKey(
        group,
        entry,
        lang,
        n,
        interpolation,
        merged.get("project"),
        merged.get("channel"),
        merged.get("v"));
  }

  static String forGroupPath(LinkedHashMap<String, String> merged) {
    return CacheKeyBuilder.buildGroupKey(
        merged.get("project"),
        merged.get("group"),
        merged.get("lang"),
        merged.get("format"),
        merged.get("channel"),
        merged.get("v"),
        parseIncludeContext(merged.get("includeContext")));
  }

  static String forProjectPath(LinkedHashMap<String, String> merged) {
    return CacheKeyBuilder.buildProjectKey(
        merged.get("project"),
        merged.get("lang"),
        merged.get("format"),
        merged.get("channel"),
        merged.get("v"),
        parseIncludeContext(merged.get("includeContext")));
  }

  static String forLocalesPath(LinkedHashMap<String, String> merged) {
    return CacheKeyBuilder.buildLocalesKey(
        merged.get("project"), merged.get("channel"), merged.get("v"));
  }

  static String forOfflinePath(LinkedHashMap<String, String> merged) {
    return CacheKeyBuilder.buildOfflineCacheKey(
        merged.get("project"),
        merged.get("channel"),
        merged.get("v"),
        parseIncludeContext(merged.get("includeContext")));
  }

  static String resolveEntryKey(Map<String, String> merged) {
    String explicit = merged.get("entry");
    if (explicit != null && !explicit.isBlank()) {
      return explicit;
    }
    for (Map.Entry<String, String> e : merged.entrySet()) {
      if (TranslationTextQueries.RESERVED_QUERY_KEYS.contains(
          e.getKey().toLowerCase(Locale.ROOT))) {
        continue;
      }
      return e.getKey();
    }
    return "";
  }

  private static Map<String, String> interpolationParams(Map<String, String> merged) {
    String resolvedEntry = resolveEntryKey(merged);
    LinkedHashMap<String, String> out = new LinkedHashMap<>();
    for (Map.Entry<String, String> e : merged.entrySet()) {
      if (TranslationTextQueries.RESERVED_QUERY_KEYS.contains(
          e.getKey().toLowerCase(Locale.ROOT))) {
        continue;
      }
      if ("entry".equalsIgnoreCase(e.getKey())) {
        continue;
      }
      if (resolvedEntry != null
          && resolvedEntry.equals(e.getKey())
          && (e.getValue() == null || e.getValue().isBlank())) {
        continue;
      }
      if (e.getValue() != null) {
        out.put(e.getKey(), e.getValue());
      }
    }
    return out.isEmpty() ? null : out;
  }

  private static BigDecimal parseDecimal(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return new BigDecimal(raw);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static Boolean parseIncludeContext(String raw) {
    if (raw == null) {
      return null;
    }
    return Boolean.parseBoolean(raw);
  }
}
