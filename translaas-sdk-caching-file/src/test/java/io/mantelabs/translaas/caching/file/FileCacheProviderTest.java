package io.mantelabs.translaas.caching.file;

import static org.assertj.core.api.Assertions.assertThat;

import io.mantelabs.translaas.caching.TranslaasCacheEntry;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileCacheProviderTest {

  @Test
  void writesUnderVersionedLayout(@TempDir Path root) throws Exception {
    FileCacheProvider cache = new FileCacheProvider(root);
    String key = "project:demo|group:g|entry:e|lang:en";
    cache.put(
        key,
        new TranslaasCacheEntry("payload".getBytes(StandardCharsets.UTF_8), "W/\"abc\"", null));
    String digest = FileCacheProvider.sha256Hex(key);
    Path data = root.resolve("v1").resolve(digest);
    Path meta = root.resolve("v1").resolve(digest + ".meta");
    assertThat(data).exists();
    assertThat(meta).exists();
    assertThat(Files.readAllBytes(data)).asString(StandardCharsets.UTF_8).isEqualTo("payload");
  }

  @Test
  void get_returnsEmptyWhenExpired(@TempDir Path root) {
    FileCacheProvider cache = new FileCacheProvider(root);
    Instant past = Instant.now().minus(1, ChronoUnit.HOURS);
    cache.put("k", new TranslaasCacheEntry(new byte[] {9}, null, past));
    assertThat(cache.get("k")).isEmpty();
  }

  @Test
  void clear_removesVersionDirectoryEntries(@TempDir Path root) throws Exception {
    FileCacheProvider cache = new FileCacheProvider(root);
    cache.put("x", new TranslaasCacheEntry(new byte[] {1}, null, null));
    try (Stream<Path> before = Files.list(root.resolve("v1"))) {
      assertThat(before).hasSize(2);
    }
    cache.clear();
    try (Stream<Path> after = Files.list(root.resolve("v1"))) {
      assertThat(after).isEmpty();
    }
  }
}
