package io.translaas.caching.file;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.node.TextNode;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.translaas.client.OfflineCacheOptions;
import io.translaas.client.OfflineFallbackMode;
import io.translaas.client.TranslaasClient;
import io.translaas.client.TranslaasOptions;
import io.translaas.caching.file.offline.SpecFileCacheProvider;
import io.translaas.models.ProjectGroupPayload;
import io.translaas.models.ProjectTranslationsResponse;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@WireMockTest
class CachingTranslaasClientTest {

  @TempDir Path cacheDir;

  @Test
  void cacheOnly_readsFromDisk(WireMockRuntimeInfo wmInfo) {
    SpecFileCacheProvider fileCache = new SpecFileCacheProvider(cacheDir);
    fileCache.saveProject(
        "demo",
        "en",
        new ProjectTranslationsResponse(
            "demo",
            "en",
            1,
            Instant.EPOCH,
            Map.of(
                "g",
                new ProjectGroupPayload(Map.of("welcome", new TextNode("Hi")), null, null)),
            null,
            null,
            null));

    CachingTranslaasClient client = createCacheOnlyClient(wmInfo, fileCache);

    assertThat(client.getEntry("g", "welcome", "en").join()).isEqualTo("Hi");
  }

  @Test
  void cacheFirst_fallsBackToApiAndSeedsCache(WireMockRuntimeInfo wmInfo) {
    wmInfo.getWireMock()
        .register(
            get(urlPathEqualTo("/sdk/v1/translations/text"))
                .willReturn(aResponse().withStatus(200).withBody("From API")));

    OfflineCacheOptions offline =
        OfflineCacheOptions.builder()
            .enabled(true)
            .cacheDirectory(cacheDir.resolve("cf").toString())
            .fallbackMode(OfflineFallbackMode.CACHE_FIRST)
            .defaultProjectId("demo")
            .build();
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(URI.create(wmInfo.getHttpBaseUrl()))
            .defaultProject("demo")
            .offlineCache(offline)
            .build();
    CachingTranslaasClient client =
        new CachingTranslaasClient(
            new TranslaasClient(options),
            new SpecFileCacheProvider(offline),
            offline,
            "demo");

    assertThat(client.getEntry("g", "x", "en").join()).isEqualTo("From API");
  }

  @Test
  void cacheOnly_performsParameterSubstitution(WireMockRuntimeInfo wmInfo) {
    SpecFileCacheProvider fileCache = new SpecFileCacheProvider(cacheDir);
    fileCache.saveProject(
        "demo",
        "en",
        new ProjectTranslationsResponse(
            "demo",
            "en",
            1,
            Instant.EPOCH,
            Map.of(
                "messages",
                new ProjectGroupPayload(
                    Map.of("greeting", new TextNode("Hello {userName}, you have {count} items")),
                    null,
                    null)),
            null,
            null,
            null));

    CachingTranslaasClient client = createCacheOnlyClient(wmInfo, fileCache);

    assertThat(
            client
                .getEntry(
                    "messages",
                    "greeting",
                    "en",
                    null,
                    Map.of("userName", "John", "count", "5"),
                    null,
                    null)
                .join())
        .isEqualTo("Hello John, you have 5 items");
  }

  @Test
  void cacheOnly_performsNumberSubstitution(WireMockRuntimeInfo wmInfo) {
    SpecFileCacheProvider fileCache = new SpecFileCacheProvider(cacheDir);
    fileCache.saveProject(
        "demo",
        "en",
        new ProjectTranslationsResponse(
            "demo",
            "en",
            1,
            Instant.EPOCH,
            Map.of(
                "messages",
                new ProjectGroupPayload(
                    Map.of("items", new TextNode("You have {N} items")), null, null)),
            null,
            null,
            null));

    CachingTranslaasClient client = createCacheOnlyClient(wmInfo, fileCache);

    assertThat(
            client
                .getEntry(
                    "messages",
                    "items",
                    "en",
                    BigDecimal.valueOf(5),
                    null,
                    null,
                    null)
                .join())
        .isEqualTo("You have 5 items");
  }

  @Test
  void cacheOnly_performsSubstitutionWithNumberAndParameters(WireMockRuntimeInfo wmInfo) {
    SpecFileCacheProvider fileCache = new SpecFileCacheProvider(cacheDir);
    fileCache.saveProject(
        "demo",
        "en",
        new ProjectTranslationsResponse(
            "demo",
            "en",
            1,
            Instant.EPOCH,
            Map.of(
                "messages",
                new ProjectGroupPayload(
                    Map.of(
                        "greeting",
                        new TextNode("Hello {userName}, you have {N} items and {pending} pending")),
                    null,
                    null)),
            null,
            null,
            null));

    CachingTranslaasClient client = createCacheOnlyClient(wmInfo, fileCache);

    assertThat(
            client
                .getEntry(
                    "messages",
                    "greeting",
                    "en",
                    BigDecimal.valueOf(5),
                    Map.of("userName", "John", "pending", "3"),
                    null,
                    null)
                .join())
        .isEqualTo("Hello John, you have 5 items and 3 pending");
  }

  private CachingTranslaasClient createCacheOnlyClient(
      WireMockRuntimeInfo wmInfo, SpecFileCacheProvider fileCache) {
    OfflineCacheOptions offline =
        OfflineCacheOptions.builder()
            .enabled(true)
            .cacheDirectory(cacheDir.toString())
            .fallbackMode(OfflineFallbackMode.CACHE_ONLY)
            .defaultProjectId("demo")
            .build();
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(URI.create(wmInfo.getHttpBaseUrl()))
            .defaultProject("demo")
            .offlineCache(offline)
            .build();
    return new CachingTranslaasClient(
        new TranslaasClient(options), fileCache, offline, "demo");
  }
}
