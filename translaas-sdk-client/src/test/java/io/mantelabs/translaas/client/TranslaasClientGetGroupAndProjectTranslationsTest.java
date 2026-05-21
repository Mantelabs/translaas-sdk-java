package io.mantelabs.translaas.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.mantelabs.translaas.models.GroupTranslationsResponse;
import io.mantelabs.translaas.models.ProjectTranslationsResponse;
import java.time.Instant;
import org.junit.jupiter.api.Test;

@WireMockTest
class TranslaasClientGetGroupAndProjectTranslationsTest {

  @Test
  void getGroupTranslations_returnsParsedJson_when200(WireMockRuntimeInfo wm) {
    String json =
        "{\"project\":\"demo\",\"lang\":\"en\",\"version\":1,"
            + "\"generatedAt\":\"2026-04-07T12:00:00Z\","
            + "\"entries\":{\"a\":\"1\"}}";
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_GROUP_PATH))
                .withQueryParam("project", equalTo("demo"))
                .withQueryParam("group", equalTo("common"))
                .withQueryParam("lang", equalTo("en"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(json)));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    GroupTranslationsResponse r = client.getGroupTranslations("demo", "common", "en").join();

    assertThat(r.getProject()).isEqualTo("demo");
    assertThat(r.getEntries().get("a").asText()).isEqualTo("1");
    assertThat(r.getGeneratedAt()).isEqualTo(Instant.parse("2026-04-07T12:00:00Z"));
  }

  @Test
  void getGroupTranslations_sendsFormatFlatJson_whenRequested(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_GROUP_PATH))
                .withQueryParam("project", equalTo("p"))
                .withQueryParam("group", equalTo("g"))
                .withQueryParam("lang", equalTo("en"))
                .withQueryParam("format", equalTo(TranslaasClient.FORMAT_FLAT_JSON))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"project\":\"p\",\"lang\":\"en\",\"version\":0,"
                            + "\"generatedAt\":\"2026-04-07T12:00:00Z\",\"entries\":{}}")));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    GroupTranslationsResponse r =
        client
            .getGroupTranslations(
                "p", "g", "en", TranslaasClient.FORMAT_FLAT_JSON, null, null)
            .join();

    assertThat(r.getVersion()).isZero();
  }

  @Test
  void getGroupTranslations_includesIncludeContextFromOptions(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_GROUP_PATH))
                .withQueryParam("project", equalTo("p"))
                .withQueryParam("group", equalTo("g"))
                .withQueryParam("lang", equalTo("en"))
                .withQueryParam("includeContext", equalTo("true"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"project\":\"p\",\"lang\":\"en\",\"version\":0,"
                            + "\"generatedAt\":\"2026-04-07T12:00:00Z\",\"entries\":{}}")));

    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.httpOrigin(wm.getHttpPort()))
            .includeContextDefault(true)
            .build();
    TranslaasClient client = new TranslaasClient(options);

    assertThat(client.getGroupTranslations("p", "g", "en").join()).isNotNull();
  }

  @Test
  void getProjectTranslations_returnsNestedGroups_when200(WireMockRuntimeInfo wm) {
    String json =
        "{\"project\":\"demo\",\"lang\":\"en\",\"version\":2,"
            + "\"generatedAt\":\"2026-04-07T12:00:00Z\","
            + "\"groups\":{\"common\":{\"entries\":{\"k\":\"v\"}}}}";
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_PROJECT_PATH))
                .withQueryParam("project", equalTo("demo"))
                .withQueryParam("lang", equalTo("en"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(json)));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    ProjectTranslationsResponse r = client.getProjectTranslations("demo", "en").join();

    assertThat(r.getGroups().get("common").getEntries().get("k").asText()).isEqualTo("v");
    assertThat(r.getEntries()).isNull();
  }

  @Test
  void getProjectTranslations_sendsFormatFlatJson_whenRequested(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_PROJECT_PATH))
                .withQueryParam("project", equalTo("demo"))
                .withQueryParam("lang", equalTo("en"))
                .withQueryParam("format", equalTo(TranslaasClient.FORMAT_FLAT_JSON))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"project\":\"demo\",\"lang\":\"en\",\"version\":0,"
                            + "\"generatedAt\":\"2026-04-07T12:00:00Z\",\"entries\":{}}")));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    ProjectTranslationsResponse r =
        client
            .getProjectTranslations("demo", "en", TranslaasClient.FORMAT_FLAT_JSON, null, null)
            .join();

    assertThat(r.getEntries()).isEmpty();
    assertThat(r.getGroups()).isNull();
  }

  @Test
  void getGroupTranslations_setsNotModifiedAndReturnsEmpty_when304(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_GROUP_PATH))
                .withQueryParam("project", equalTo("demo"))
                .withQueryParam("group", equalTo("g"))
                .withQueryParam("lang", equalTo("en"))
                .withHeader("If-None-Match", equalTo("\"etag-group\""))
                .willReturn(aResponse().withStatus(304)));

    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.httpOrigin(wm.getHttpPort()))
            .useConditionalRequests(true)
            .build();
    TranslaasClient client = new TranslaasClient(options);

    TranslaasRequestContext ctx = new TranslaasRequestContext();
    ctx.setIfNoneMatch("\"etag-group\"");

    GroupTranslationsResponse r = client.getGroupTranslations("demo", "g", "en", ctx).join();

    assertThat(r).isNotNull();
    assertThat(r.getEntries()).isEmpty();
    assertThat(ctx.isNotModified()).isTrue();
  }

  @Test
  void getProjectTranslations_setsNotModifiedAndReturnsEmpty_when304(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_PROJECT_PATH))
                .withQueryParam("project", equalTo("demo"))
                .withQueryParam("lang", equalTo("en"))
                .withHeader("If-None-Match", equalTo("\"etag-proj\""))
                .willReturn(aResponse().withStatus(304)));

    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.httpOrigin(wm.getHttpPort()))
            .useConditionalRequests(true)
            .build();
    TranslaasClient client = new TranslaasClient(options);

    TranslaasRequestContext ctx = new TranslaasRequestContext();
    ctx.setIfNoneMatch("\"etag-proj\"");

    ProjectTranslationsResponse r = client.getProjectTranslations("demo", "en", ctx).join();

    assertThat(r).isNotNull();
    assertThat(r.getGroups()).isEmpty();
    assertThat(ctx.isNotModified()).isTrue();
  }

  @Test
  void getGroupTranslations_throwsIllegalArgumentException_whenGroupBlank() {
    TranslaasClient client =
        new TranslaasClient(
            TranslaasOptions.builder()
                .apiKey("k")
                .baseUrl(TestApiUrls.httpOrigin(9))
                .build());

    assertThatThrownBy(() -> client.getGroupTranslations("p", "  ", "en").join())
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
