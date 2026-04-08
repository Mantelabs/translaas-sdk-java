package io.mantelabs.translaas.caching;

import java.util.OptionalInt;

/** Options for {@link MemoryTranslaasCacheProvider} (LRU size cap). */
public final class MemoryTranslaasCacheOptions {

  private final OptionalInt maxEntries;

  private MemoryTranslaasCacheOptions(OptionalInt maxEntries) {
    this.maxEntries = maxEntries;
  }

  public static MemoryTranslaasCacheOptions defaults() {
    return new MemoryTranslaasCacheOptions(OptionalInt.empty());
  }

  public static MemoryTranslaasCacheOptions lru(int maxEntries) {
    return new MemoryTranslaasCacheOptions(OptionalInt.of(maxEntries));
  }

  /** When empty, the memory cache is unbounded (aside from expiry checks on read). */
  public OptionalInt getMaxEntries() {
    return maxEntries;
  }
}
