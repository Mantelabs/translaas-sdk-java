package io.translaas.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.translaas.models.OfflineCacheDownloadResult;
import io.translaas.models.ReportMissingKeyItemRequest;
import io.translaas.models.ReportMissingKeysRequest;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

@WireMockTest
class TranslaasClientStatusSemanticsTest {

  @Test
  void getEntry_204_returnsEntryKey(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH))
                .withQueryParam("group", equalTo("g"))
                .withQueryParam("entry", equalTo("missing"))
                .withQueryParam("lang", equalTo("en"))
                .willReturn(aResponse().withStatus(204)));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    assertThat(client.getEntry("g", "missing", "en").join()).isEqualTo("missing");
  }

  @Test
  void getGroupTranslations_204_returnsEmptyEntries(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_GROUP_PATH))
                .willReturn(aResponse().withStatus(204)));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    var r = client.getGroupTranslations("p", "g", "en").join();
    assertThat(r.getEntries()).isEmpty();
  }

  @Test
  void reportMissingKeys_emptyList_skipsHttp(WireMockRuntimeInfo wm) {
    TranslaasClient client = clientForPort(wm.getHttpPort());
    client.reportMissingKeys(new ReportMissingKeysRequest(List.of())).join();
    verify(0, postRequestedFor(urlPathEqualTo(TranslaasClient.TRANSLATIONS_REPORT_MISSING_PATH)));
  }

  @Test
  void getEntry_injectsN_whenPluralSet(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH))
                .withQueryParam("group", equalTo("g"))
                .withQueryParam("entry", equalTo("e"))
                .withQueryParam("lang", equalTo("en"))
                .withQueryParam("n", equalTo("2"))
                .withQueryParam("N", equalTo("2"))
                .willReturn(aResponse().withStatus(200).withBody("ok")));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    client.getEntry("g", "e", "en", new BigDecimal("2"), null, null).join();
  }

  @Test
  void getOfflineCache_304_returnsNotModifiedResult(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_OFFLINE_CACHE_PATH))
                .willReturn(aResponse().withStatus(304)));

    TranslaasRequestContext ctx = new TranslaasRequestContext();
    OfflineCacheDownloadResult r =
        clientForPort(wm.getHttpPort()).getOfflineCache("p", ctx).join();
    assertThat(r.isNotModified()).isTrue();
    assertThat(ctx.isNotModified()).isTrue();
  }

  private static TranslaasClient clientForPort(int port) {
    return new TranslaasClient(
        TranslaasOptions.builder()
            .apiKey("test-key")
            .baseUrl(URI.create("http://127.0.0.1:" + port))
            .build());
  }
}
