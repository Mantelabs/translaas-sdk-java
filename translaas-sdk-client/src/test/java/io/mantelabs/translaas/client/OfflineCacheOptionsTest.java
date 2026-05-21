package io.mantelabs.translaas.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class OfflineCacheOptionsTest {

  @Test
  void builder_defaults() {
    OfflineCacheOptions options =
        OfflineCacheOptions.builder()
            .enabled(true)
            .projects(List.of("demo"))
            .languages(List.of("en"))
            .defaultProjectId("demo")
            .autoSyncInterval(Duration.ofMinutes(5))
            .build();
    assertThat(options.isEnabled()).isTrue();
    assertThat(options.getCacheDirectory()).isEqualTo(OfflineCacheOptions.DEFAULT_CACHE_DIRECTORY);
    assertThat(options.getFallbackMode()).isEqualTo(OfflineFallbackMode.CACHE_FIRST);
    assertThat(options.getProjects()).containsExactly("demo");
    assertThat(options.getHybridCache().isEnabled()).isTrue();
  }
}
