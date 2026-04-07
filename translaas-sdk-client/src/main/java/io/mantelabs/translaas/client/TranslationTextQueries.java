package io.mantelabs.translaas.client;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Query parameters for {@code GET /sdk/v1/translations/text}.
 *
 * <p>Reserved query keys (must not be used as interpolation placeholder names): {@link
 * #RESERVED_QUERY_KEYS}. Prefer sending the entry key as {@code entry=&lt;key&gt;}; the server also
 * accepts <strong>shorthand</strong> where the entry key appears as a query parameter name with an
 * empty value (see {@link #withShorthandEntryKey(Map, String, String, String, BigDecimal,
 * Map)}).
 */
public final class TranslationTextQueries {

  /**
   * Query keys reserved by the text endpoint or shared SDK query handling. Interpolation parameters
   * are sent as additional query keys; those keys must not collide with this set.
   */
  public static final Set<String> RESERVED_QUERY_KEYS =
      Set.of(
          "group",
          "lang",
          "entry",
          "n",
          "N",
          "project",
          "channel",
          "v",
          "includeContext");

  private TranslationTextQueries() {}

  /**
   * Builds query entries for {@code group}, {@code lang}, {@code entry}, optional plural {@code n},
   * and interpolation parameters. Uses {@code entry=&lt;entryKey&gt;}.
   */
  public static Map<String, String> withExplicitEntry(
      String group,
      String lang,
      String entryKey,
      BigDecimal n,
      Map<String, String> interpolationParameters) {
    Objects.requireNonNull(group, "group");
    Objects.requireNonNull(lang, "lang");
    Objects.requireNonNull(entryKey, "entryKey");
    LinkedHashMap<String, String> q = new LinkedHashMap<>();
    q.put("group", group);
    q.put("lang", lang);
    q.put("entry", entryKey);
    putPluralAndInterpolation(q, n, interpolationParameters);
    return Collections.unmodifiableMap(q);
  }

  /**
   * Same as {@link #withExplicitEntry} but sends the entry key in <strong>shorthand</strong> form:
   * the entry appears as a query parameter whose <em>name</em> is the entry key and whose value is
   * empty.
   */
  public static Map<String, String> withShorthandEntryKey(
      String group,
      String lang,
      String entryKey,
      BigDecimal n,
      Map<String, String> interpolationParameters) {
    Objects.requireNonNull(group, "group");
    Objects.requireNonNull(lang, "lang");
    Objects.requireNonNull(entryKey, "entryKey");
    if (RESERVED_QUERY_KEYS.contains(entryKey.toLowerCase(Locale.ROOT))) {
      throw new IllegalArgumentException(
          "entryKey must not match a reserved query key (case-insensitive): " + entryKey);
    }
    LinkedHashMap<String, String> q = new LinkedHashMap<>();
    q.put("group", group);
    q.put("lang", lang);
    q.put(entryKey, "");
    putPluralAndInterpolation(q, n, interpolationParameters);
    return Collections.unmodifiableMap(q);
  }

  private static void putPluralAndInterpolation(
      LinkedHashMap<String, String> q, BigDecimal n, Map<String, String> interpolationParameters) {
    if (n != null) {
      q.put("n", n.toPlainString());
    }
    if (interpolationParameters == null || interpolationParameters.isEmpty()) {
      return;
    }
    for (Map.Entry<String, String> e : interpolationParameters.entrySet()) {
      String key = e.getKey();
      String value = e.getValue();
      if (key == null || value == null) {
        continue;
      }
      if (RESERVED_QUERY_KEYS.contains(key.toLowerCase(Locale.ROOT))) {
        throw new IllegalArgumentException(
            "Interpolation parameter name must not match a reserved query key (case-insensitive): "
                + key);
      }
      q.put(key, value);
    }
  }
}
