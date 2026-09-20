package io.translaas.i18n;

import com.ibm.icu.text.PluralRules;
import com.ibm.icu.util.ULocale;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Resolves CLDR cardinal plural categories for offline / file-cache entry selection.
 *
 * <p>Uses ICU4J {@link PluralRules} so locales such as Arabic, Polish, and French select {@code
 * zero} / {@code two} / {@code few} / {@code one} instead of an English-like {@code n == 1}
 * heuristic. Pass a full BCP-47 tag ({@code pt} vs {@code pt-PT}). Invalid or empty language tags
 * fall back to the base language, then {@code en}.
 *
 * <p>Live HTTP {@code GetEntry} is unchanged — the server still selects from {@code n}.
 */
public final class PluralResolver {

  private static final String ENGLISH_LOCALE = "en";
  private static final ConcurrentMap<String, PluralRules> RULES_CACHE = new ConcurrentHashMap<>();

  private PluralResolver() {}

  /**
   * Normalizes a language code by extracting the base language from a locale tag.
   *
   * <p>Examples: {@code en-US} → {@code en}, {@code fr-CA} → {@code fr}. Empty or null input
   * returns {@code en}. This helper is not the locale passed to ICU on the first attempt; {@link
   * #resolveCategory(Number, String)} uses the full BCP-47 tag so {@code pt} and {@code pt-PT} can
   * differ.
   *
   * @param lang language or locale code
   * @return base language code in lowercase, or {@code en} when {@code lang} is null or blank
   */
  public static String normalizeLanguageCode(String lang) {
    if (lang == null || lang.isBlank()) {
      return ENGLISH_LOCALE;
    }
    return lang.split("-")[0].toLowerCase(Locale.ROOT);
  }

  /**
   * Resolves the CLDR cardinal category for {@code number} and {@code lang}.
   *
   * @param number the count used for plural selection; when {@code null}, returns {@link
   *     PluralCategory#OTHER}
   * @param lang BCP-47 language tag (hyphens or underscores); empty or invalid tags fall back to
   *     {@code en}
   * @return the CLDR plural category
   */
  public static PluralCategory resolveCategory(Number number, String lang) {
    if (number == null) {
      return PluralCategory.OTHER;
    }
    final PluralRules rules = getPluralRules(lang);
    return mapKeyword(rules.select(number.doubleValue()));
  }

  private static PluralRules getPluralRules(String lang) {
    final String locale = normalizeLocaleTag(lang);
    final String cacheKey = locale.toLowerCase(Locale.ROOT);
    return RULES_CACHE.computeIfAbsent(cacheKey, ignored -> createPluralRules(locale));
  }

  private static String normalizeLocaleTag(String lang) {
    if (lang == null || lang.isBlank()) {
      return ENGLISH_LOCALE;
    }
    final String trimmed = lang.trim().replace('_', '-');
    return trimmed.isEmpty() ? ENGLISH_LOCALE : trimmed;
  }

  private static PluralRules createPluralRules(String locale) {
    PluralRules rules = tryForLocale(locale);
    if (rules != null) {
      return rules;
    }
    final String baseLanguage = getBaseLanguage(locale);
    if (!baseLanguage.equalsIgnoreCase(locale)) {
      rules = tryForLocale(baseLanguage);
      if (rules != null) {
        return rules;
      }
    }
    return PluralRules.forLocale(
        ULocale.forLanguageTag(ENGLISH_LOCALE), PluralRules.PluralType.CARDINAL);
  }

  private static String getBaseLanguage(String locale) {
    final int separator = locale.indexOf('-');
    if (separator <= 0) {
      return locale;
    }
    return locale.substring(0, separator);
  }

  private static PluralRules tryForLocale(String locale) {
    if (!looksLikeBcp47(locale)) {
      return null;
    }
    try {
      return PluralRules.forLocale(
          ULocale.forLanguageTag(locale), PluralRules.PluralType.CARDINAL);
    } catch (RuntimeException ex) {
      return null;
    }
  }

  /**
   * Accepts language tags such as {@code en}, {@code pt-PT}, {@code zh-Hans-CN}. Rejects free text
   * so ICU does not silently use the root locale (always {@code other}).
   */
  private static boolean looksLikeBcp47(String locale) {
    if (locale.length() < 2) {
      return false;
    }
    boolean firstSegment = true;
    int segmentLength = 0;
    for (int i = 0; i < locale.length(); i++) {
      final char c = locale.charAt(i);
      if (c == '-') {
        if (segmentLength == 0 || (firstSegment && segmentLength < 2)) {
          return false;
        }
        firstSegment = false;
        segmentLength = 0;
        continue;
      }
      final boolean isLetter = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
      final boolean isDigit = c >= '0' && c <= '9';
      if (firstSegment) {
        if (!isLetter) {
          return false;
        }
      } else if (!isLetter && !isDigit) {
        return false;
      }
      segmentLength++;
      if (segmentLength > 8) {
        return false;
      }
    }
    return firstSegment ? segmentLength >= 2 : segmentLength >= 1;
  }

  private static PluralCategory mapKeyword(String keyword) {
    if (keyword == null) {
      return PluralCategory.OTHER;
    }
    switch (keyword) {
      case "zero":
        return PluralCategory.ZERO;
      case "one":
        return PluralCategory.ONE;
      case "two":
        return PluralCategory.TWO;
      case "few":
        return PluralCategory.FEW;
      case "many":
        return PluralCategory.MANY;
      default:
        return PluralCategory.OTHER;
    }
  }
}
