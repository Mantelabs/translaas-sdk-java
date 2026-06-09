package io.translaas.i18n;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Resolves plural categories for offline cache reads.
 *
 * <p>Matches .NET {@code CachingTranslaasClient.DeterminePluralCategory}: one/other only ({@code 1 →
 * one}, else {@code other}); the language code is ignored.
 */
public final class PluralResolver {

  private PluralResolver() {}

  public static String normalizeLanguageCode(String lang) {
    if (lang == null || lang.isBlank()) {
      return "en";
    }
    return lang.split("-")[0].toLowerCase(Locale.ROOT);
  }

  public static PluralCategory resolveCategory(Number number, String lang) {
    if (number == null) {
      return PluralCategory.OTHER;
    }
    BigDecimal value =
        number instanceof BigDecimal
            ? (BigDecimal) number
            : BigDecimal.valueOf(number.doubleValue());
    return value.compareTo(BigDecimal.ONE) == 0 ? PluralCategory.ONE : PluralCategory.OTHER;
  }
}
