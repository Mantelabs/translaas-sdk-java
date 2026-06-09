package io.translaas.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PluralResolverTest {

  @Test
  void resolveCategory_oneOtherOnly() {
    assertThat(PluralResolver.resolveCategory(1, "en")).isEqualTo(PluralCategory.ONE);
    assertThat(PluralResolver.resolveCategory(BigDecimal.ONE, "en")).isEqualTo(PluralCategory.ONE);
    assertThat(PluralResolver.resolveCategory(0, "en")).isEqualTo(PluralCategory.OTHER);
    assertThat(PluralResolver.resolveCategory(5, "en")).isEqualTo(PluralCategory.OTHER);
    assertThat(PluralResolver.resolveCategory(null, "en")).isEqualTo(PluralCategory.OTHER);
  }

  @Test
  void resolveCategory_ignoresLanguage() {
    assertThat(PluralResolver.resolveCategory(0, "fr")).isEqualTo(PluralCategory.OTHER);
    assertThat(PluralResolver.resolveCategory(1, "fr")).isEqualTo(PluralCategory.ONE);
    assertThat(PluralResolver.resolveCategory(2, "ru")).isEqualTo(PluralCategory.OTHER);
  }
}
