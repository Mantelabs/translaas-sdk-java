package io.mantelabs.translaas.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.mantelabs.translaas.models.exception.TranslaasApiException;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

@WireMockTest
class TranslaasClientGetEntryTest {

  @Test
  void getEntry_returnsPlainText_when200(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH))
                .withQueryParam("group", equalTo("common"))
                .withQueryParam("lang", equalTo("en"))
                .withQueryParam("entry", equalTo("welcome"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain; charset=utf-8")
                        .withBody("Hello")));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    String text = client.getEntry("common", "welcome", "en").join();

    assertThat(text).isEqualTo("Hello");
  }

  @Test
  void getEntry_includesInterpolationParametersAsQueryKeys(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH))
                .withQueryParam("group", equalTo("g"))
                .withQueryParam("lang", equalTo("en"))
                .withQueryParam("entry", equalTo("greet"))
                .withQueryParam("n", equalTo("2"))
                .withQueryParam("userName", equalTo("Ada"))
                .willReturn(aResponse().withStatus(200).withBody("Hi Ada")));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    String text =
        client
            .getEntry(
                "g",
                "greet",
                "en",
                new BigDecimal("2"),
                Map.of("userName", "Ada"),
                null)
            .join();

    assertThat(text).isEqualTo("Hi Ada");
  }

  @Test
  void getEntryUsingShorthand_usesEntryKeyAsQueryName(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH))
                .withQueryParam("group", equalTo("ui"))
                .withQueryParam("lang", equalTo("en"))
                .withQueryParam("button.save", equalTo(""))
                .withQueryParam("icon", equalTo("ok"))
                .willReturn(aResponse().withStatus(200).withBody("Save")));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    String text =
        client
            .getEntryUsingShorthand(
                "ui", "button.save", "en", null, Map.of("icon", "ok"), null, null)
            .join();

    assertThat(text).isEqualTo("Save");
  }

  @Test
  void getEntry_throwsTranslaasApiException_when404(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH))
                .willReturn(aResponse().withStatus(404).withBody("not found")));

    TranslaasClient client = clientForPort(wm.getHttpPort());

    assertThatThrownBy(() -> client.getEntry("a", "b", "en").join())
        .hasCauseInstanceOf(TranslaasApiException.class)
        .satisfies(
            t -> assertThat(((TranslaasApiException) t.getCause()).getHttpStatus()).isEqualTo(404));
  }

  @Test
  void getEntry_setsNotModifiedAndReturnsEmptyString_when304(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH))
                .withHeader("If-None-Match", equalTo("\"etag-1\""))
                .willReturn(aResponse().withStatus(304)));

    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.httpOrigin(wm.getHttpPort()))
            .useConditionalRequests(true)
            .build();
    TranslaasClient client = new TranslaasClient(options);

    TranslaasRequestContext ctx = new TranslaasRequestContext();
    ctx.setIfNoneMatch("\"etag-1\"");

    String text = client.getEntry("g", "e", "en", ctx).join();

    assertThat(text).isEmpty();
    assertThat(ctx.isNotModified()).isTrue();
  }

  private static TranslaasClient clientForPort(int port) {
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("test-key")
            .baseUrl(TestApiUrls.httpOrigin(port))
            .build();
    return new TranslaasClient(options);
  }
}
