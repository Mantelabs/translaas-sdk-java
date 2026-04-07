package io.mantelabs.translaas.client;

import static org.assertj.core.api.Assertions.assertThat;

import io.mantelabs.translaas.client.http.TranslaasHttp;
import org.junit.jupiter.api.Test;

class ClientModuleTest {

  @Test
  void translaasOptions_andHttpType_load() {
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl("https://api.mantelabs.io")
            .build();
    assertThat(options.getBaseUrl().getHost()).isEqualTo("api.mantelabs.io");
    assertThat(new TranslaasHttp(options)).isNotNull();
  }
}
