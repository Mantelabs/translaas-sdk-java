package io.mantelabs.translaas.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ContentDispositionFilenamesTest {

  @Test
  void parseFilename_returnsQuotedValue() {
    assertThat(
            ContentDispositionFilenames.parseFilename(
                "attachment; filename=\"my bundle.zip\""))
        .isEqualTo("my bundle.zip");
  }

  @Test
  void parseFilename_returnsBareValue() {
    assertThat(ContentDispositionFilenames.parseFilename("attachment; filename=bundle.zip"))
        .isEqualTo("bundle.zip");
  }

  @Test
  void parseFilename_decodesFilenameStar() {
    assertThat(
            ContentDispositionFilenames.parseFilename(
                "attachment; filename*=UTF-8''hello%20world.zip"))
        .isEqualTo("hello world.zip");
  }

  @Test
  void parseFilename_prefersFilenameStar() {
    assertThat(
            ContentDispositionFilenames.parseFilename(
                "attachment; filename=\"a.zip\"; filename*=UTF-8''b.zip"))
        .isEqualTo("b.zip");
  }

  @Test
  void parseFilename_returnsNull_whenMissing() {
    assertThat(ContentDispositionFilenames.parseFilename("attachment")).isNull();
    assertThat(ContentDispositionFilenames.parseFilename(null)).isNull();
  }
}
