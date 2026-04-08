package io.mantelabs.translaas.caching.file;

import static org.assertj.core.api.Assertions.assertThat;

import io.mantelabs.translaas.client.TranslaasOptions;
import java.net.URI;
import org.junit.jupiter.api.Test;

class CachingFileModuleTest {

  @Test
  void skipApiKeyValidation_reflectsOptions() {
    TranslaasOptions opts =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(URI.create("https://api.example.test"))
            .skipApiValidation(true)
            .build();
    assertThat(TranslaasCachingFile.skipApiKeyValidation(opts)).isTrue();
  }
}
