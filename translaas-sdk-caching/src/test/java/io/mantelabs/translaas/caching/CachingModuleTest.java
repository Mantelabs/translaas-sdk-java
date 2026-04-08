package io.mantelabs.translaas.caching;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class CachingModuleTest {

  @Test
  void memory_putGet_roundTrip() {
    MemoryTranslaasCacheProvider cache = new MemoryTranslaasCacheProvider();
    TranslaasCacheEntry in =
        new TranslaasCacheEntry("hello".getBytes(StandardCharsets.UTF_8), "\"t1\"", null);
    cache.put("k", in);
    assertThat(cache.get("k"))
        .get()
        .satisfies(
            e -> {
              assertThat(e.getValue()).asString(StandardCharsets.UTF_8).isEqualTo("hello");
              assertThat(e.getEtag()).contains("\"t1\"");
            });
  }

  @Test
  void memory_expiredEntry_removedOnRead() {
    MemoryTranslaasCacheProvider cache = new MemoryTranslaasCacheProvider();
    Instant past = Instant.now().minus(1, ChronoUnit.HOURS);
    cache.put("k", new TranslaasCacheEntry(new byte[] {1}, null, past));
    assertThat(cache.get("k")).isEmpty();
  }

  @Test
  void memory_lru_evictsOldestWhenFull() {
    MemoryTranslaasCacheProvider cache =
        new MemoryTranslaasCacheProvider(MemoryTranslaasCacheOptions.lru(1));
    cache.put("a", new TranslaasCacheEntry(new byte[] {1}, null, null));
    cache.put("b", new TranslaasCacheEntry(new byte[] {2}, null, null));
    assertThat(cache.get("a")).isEmpty();
    assertThat(cache.get("b")).isPresent();
  }
}
