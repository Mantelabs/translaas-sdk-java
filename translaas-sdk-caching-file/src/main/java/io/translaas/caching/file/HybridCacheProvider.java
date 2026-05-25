package io.translaas.caching.file;

import io.translaas.caching.MemoryTranslaasCacheProvider;
import io.translaas.caching.TranslaasCacheEntry;
import io.translaas.caching.TranslaasCacheProvider;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Two-tier cache: L1 {@link MemoryTranslaasCacheProvider} and L2 {@link FileCacheProvider}. On a
 * miss in L1, L2 is consulted; when {@linkplain HybridCacheOptions#isPromoteL2HitsToL1() promotion}
 * is enabled, an L2 hit is copied into L1.
 */
public final class HybridCacheProvider implements TranslaasCacheProvider {

  private final TranslaasCacheProvider l1;
  private final TranslaasCacheProvider l2;
  private final boolean promoteL2HitsToL1;

  public HybridCacheProvider(Path fileRoot, HybridCacheOptions options) {
    this(
        new MemoryTranslaasCacheProvider(options.getMemory()),
        new FileCacheProvider(fileRoot),
        options.isPromoteL2HitsToL1());
  }

  public HybridCacheProvider(
      TranslaasCacheProvider memoryLayer,
      TranslaasCacheProvider fileLayer,
      HybridCacheOptions options) {
    this(memoryLayer, fileLayer, options.isPromoteL2HitsToL1());
  }

  public HybridCacheProvider(
      TranslaasCacheProvider l1,
      TranslaasCacheProvider l2,
      boolean promoteL2HitsToL1) {
    this.l1 = Objects.requireNonNull(l1, "l1");
    this.l2 = Objects.requireNonNull(l2, "l2");
    this.promoteL2HitsToL1 = promoteL2HitsToL1;
  }

  @Override
  public Optional<TranslaasCacheEntry> get(String key) {
    Objects.requireNonNull(key, "key");
    Optional<TranslaasCacheEntry> fromMemory = l1.get(key);
    if (fromMemory.isPresent()) {
      return fromMemory;
    }
    Optional<TranslaasCacheEntry> fromDisk = l2.get(key);
    if (fromDisk.isEmpty()) {
      return Optional.empty();
    }
    if (promoteL2HitsToL1) {
      l1.put(key, fromDisk.get());
    }
    return fromDisk;
  }

  @Override
  public void put(String key, TranslaasCacheEntry entry) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(entry, "entry");
    l1.put(key, entry);
    l2.put(key, entry);
  }

  @Override
  public void remove(String key) {
    Objects.requireNonNull(key, "key");
    l1.remove(key);
    l2.remove(key);
  }

  @Override
  public void clear() {
    l1.clear();
    l2.clear();
  }

  /** Exposes the L1 provider (typically memory) for tests or advanced tuning. */
  public TranslaasCacheProvider getMemoryLayer() {
    return l1;
  }

  /** Exposes the L2 provider (typically file) for tests or advanced tuning. */
  public TranslaasCacheProvider getFileLayer() {
    return l2;
  }
}
