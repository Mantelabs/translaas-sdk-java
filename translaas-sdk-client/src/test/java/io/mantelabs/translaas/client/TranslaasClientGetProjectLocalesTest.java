package io.mantelabs.translaas.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.mantelabs.translaas.models.ProjectLocalesResponse;
import java.net.URI;
import java.time.Instant;
import org.junit.jupiter.api.Test;

@WireMockTest
class TranslaasClientGetProjectLocalesTest {

  @Test
  void getProjectLocales_returnsParsedJson_when200(WireMockRuntimeInfo wm) {
    String json =
        "{\"project\":\"demo\",\"locales\":[\"en\",\"es-MX\"],"
            + "\"lastModifiedUtc\":\"2026-04-07T12:00:00Z\"}";
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_LOCALES_PATH))
                .withQueryParam("project", equalTo("demo"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(json)));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    ProjectLocalesResponse r = client.getProjectLocales("demo").join();

    assertThat(r.getProject()).isEqualTo("demo");
    assertThat(r.getLocales()).containsExactly("en", "es-MX");
    assertThat(r.getLastModifiedUtc()).isEqualTo(Instant.parse("2026-04-07T12:00:00Z"));
  }

  @Test
  void getProjectLocales_includesChannelAndVersionFromOptions(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_LOCALES_PATH))
                .withQueryParam("project", equalTo("p1"))
                .withQueryParam("channel", equalTo("beta"))
                .withQueryParam("v", equalTo("3"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"project\":\"p1\",\"locales\":[\"en\"]}")));

    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(URI.create("http://localhost:" + wm.getHttpPort()))
            .channel("beta")
            .snapshotVersion("3")
            .build();
    TranslaasClient client = new TranslaasClient(options);

    ProjectLocalesResponse r = client.getProjectLocales("p1").join();

    assertThat(r.getLocales()).containsExactly("en");
  }

  @Test
  void getProjectLocales_setsNotModifiedAndReturnsNull_when304(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_LOCALES_PATH))
                .withQueryParam("project", equalTo("demo"))
                .withHeader("If-None-Match", equalTo("\"etag-locales\""))
                .willReturn(aResponse().withStatus(304)));

    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(URI.create("http://localhost:" + wm.getHttpPort()))
            .useConditionalRequests(true)
            .build();
    TranslaasClient client = new TranslaasClient(options);

    TranslaasRequestContext ctx = new TranslaasRequestContext();
    ctx.setIfNoneMatch("\"etag-locales\"");

    ProjectLocalesResponse r = client.getProjectLocales("demo", ctx).join();

    assertThat(r).isNull();
    assertThat(ctx.isNotModified()).isTrue();
  }

  @Test
  void getProjectLocales_throwsIllegalArgumentException_whenProjectBlank() {
    TranslaasClient client =
        new TranslaasClient(
            TranslaasOptions.builder()
                .apiKey("k")
                .baseUrl(URI.create("http://localhost:9"))
                .build());

    assertThatThrownBy(() -> client.getProjectLocales("  ").join())
        .hasCauseInstanceOf(IllegalArgumentException.class);
  }

  private static TranslaasClient clientForPort(int port) {
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("test-key")
            .baseUrl(URI.create("http://localhost:" + port))
            .build();
    return new TranslaasClient(options);
  }
}
