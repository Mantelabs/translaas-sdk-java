package io.translaas.caching.file;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.translaas.caching.file.offline.SpecFileCacheProvider;
import io.translaas.client.OfflineCacheOptions;
import io.translaas.client.OfflineFallbackMode;
import io.translaas.client.TranslaasClient;
import io.translaas.client.TranslaasOptions;
import io.translaas.models.ProjectGroupPayload;
import io.translaas.models.ProjectTranslationsResponse;
import io.translaas.models.json.TranslaasJson;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@WireMockTest
class CachingTranslaasClientPluralTest {

  @TempDir Path cacheDir;

  static Stream<Arguments> goldenGetEntryRows() {
    return Stream.of(
        Arguments.of("ar", BigDecimal.ZERO, "FORM:zero"),
        Arguments.of("ar", BigDecimal.valueOf(2), "FORM:two"),
        Arguments.of("pl", BigDecimal.valueOf(2), "FORM:few"),
        Arguments.of("fr", BigDecimal.ZERO, "FORM:one"),
        Arguments.of("en", BigDecimal.ZERO, "FORM:other"),
        Arguments.of("en", BigDecimal.ONE, "FORM:one"),
        Arguments.of("pt", BigDecimal.ZERO, "FORM:one"),
        Arguments.of("pt-PT", BigDecimal.ZERO, "FORM:other"));
  }

  @ParameterizedTest
  @MethodSource("goldenGetEntryRows")
  void cacheOnly_returnsFormForCldrCategory(
      String lang, BigDecimal number, String expected, WireMockRuntimeInfo wmInfo) {
    SpecFileCacheProvider fileCache = new SpecFileCacheProvider(cacheDir);
    seedPluralGroup(fileCache, lang, allForms());
    CachingTranslaasClient client = createClient(wmInfo, fileCache, OfflineFallbackMode.CACHE_ONLY);

    assertThat(client.getEntry("messages", "items", lang, number, null, null, null).join())
        .isEqualTo(expected);
  }

  @Test
  void cacheOnly_fallsBackToOtherForm_whenSelectedCategoryMissing(WireMockRuntimeInfo wmInfo) {
    SpecFileCacheProvider fileCache = new SpecFileCacheProvider(cacheDir);
    ObjectNode otherOnly = TranslaasJson.mapper().createObjectNode();
    otherOnly.put("other", "FORM:other-only");
    seedPluralGroup(fileCache, "ar", otherOnly);
    CachingTranslaasClient client = createClient(wmInfo, fileCache, OfflineFallbackMode.CACHE_ONLY);

    assertThat(
            client
                .getEntry("messages", "items", "ar", BigDecimal.valueOf(2), null, null, null)
                .join())
        .isEqualTo("FORM:other-only");
  }

  @Test
  void cacheOnly_returnsOtherForm_whenNumberIsNull(WireMockRuntimeInfo wmInfo) {
    SpecFileCacheProvider fileCache = new SpecFileCacheProvider(cacheDir);
    seedPluralGroup(fileCache, "fr", allForms());
    CachingTranslaasClient client = createClient(wmInfo, fileCache, OfflineFallbackMode.CACHE_ONLY);

    assertThat(client.getEntry("messages", "items", "fr", null, null, null, null).join())
        .isEqualTo("FORM:other");
  }

  @Test
  void cacheOnly_stillSubstitutesN_whenPluralFormSelected(WireMockRuntimeInfo wmInfo) {
    SpecFileCacheProvider fileCache = new SpecFileCacheProvider(cacheDir);
    ObjectNode forms = TranslaasJson.mapper().createObjectNode();
    forms.put("other", "Count {N}");
    seedPluralGroup(fileCache, "en", forms);
    CachingTranslaasClient client = createClient(wmInfo, fileCache, OfflineFallbackMode.CACHE_ONLY);

    assertThat(
            client
                .getEntry("messages", "items", "en", BigDecimal.ZERO, null, null, null)
                .join())
        .isEqualTo("Count 0");
  }

  @Test
  void cacheOnly_returnsSimpleString_whenEntryNotPlural(WireMockRuntimeInfo wmInfo) {
    SpecFileCacheProvider fileCache = new SpecFileCacheProvider(cacheDir);
    seedGroup(fileCache, "en", "common", "hello", new TextNode("Hello World"));
    CachingTranslaasClient client = createClient(wmInfo, fileCache, OfflineFallbackMode.CACHE_ONLY);

    assertThat(
            client
                .getEntry("common", "hello", "en", BigDecimal.valueOf(5), null, null, null)
                .join())
        .isEqualTo("Hello World");
  }

  @Test
  void cacheFirst_returnsCldrPlural_whenCacheHit(WireMockRuntimeInfo wmInfo) {
    SpecFileCacheProvider fileCache = new SpecFileCacheProvider(cacheDir);
    seedPluralGroup(fileCache, "pl", allForms());
    CachingTranslaasClient client = createClient(wmInfo, fileCache, OfflineFallbackMode.CACHE_FIRST);

    assertThat(
            client
                .getEntry("messages", "items", "pl", BigDecimal.valueOf(2), null, null, null)
                .join())
        .isEqualTo("FORM:few");
  }

  @Test
  void cacheFirst_usesPtPtRules_whenLocaleIsPtPt(WireMockRuntimeInfo wmInfo) {
    SpecFileCacheProvider fileCache = new SpecFileCacheProvider(cacheDir);
    seedPluralGroup(fileCache, "pt-PT", allForms());
    CachingTranslaasClient client = createClient(wmInfo, fileCache, OfflineFallbackMode.CACHE_FIRST);

    assertThat(
            client
                .getEntry("messages", "items", "pt-PT", BigDecimal.ZERO, null, null, null)
                .join())
        .isEqualTo("FORM:other");
  }

  private CachingTranslaasClient createClient(
      WireMockRuntimeInfo wmInfo, SpecFileCacheProvider fileCache, OfflineFallbackMode mode) {
    OfflineCacheOptions offline =
        OfflineCacheOptions.builder()
            .enabled(true)
            .cacheDirectory(cacheDir.toString())
            .fallbackMode(mode)
            .defaultProjectId("demo")
            .build();
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(URI.create(wmInfo.getHttpBaseUrl()))
            .defaultProject("demo")
            .offlineCache(offline)
            .build();
    return new CachingTranslaasClient(new TranslaasClient(options), fileCache, offline, "demo");
  }

  private static ObjectNode allForms() {
    ObjectNode plural = TranslaasJson.mapper().createObjectNode();
    plural.put("zero", "FORM:zero");
    plural.put("one", "FORM:one");
    plural.put("two", "FORM:two");
    plural.put("few", "FORM:few");
    plural.put("many", "FORM:many");
    plural.put("other", "FORM:other");
    return plural;
  }

  private void seedPluralGroup(SpecFileCacheProvider fileCache, String lang, JsonNode forms) {
    seedGroup(fileCache, lang, "messages", "items", forms);
  }

  private void seedGroup(
      SpecFileCacheProvider fileCache, String lang, String group, String entry, JsonNode value) {
    fileCache.saveProject(
        "demo",
        lang,
        new ProjectTranslationsResponse(
            "demo",
            lang,
            1,
            Instant.EPOCH,
            Map.of(group, new ProjectGroupPayload(Map.of(entry, value), null, null)),
            null,
            null,
            null));
  }
}
