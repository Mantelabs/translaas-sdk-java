package io.mantelabs.translaas.caching;

import java.util.Optional;

/**
 * Pluggable translation response cache (memory, file, hybrid, etc.).
 *
 * <p>Implementations must be safe for concurrent use from multiple threads unless explicitly
 * documented otherwise. Keys are opaque UTF-8 strings built by the client layer (project, group,
 * entry, locale, channel, version, flags, etc.).
 */
public interface TranslaasCacheProvider {

  Optional<TranslaasCacheEntry> get(String key);

  void put(String key, TranslaasCacheEntry entry);

  void remove(String key);

  void clear();
}
