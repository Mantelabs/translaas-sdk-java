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
            .baseUrl(TestApiUrls.ORIGIN)
            .build();
    assertThat(options.getBaseUrl().getHost()).isEqualTo(TestApiUrls.HOST);
    assertThat(options.isPreferHttp11()).isFalse();
    assertThat(new TranslaasHttp(options)).isNotNull();
  }

  @Test
  void translaasOptions_preferHttp11() {
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.ORIGIN)
            .preferHttp11(true)
            .build();
    assertThat(options.isPreferHttp11()).isTrue();
    assertThat(new TranslaasHttp(options)).isNotNull();
  }
}
