package io.mantelabs.translaas.caching.file;

import static org.assertj.core.api.Assertions.assertThat;

import io.mantelabs.translaas.caching.MemoryTranslaasCacheOptions;
import io.mantelabs.translaas.caching.MemoryTranslaasCacheProvider;
import io.mantelabs.translaas.caching.TranslaasCacheEntry;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HybridCacheProviderTest {

  @Test
  void l2Hit_promotesToL1_whenEnabled(@TempDir Path root) {
    FileCacheProvider disk = new FileCacheProvider(root);
    disk.put("k", new TranslaasCacheEntry("v".getBytes(StandardCharsets.UTF_8), null, null));

    MemoryTranslaasCacheProvider memory = new MemoryTranslaasCacheProvider();
    HybridCacheProvider hybrid =
        new HybridCacheProvider(
            memory, disk, HybridCacheOptions.builder().promoteL2HitsToL1(true).build());

    assertThat(memory.get("k")).isEmpty();
    assertThat(hybrid.get("k")).isPresent();
    assertThat(memory.get("k")).isPresent();
  }

  @Test
  void l2Hit_doesNotPromote_whenDisabled(@TempDir Path root) {
    FileCacheProvider disk = new FileCacheProvider(root);
    disk.put("k", new TranslaasCacheEntry("v".getBytes(StandardCharsets.UTF_8), null, null));
    MemoryTranslaasCacheProvider memory = new MemoryTranslaasCacheProvider();
    HybridCacheProvider hybrid =
        new HybridCacheProvider(
            memory, disk, HybridCacheOptions.builder().promoteL2HitsToL1(false).build());
    assertThat(hybrid.get("k")).isPresent();
    assertThat(memory.get("k")).isEmpty();
  }

  @Test
  void convenienceConstructor_buildsMemoryAndFile(@TempDir Path root) {
    HybridCacheProvider hybrid =
        new HybridCacheProvider(
            root,
            HybridCacheOptions.builder()
                .memory(MemoryTranslaasCacheOptions.lru(8))
                .promoteL2HitsToL1(true)
                .build());
    hybrid.put("k", new TranslaasCacheEntry(new byte[] {1}, null, null));
    assertThat(hybrid.get("k")).isPresent();
  }
}
