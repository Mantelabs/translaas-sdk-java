package io.mantelabs.translaas.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.mantelabs.translaas.models.ReportMissingKeyItemRequest;
import io.mantelabs.translaas.models.ReportMissingKeysRequest;
import io.mantelabs.translaas.models.exception.TranslaasApiException;
import io.mantelabs.translaas.models.json.TranslaasJson;
import java.util.List;
import org.junit.jupiter.api.Test;

@WireMockTest
class TranslaasClientReportMissingKeysTest {

  @Test
  void reportMissingKeys_completes_when202_andSendsJsonBody(WireMockRuntimeInfo wm)
      throws JsonProcessingException {
    ReportMissingKeysRequest req =
        new ReportMissingKeysRequest(
            List.of(new ReportMissingKeyItemRequest("common", "welcome", "en")));
    String json = TranslaasJson.mapper().writeValueAsString(req);

    wm.getWireMock()
        .register(
            post(urlPathEqualTo(TranslaasClient.TRANSLATIONS_REPORT_MISSING_PATH))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalTo(json))
                .willReturn(aResponse().withStatus(202)));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    client.reportMissingKeys(req).join();
  }

  @Test
  void reportMissingKeys_includesProjectChannelAndVersionFromOptions(WireMockRuntimeInfo wm)
      throws JsonProcessingException {
    ReportMissingKeysRequest req =
        new ReportMissingKeysRequest(
            List.of(new ReportMissingKeyItemRequest("g", "k", "en")));
    String json = TranslaasJson.mapper().writeValueAsString(req);

    wm.getWireMock()
        .register(
            post(urlPathEqualTo(TranslaasClient.TRANSLATIONS_REPORT_MISSING_PATH))
                .withQueryParam("project", equalTo("my-proj"))
                .withQueryParam("channel", equalTo("beta"))
                .withQueryParam("v", equalTo("3"))
                .withRequestBody(equalTo(json))
                .willReturn(aResponse().withStatus(202)));

    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.httpOrigin(wm.getHttpPort()))
            .defaultProject("my-proj")
            .channel("beta")
            .snapshotVersion("3")
            .build();
    TranslaasClient client = new TranslaasClient(options);

    client.reportMissingKeys(req).join();
  }

  @Test
  void reportMissingKeys_throwsTranslaasApiException_when400(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            post(urlPathEqualTo(TranslaasClient.TRANSLATIONS_REPORT_MISSING_PATH))
                .willReturn(aResponse().withStatus(400).withBody("invalid")));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    ReportMissingKeysRequest req =
        new ReportMissingKeysRequest(List.of(new ReportMissingKeyItemRequest("a", "b", "en")));

    assertThatThrownBy(() -> client.reportMissingKeys(req).join())
        .hasCauseInstanceOf(TranslaasApiException.class)
        .satisfies(
            t -> assertThat(((TranslaasApiException) t.getCause()).getHttpStatus()).isEqualTo(400));
  }

  @Test
  void reportMissingKeys_emptyKeys_skipsHttp(WireMockRuntimeInfo wm) {
    TranslaasClient client = clientForPort(wm.getHttpPort());
    client.reportMissingKeys(new ReportMissingKeysRequest(List.of())).join();
    verify(0, postRequestedFor(urlPathEqualTo(TranslaasClient.TRANSLATIONS_REPORT_MISSING_PATH)));
  }

  @Test
  void reportMissingKeys_throwsTranslaasApiException_when401(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            post(urlPathEqualTo(TranslaasClient.TRANSLATIONS_REPORT_MISSING_PATH))
                .willReturn(aResponse().withStatus(401).withBody("unauthorized")));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    ReportMissingKeysRequest req =
        new ReportMissingKeysRequest(List.of(new ReportMissingKeyItemRequest("g", "k", "en")));

    assertThatThrownBy(() -> client.reportMissingKeys(req).join())
        .hasCauseInstanceOf(TranslaasApiException.class)
        .satisfies(
            t -> assertThat(((TranslaasApiException) t.getCause()).getHttpStatus()).isEqualTo(401));
  }

  @Test
  void reportMissingKeys_throwsTranslaasApiException_when404(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            post(urlPathEqualTo(TranslaasClient.TRANSLATIONS_REPORT_MISSING_PATH))
                .willReturn(aResponse().withStatus(404).withBody("missing")));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    ReportMissingKeysRequest req =
        new ReportMissingKeysRequest(List.of(new ReportMissingKeyItemRequest("x", "y", "en")));

    assertThatThrownBy(() -> client.reportMissingKeys(req).join())
        .hasCauseInstanceOf(TranslaasApiException.class)
        .satisfies(
            t -> assertThat(((TranslaasApiException) t.getCause()).getHttpStatus()).isEqualTo(404));
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
