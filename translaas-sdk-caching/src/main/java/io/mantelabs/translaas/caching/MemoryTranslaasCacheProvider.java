package io.mantelabs.translaas.caching;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-process cache backed by either a {@link ConcurrentHashMap} or a size-bounded LRU {@link
 * LinkedHashMap} (access-order), depending on {@link MemoryTranslaasCacheOptions}.
 */
public final class MemoryTranslaasCacheProvider implements TranslaasCacheProvider {

  private final Map<String, TranslaasCacheEntry> store;
  private final Object mutex;

  public MemoryTranslaasCacheProvider() {
    this(MemoryTranslaasCacheOptions.defaults());
  }

  public MemoryTranslaasCacheProvider(MemoryTranslaasCacheOptions options) {
    Objects.requireNonNull(options, "options");
    if (options.getMaxEntries().isEmpty()) {
      this.store = new ConcurrentHashMap<>();
      this.mutex = null;
    } else {
      int max = options.getMaxEntries().getAsInt();
      if (max < 1) {
        throw new IllegalArgumentException("maxEntries must be >= 1 when set");
      }
      this.store =
          new LinkedHashMap<String, TranslaasCacheEntry>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, TranslaasCacheEntry> eldest) {
              return size() > max;
            }
          };
      this.mutex = new Object();
    }
  }

  @Override
  public Optional<TranslaasCacheEntry> get(String key) {
    Objects.requireNonNull(key, "key");
    Instant now = Instant.now();
    if (mutex == null) {
      TranslaasCacheEntry e = store.get(key);
      if (e == null) {
        return Optional.empty();
      }
      if (e.isExpiredAt(now)) {
        store.remove(key, e);
        return Optional.empty();
      }
      return Optional.of(e);
    }
    synchronized (mutex) {
      TranslaasCacheEntry e = store.get(key);
      if (e == null) {
        return Optional.empty();
      }
      if (e.isExpiredAt(now)) {
        store.remove(key);
        return Optional.empty();
      }
      return Optional.of(e);
    }
  }

  @Override
  public void put(String key, TranslaasCacheEntry entry) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(entry, "entry");
    if (mutex == null) {
      store.put(key, entry);
    } else {
      synchronized (mutex) {
        store.put(key, entry);
      }
    }
  }

  @Override
  public void remove(String key) {
    Objects.requireNonNull(key, "key");
    if (mutex == null) {
      store.remove(key);
    } else {
      synchronized (mutex) {
        store.remove(key);
      }
    }
  }

  @Override
  public void clear() {
    if (mutex == null) {
      store.clear();
    } else {
      synchronized (mutex) {
        store.clear();
      }
    }
  }
}
