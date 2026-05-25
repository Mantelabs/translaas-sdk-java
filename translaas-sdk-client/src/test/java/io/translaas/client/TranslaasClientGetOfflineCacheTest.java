package io.translaas.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.translaas.models.OfflineCacheDownloadResult;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

@WireMockTest
class TranslaasClientGetOfflineCacheTest {

  @Test
  void getOfflineCache_returnsZipBytesAndFilename_when200(WireMockRuntimeInfo wm) {
    byte[] payload = new byte[] {0x50, 0x4b, 0x03, 0x04, 0x0a};
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_OFFLINE_CACHE_PATH))
                .withQueryParam("project", equalTo("demo"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/zip")
                        .withHeader(
                            "Content-Disposition", "attachment; filename=\"translations-offline.zip\"")
                        .withBody(payload)));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    OfflineCacheDownloadResult r = client.getOfflineCache("demo").join();

    assertThat(r.getZipBytes()).isEqualTo(payload);
    assertThat(r.getContentDispositionFilename()).hasValue("translations-offline.zip");
  }

  @Test
  void getOfflineCache_setsNotModifiedAndReturnsEmptyResult_when304(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_OFFLINE_CACHE_PATH))
                .withQueryParam("project", equalTo("demo"))
                .withHeader("If-None-Match", equalTo("\"etag-cache\""))
                .willReturn(aResponse().withStatus(304)));

    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.httpOrigin(wm.getHttpPort()))
            .useConditionalRequests(true)
            .build();
    TranslaasClient client = new TranslaasClient(options);

    TranslaasRequestContext ctx = new TranslaasRequestContext();
    ctx.setIfNoneMatch("\"etag-cache\"");

    OfflineCacheDownloadResult r = client.getOfflineCache("demo", ctx).join();

    assertThat(r).isNotNull();
    assertThat(r.isNotModified()).isTrue();
    assertThat(r.getZipBytes()).isEmpty();
    assertThat(ctx.isNotModified()).isTrue();
  }

  @Test
  void getOfflineCache_includesChannelVersionAndIncludeContextFromOptionsAndContext(
      WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_OFFLINE_CACHE_PATH))
                .withQueryParam("project", equalTo("p1"))
                .withQueryParam("channel", equalTo("beta"))
                .withQueryParam("v", equalTo("7"))
                .withQueryParam("includeContext", equalTo("true"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/zip")
                        .withBody("ZIP".getBytes(StandardCharsets.UTF_8))));

    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.httpOrigin(wm.getHttpPort()))
            .channel("beta")
            .snapshotVersion("7")
            .includeContextDefault(true)
            .build();
    TranslaasClient client = new TranslaasClient(options);
    TranslaasRequestContext ctx = new TranslaasRequestContext();
    ctx.setIncludeContext(true);

    OfflineCacheDownloadResult r = client.getOfflineCache("p1", ctx).join();

    assertThat(r.getZipBytes()).asString(StandardCharsets.UTF_8).isEqualTo("ZIP");
  }

  @Test
  void getOfflineCache_throwsIllegalArgumentException_whenProjectBlank() {
    TranslaasClient client =
        new TranslaasClient(
            TranslaasOptions.builder()
                .apiKey("k")
                .baseUrl(TestApiUrls.httpOrigin(9))
                .build());

    assertThatThrownBy(() -> client.getOfflineCache("  ").join())
        .hasCauseInstanceOf(IllegalArgumentException.class);
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
