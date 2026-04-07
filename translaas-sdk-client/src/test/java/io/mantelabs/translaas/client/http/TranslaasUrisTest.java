package io.mantelabs.translaas.client.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.mantelabs.translaas.client.CacheMode;
import io.mantelabs.translaas.client.TestApiUrls;
import io.mantelabs.translaas.client.TranslaasOptions;
import io.mantelabs.translaas.client.TranslaasRequestContext;
import io.mantelabs.translaas.models.exception.TranslaasConfigurationException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TranslaasUrisTest {

  @Test
  void normalizeApiOrigin_stripsPathAndQuery() {
    URI in = URI.create(TestApiUrls.ORIGIN + "/sdk/v1/extra?x=1#frag");
    assertThat(TranslaasUris.normalizeApiOrigin(in)).hasToString(TestApiUrls.ORIGIN);
  }

  @Test
  void normalizeApiOrigin_trailingSlashInInput_yieldsOriginWithoutPath() {
    URI in = URI.create(TestApiUrls.ORIGIN + "/");
    assertThat(TranslaasUris.normalizeApiOrigin(in)).hasToString(TestApiUrls.ORIGIN);
  }

  @Test
  void normalizeApiOrigin_preservesNonDefaultPort() {
    URI in = URI.create(TestApiUrls.ORIGIN_PORT_8443 + "/foo");
    assertThat(TranslaasUris.normalizeApiOrigin(in)).hasToString(TestApiUrls.ORIGIN_PORT_8443);
  }

  @Test
  void normalizeApiOrigin_rejectsRelativeUri() {
    assertThatThrownBy(() -> TranslaasUris.normalizeApiOrigin(URI.create("/only/path")))
        .isInstanceOf(TranslaasConfigurationException.class);
  }

  @Test
  void buildUri_appendsPathOnce_noDuplicateSdkSegment() {
    URI origin = URI.create(TestApiUrls.ORIGIN);
    URI full =
        TranslaasUris.buildUri(
            origin, "/sdk/v1/translations/text", Map.of("locale", "en", "q", "a b"));
    assertThat(full.getScheme()).isEqualTo("https");
    assertThat(full.getHost()).isEqualTo(TestApiUrls.HOST);
    assertThat(full.getPath()).isEqualTo("/sdk/v1/translations/text");
    assertThat(full.getRawQuery()).contains("locale=en");
    assertThat(full.getRawQuery()).contains("q=a+b");
  }

  @Test
  void translaasOptions_builderNormalizesBaseUrl_forHostOnlyConfig() {
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.ORIGIN + "/sdk/v1/")
            .build();
    assertThat(options.getBaseUrl()).hasToString(TestApiUrls.ORIGIN);
  }

  @Test
  void mergeQueryParams_explicitWinsOnCollision() {
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.ORIGIN)
            .channel("default-ch")
            .snapshotVersion("1")
            .build();
    TranslaasRequestContext ctx = new TranslaasRequestContext();
    ctx.setChannel("ctx-ch");
    LinkedHashMap<String, String> merged =
        TranslaasUris.mergeQueryParams(
            options, ctx, Map.of("channel", "explicit-ch", "locale", "en"));
    assertThat(merged)
        .containsEntry("channel", "explicit-ch")
        .containsEntry("v", "1")
        .containsEntry("locale", "en");
  }

  @Test
  void mergeQueryParams_includeContextFromOptions() {
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.ORIGIN)
            .cacheMode(CacheMode.NONE)
            .includeContextDefault(true)
            .build();
    LinkedHashMap<String, String> merged =
        TranslaasUris.mergeQueryParams(options, new TranslaasRequestContext(), Map.of());
    assertThat(merged).containsEntry("includeContext", "true");
  }
}
