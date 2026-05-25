package io.translaas.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.translaas.models.ProjectLocalesResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/**
 * L1 memory caching: hit avoids HTTP, miss fetches, expiration evicts, {@code If-None-Match} skips
 * cache read, {@code 304} can be satisfied from cache when a prior {@code 200} was stored.
 */
@WireMockTest
class TranslaasClientCachingTest {

  @Test
  void getEntry_secondCallUsesCache_soSingleHttpRequest(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH))
                .withQueryParam("group", equalTo("g"))
                .withQueryParam("lang", equalTo("en"))
                .withQueryParam("entry", equalTo("k"))
                .willReturn(aResponse().withStatus(200).withBody("one")));

    TranslaasClient client = client(wm.getHttpPort(), CacheMode.ENTRY);

    assertThat(client.getEntry("g", "k", "en").join()).isEqualTo("one");
    assertThat(client.getEntry("g", "k", "en").join()).isEqualTo("one");

    verify(
        exactly(1),
        getRequestedFor(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH)));
  }

  @Test
  void getEntry_afterAbsoluteExpiration_refetches(WireMockRuntimeInfo wm) throws Exception {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH))
                .withQueryParam("group", equalTo("g"))
                .withQueryParam("lang", equalTo("en"))
                .withQueryParam("entry", equalTo("k"))
                .willReturn(aResponse().withStatus(200).withBody("a"))
                .atPriority(1));

    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.httpOrigin(wm.getHttpPort()))
            .cacheMode(CacheMode.ENTRY)
            .cacheAbsoluteExpiration(Duration.ofMillis(30))
            .build();
    TranslaasClient client = new TranslaasClient(options);

    assertThat(client.getEntry("g", "k", "en").join()).isEqualTo("a");
    Thread.sleep(60);
    assertThat(client.getEntry("g", "k", "en").join()).isEqualTo("a");

    verify(
        exactly(2),
        getRequestedFor(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH)));
  }

  @Test
  void getProjectLocales_notCached_whenCacheModeIsGroup(WireMockRuntimeInfo wm) {
    String json = "{\"project\":\"demo\",\"locales\":[\"en\"]}";
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_LOCALES_PATH))
                .withQueryParam("project", equalTo("demo"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(json)));

    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.httpOrigin(wm.getHttpPort()))
            .cacheMode(CacheMode.GROUP)
            .cacheAbsoluteExpiration(Duration.ofHours(1))
            .build();
    TranslaasClient client = new TranslaasClient(options);

    client.getProjectLocales("demo").join();
    client.getProjectLocales("demo").join();

    verify(
        exactly(2),
        getRequestedFor(urlPathEqualTo(TranslaasClient.TRANSLATIONS_LOCALES_PATH)));
  }

  @Test
  void getProjectLocales_secondCallUsesCache_whenCacheModeIsProject(WireMockRuntimeInfo wm) {
    String json = "{\"project\":\"demo\",\"locales\":[\"en\"]}";
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_LOCALES_PATH))
                .withQueryParam("project", equalTo("demo"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(json)));

    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.httpOrigin(wm.getHttpPort()))
            .cacheMode(CacheMode.PROJECT)
            .cacheAbsoluteExpiration(Duration.ofHours(1))
            .build();
    TranslaasClient client = new TranslaasClient(options);

    ProjectLocalesResponse a = client.getProjectLocales("demo").join();
    ProjectLocalesResponse b = client.getProjectLocales("demo").join();

    assertThat(a.getLocales()).containsExactly("en");
    assertThat(b.getLocales()).containsExactly("en");

    verify(
        exactly(1),
        getRequestedFor(urlPathEqualTo(TranslaasClient.TRANSLATIONS_LOCALES_PATH)));
  }

  @Test
  void getEntry_ifNoneMatchSet_skipsCacheRead_soTwoRequests(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH))
                .withQueryParam("group", equalTo("g"))
                .withQueryParam("lang", equalTo("en"))
                .withQueryParam("entry", equalTo("k"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("ETag", "\"t\"")
                        .withBody("x")));

    TranslaasClient client = client(wm.getHttpPort(), CacheMode.ENTRY);

    TranslaasRequestContext first = new TranslaasRequestContext();
    client.getEntry("g", "k", "en", first).join();

    TranslaasRequestContext second = new TranslaasRequestContext();
    second.setIfNoneMatch(first.getResponseETag().get());
    client.getEntry("g", "k", "en", second).join();

    verify(
        exactly(2),
        getRequestedFor(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH)));
  }

  @Test
  void getEntry_returnsCachedBody_when304AfterConditionalRequest(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH))
                .withQueryParam("group", equalTo("g"))
                .withQueryParam("lang", equalTo("en"))
                .withQueryParam("entry", equalTo("k"))
                .withHeader("If-None-Match", equalTo("\"v1\""))
                .willReturn(aResponse().withStatus(304))
                .atPriority(1));

    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH))
                .withQueryParam("group", equalTo("g"))
                .withQueryParam("lang", equalTo("en"))
                .withQueryParam("entry", equalTo("k"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("ETag", "\"v1\"")
                        .withBody("cached-text"))
                .atPriority(5));

    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.httpOrigin(wm.getHttpPort()))
            .cacheMode(CacheMode.ENTRY)
            .cacheAbsoluteExpiration(Duration.ofHours(1))
            .useConditionalRequests(true)
            .build();
    TranslaasClient client = new TranslaasClient(options);

    TranslaasRequestContext seed = new TranslaasRequestContext();
    assertThat(client.getEntry("g", "k", "en", seed).join()).isEqualTo("cached-text");

    TranslaasRequestContext revalidate = new TranslaasRequestContext();
    revalidate.setIfNoneMatch("\"v1\"");
    String second = client.getEntry("g", "k", "en", revalidate).join();

    assertThat(second).isEqualTo("cached-text");
    assertThat(revalidate.isNotModified()).isTrue();

    verify(
        exactly(2),
        getRequestedFor(urlPathEqualTo(TranslaasClient.TRANSLATIONS_TEXT_PATH)));
  }

  private static TranslaasClient client(int port, CacheMode mode) {
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.httpOrigin(port))
            .cacheMode(mode)
            .cacheAbsoluteExpiration(Duration.ofHours(1))
            .build();
    return new TranslaasClient(options);
  }
}
