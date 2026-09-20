package io.translaas.i18n;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PluralResolverTest {

  static Stream<Arguments> goldenPluralRows() {
    return Stream.of(
        Arguments.of("ar", 0, PluralCategory.ZERO),
        Arguments.of("ar", 2, PluralCategory.TWO),
        Arguments.of("pl", 2, PluralCategory.FEW),
        Arguments.of("fr", 0, PluralCategory.ONE),
        Arguments.of("en", 0, PluralCategory.OTHER),
        Arguments.of("en", 1, PluralCategory.ONE));
  }

  static Stream<Arguments> antiBucketPluralRows() {
    return Stream.of(
        Arguments.of("he", 2, PluralCategory.TWO),
        Arguments.of("ja", 1, PluralCategory.OTHER),
        Arguments.of("pt", 0, PluralCategory.ONE),
        Arguments.of("pt-PT", 0, PluralCategory.OTHER),
        Arguments.of("bg", 2, PluralCategory.OTHER),
        Arguments.of("es", 0, PluralCategory.OTHER),
        Arguments.of("fr-CA", 0, PluralCategory.ONE),
        Arguments.of("ar_EG", 0, PluralCategory.ZERO));
  }

  @ParameterizedTest
  @MethodSource("goldenPluralRows")
  void resolveCategory_whenGoldenTableRow_returnsExpectedCategory(
      String lang, int number, PluralCategory expected) {
    assertThat(PluralResolver.resolveCategory(number, lang)).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("antiBucketPluralRows")
  void resolveCategory_whenAntiBucketRow_returnsExpectedCategory(
      String lang, int number, PluralCategory expected) {
    assertThat(PluralResolver.resolveCategory(number, lang)).isEqualTo(expected);
  }

  @Test
  void resolveCategory_whenFormerIgnoresLanguageCases_usesCldr() {
    assertThat(PluralResolver.resolveCategory(0, "fr")).isEqualTo(PluralCategory.ONE);
    assertThat(PluralResolver.resolveCategory(1, "fr")).isEqualTo(PluralCategory.ONE);
    assertThat(PluralResolver.resolveCategory(2, "ru")).isEqualTo(PluralCategory.FEW);
  }

  @Test
  void resolveCategory_whenNullNumber_returnsOther() {
    assertThat(PluralResolver.resolveCategory(null, "ar")).isEqualTo(PluralCategory.OTHER);
    assertThat(PluralResolver.resolveCategory(null, "en")).isEqualTo(PluralCategory.OTHER);
  }

  @Test
  void resolveCategory_whenBigDecimalOne_returnsOne() {
    assertThat(PluralResolver.resolveCategory(BigDecimal.ONE, "en")).isEqualTo(PluralCategory.ONE);
  }

  @Test
  void resolveCategory_whenLocaleUsesUnderscore_normalizesToHyphen() {
    assertThat(PluralResolver.resolveCategory(1, "en_US")).isEqualTo(PluralCategory.ONE);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void resolveCategory_whenLangMissing_fallsBackToEnglish(String lang) {
    assertThat(PluralResolver.resolveCategory(1, lang)).isEqualTo(PluralCategory.ONE);
    assertThat(PluralResolver.resolveCategory(0, lang)).isEqualTo(PluralCategory.OTHER);
  }

  @Test
  void resolveCategory_whenInvalidLocale_fallsBackToEnglish() {
    assertThatCode(() -> PluralResolver.resolveCategory(1, "not a locale!!"))
        .doesNotThrowAnyException();
    assertThat(PluralResolver.resolveCategory(0, "not a locale!!")).isEqualTo(PluralCategory.OTHER);
    assertThat(PluralResolver.resolveCategory(1, "not a locale!!")).isEqualTo(PluralCategory.ONE);
  }

  @Test
  void resolveCategory_whenPortugueseRegionDiffers_doesNotStripToBaseLanguage() {
    assertThat(PluralResolver.resolveCategory(0, "pt")).isEqualTo(PluralCategory.ONE);
    assertThat(PluralResolver.resolveCategory(0, "pt-PT")).isEqualTo(PluralCategory.OTHER);
  }

  @Test
  void resolveCategory_whenDecimalN_matchesCldr() {
    assertThat(PluralResolver.resolveCategory(new BigDecimal("1.5"), "en"))
        .isEqualTo(PluralCategory.OTHER);
    // CLDR French `one` includes n in 0..1 (decimals such as 1.5).
    assertThat(PluralResolver.resolveCategory(new BigDecimal("1.5"), "fr"))
        .isEqualTo(PluralCategory.ONE);
  }

  @Test
  void normalizeLanguageCode_whenLocaleHasRegion_extractsBaseLanguage() {
    assertThat(PluralResolver.normalizeLanguageCode("en-US")).isEqualTo("en");
    assertThat(PluralResolver.normalizeLanguageCode("fr-CA")).isEqualTo("fr");
  }

  @Test
  void normalizeLanguageCode_whenInvalidInput_fallsBackToEn() {
    assertThat(PluralResolver.normalizeLanguageCode(null)).isEqualTo("en");
    assertThat(PluralResolver.normalizeLanguageCode("")).isEqualTo("en");
    assertThat(PluralResolver.normalizeLanguageCode("   ")).isEqualTo("en");
  }
}
