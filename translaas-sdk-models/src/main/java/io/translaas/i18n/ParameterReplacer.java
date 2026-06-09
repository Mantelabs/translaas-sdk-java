package io.translaas.i18n;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces {@code {name}} placeholders in offline cache templates.
 *
 * <p>Matches .NET {@code CachingTranslaasClient.SubstituteParameters}: {@code {name}} only,
 * case-insensitive parameter keys, and auto-{@code N} from {@code number} unless an explicit {@code
 * N} is already provided.
 */
public final class ParameterReplacer {

  private static final Pattern PLACEHOLDER = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");

  private ParameterReplacer() {}

  public static String replace(
      String text, Map<String, String> parameters, BigDecimal number) {
    Map<String, String> merged = mergeNumber(number, parameters);
    if (merged.isEmpty()) {
      return text;
    }
    Matcher matcher = PLACEHOLDER.matcher(text);
    StringBuffer buffer = new StringBuffer();
    while (matcher.find()) {
      String placeholderName = matcher.group(1);
      String value = lookup(merged, placeholderName);
      matcher.appendReplacement(buffer, Matcher.quoteReplacement(value != null ? value : matcher.group(0)));
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }

  private static Map<String, String> mergeNumber(
      BigDecimal number, Map<String, String> parameters) {
    LinkedHashMap<String, String> merged = new LinkedHashMap<>();
    if (parameters != null) {
      parameters.forEach(
          (k, v) -> {
            if (k != null && v != null) {
              merged.put(k, v);
            }
          });
    }
    if (number != null && lookup(merged, "N") == null) {
      merged.put("N", number.toPlainString());
    }
    return merged;
  }

  private static String lookup(Map<String, String> parameters, String name) {
    if (parameters.containsKey(name)) {
      return parameters.get(name);
    }
    String lowered = name.toLowerCase(Locale.ROOT);
    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      if (entry.getKey() != null && entry.getKey().toLowerCase(Locale.ROOT).equals(lowered)) {
        return entry.getValue();
      }
    }
    return null;
  }
}
