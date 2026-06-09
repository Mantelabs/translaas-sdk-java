package io.translaas.client;

import static org.assertj.core.api.Assertions.assertThat;

import io.translaas.client.http.TranslaasHttp;
import java.net.http.HttpClient;
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
    assertThat(new TranslaasHttp(options)).isNotNull();
  }

  @Test
  void preferHttp11_defaultsToFalse_andUsesDefaultHttpVersion() {
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.ORIGIN)
            .build();

    assertThat(options.isPreferHttp11()).isFalse();
    assertThat(JdkHttpClientFactory.create(options).version()).isEqualTo(HttpClient.Version.HTTP_2);
  }

  @Test
  void preferHttp11_whenTrue_pinsClientToHttp11() {
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.ORIGIN)
            .preferHttp11(true)
            .build();

    assertThat(options.isPreferHttp11()).isTrue();
    assertThat(JdkHttpClientFactory.create(options).version()).isEqualTo(HttpClient.Version.HTTP_1_1);
  }
}
