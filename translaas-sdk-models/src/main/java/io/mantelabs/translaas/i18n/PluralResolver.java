package io.mantelabs.translaas.i18n;

import java.util.Locale;
import java.util.Map;

/** Resolves plural categories from a number and language code (offline reads). */
public final class PluralResolver {

  private enum PluralPattern {
    ENGLISH_LIKE,
    FRENCH_LIKE,
    SLAVIC,
    ARABIC
  }

  private static final Map<String, PluralPattern> LANGUAGE_PATTERNS =
      Map.ofEntries(
          Map.entry("en", PluralPattern.ENGLISH_LIKE),
          Map.entry("de", PluralPattern.ENGLISH_LIKE),
          Map.entry("nl", PluralPattern.ENGLISH_LIKE),
          Map.entry("fr", PluralPattern.FRENCH_LIKE),
          Map.entry("pt", PluralPattern.FRENCH_LIKE),
          Map.entry("es", PluralPattern.FRENCH_LIKE),
          Map.entry("ru", PluralPattern.SLAVIC),
          Map.entry("pl", PluralPattern.SLAVIC),
          Map.entry("ar", PluralPattern.ARABIC));

  private PluralResolver() {}

  public static String normalizeLanguageCode(String lang) {
    if (lang == null || lang.isBlank()) {
      return "en";
    }
    return lang.split("-")[0].toLowerCase(Locale.ROOT);
  }

  public static PluralCategory resolveCategory(Number number, String lang) {
    double n = number != null ? number.doubleValue() : 0;
    PluralPattern pattern = LANGUAGE_PATTERNS.get(normalizeLanguageCode(lang));
    if (pattern == null || pattern == PluralPattern.ENGLISH_LIKE) {
      return englishLike(n);
    }
    if (pattern == PluralPattern.FRENCH_LIKE) {
      return frenchLike(n);
    }
    if (pattern == PluralPattern.SLAVIC) {
      return slavic(n, lang);
    }
    if (pattern == PluralPattern.ARABIC) {
      return arabic(n);
    }
    return englishLike(n);
  }

  private static PluralCategory englishLike(double number) {
    return Math.abs(number) == 1 ? PluralCategory.ONE : PluralCategory.OTHER;
  }

  private static PluralCategory frenchLike(double number) {
    double n = Math.abs(number);
    return (n == 0 || n == 1) ? PluralCategory.ONE : PluralCategory.OTHER;
  }

  private static PluralCategory slavic(double number, String lang) {
    String normalized = normalizeLanguageCode(lang);
    int n = (int) Math.abs(number);
    int mod10 = n % 10;
    int mod100 = n % 100;
    if ("pl".equals(normalized)) {
      if (n == 1) {
        return PluralCategory.ONE;
      }
      if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) {
        return PluralCategory.FEW;
      }
      return PluralCategory.MANY;
    }
    if ("cs".equals(normalized) || "sk".equals(normalized)) {
      if (n == 1) {
        return PluralCategory.ONE;
      }
      if (n >= 2 && n <= 4) {
        return PluralCategory.FEW;
      }
      return PluralCategory.MANY;
    }
    if (mod10 == 1 && mod100 != 11) {
      return PluralCategory.ONE;
    }
    if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) {
      return PluralCategory.FEW;
    }
    return PluralCategory.MANY;
  }

  private static PluralCategory arabic(double number) {
    int n = (int) Math.abs(number);
    if (n == 0) {
      return PluralCategory.ZERO;
    }
    if (n == 1) {
      return PluralCategory.ONE;
    }
    if (n == 2) {
      return PluralCategory.TWO;
    }
    int mod100 = n % 100;
    if (mod100 >= 3 && mod100 <= 10) {
      return PluralCategory.FEW;
    }
    if (mod100 >= 11 && mod100 <= 99) {
      return PluralCategory.MANY;
    }
    return PluralCategory.OTHER;
  }
}
