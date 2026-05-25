package io.translaas.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.translaas.models.exception.TranslaasConfigurationException;
import java.net.URI;
import org.junit.jupiter.api.Test;

class SdkTranslationPathsTest {

  @Test
  void normalizesCustomPrefix() {
    SdkTranslationPaths paths = new SdkTranslationPaths("/custom/v1/translations");
    assertThat(paths.text()).isEqualTo("/custom/v1/translations/text");
    assertThat(paths.offlineCache()).isEqualTo("/custom/v1/translations/offline-cache");
  }

  @Test
  void optionsBuilder_appliesPrefix() {
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(URI.create("https://api.example.com"))
            .sdkTranslationsPathPrefix("/sdk/v2/translations")
            .build();
    assertThat(options.getSdkTranslationsPathPrefix()).isEqualTo("/sdk/v2/translations");
    assertThat(new SdkTranslationPaths(options).group()).isEqualTo("/sdk/v2/translations/group");
  }

  @Test
  void normalizePrefix_nullUsesDefault() {
    assertThat(SdkTranslationPaths.normalizePrefix(null))
        .isEqualTo(SdkTranslationPaths.DEFAULT_PREFIX);
  }

  @Test
  void normalizePrefix_trimsTrailingSlashes() {
    assertThat(SdkTranslationPaths.normalizePrefix("sdk/v1/translations///"))
        .isEqualTo("/sdk/v1/translations");
  }

  @Test
  void normalizePrefix_rejectsEmptyAfterTrim() {
    assertThatThrownBy(() -> SdkTranslationPaths.normalizePrefix("///"))
        .isInstanceOf(TranslaasConfigurationException.class);
  }
}
