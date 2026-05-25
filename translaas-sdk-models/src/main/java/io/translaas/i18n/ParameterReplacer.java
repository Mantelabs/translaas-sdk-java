package io.translaas.i18n;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Replaces {@code {{name}}}, {@code {name}}, and {@code %name%} placeholders. */
public final class ParameterReplacer {

  private ParameterReplacer() {}

  public static String replace(
      String text, Map<String, String> parameters, BigDecimal number) {
    Map<String, String> merged = mergeNumber(number, parameters);
    if (merged.isEmpty()) {
      return text;
    }
    String result = text;
    for (Map.Entry<String, String> e : merged.entrySet()) {
      String key = Pattern.quote(e.getKey());
      String value = e.getValue();
      result = result.replaceAll("\\{\\{" + key + "\\}\\}", value);
    }
    for (Map.Entry<String, String> e : merged.entrySet()) {
      String key = Pattern.quote(e.getKey());
      String value = e.getValue();
      result = result.replaceAll("(?<!\\{)\\{" + key + "\\}(?!\\})", value);
    }
    for (Map.Entry<String, String> e : merged.entrySet()) {
      String key = Pattern.quote(e.getKey());
      result = result.replaceAll("%" + key + "%", e.getValue());
    }
    return result;
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
    if (number != null && !merged.containsKey("N") && !merged.containsKey("n")) {
      merged.put("N", number.toPlainString());
    }
    return merged;
  }
}
