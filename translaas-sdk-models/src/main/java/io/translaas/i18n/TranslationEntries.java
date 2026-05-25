package io.translaas.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.Map;

/** Helpers for resolving translation entries from cached JSON maps. */
public final class TranslationEntries {

  private TranslationEntries() {}

  public static boolean hasPluralForms(Map<String, JsonNode> entries, String key) {
    JsonNode node = entries != null ? entries.get(key) : null;
    return node != null && node.isObject();
  }

  public static String getValue(Map<String, JsonNode> entries, String key) {
    JsonNode node = entries != null ? entries.get(key) : null;
    if (node != null && node.isTextual()) {
      return node.asText();
    }
    return null;
  }

  public static String getPluralForm(
      Map<String, JsonNode> entries, String key, PluralCategory category) {
    JsonNode node = entries != null ? entries.get(key) : null;
    if (node == null || !node.isObject()) {
      return null;
    }
    JsonNode form = node.get(category.wireName());
    return form != null && form.isTextual() ? form.asText() : null;
  }

  public static String resolveEntryText(
      Map<String, JsonNode> entries,
      String entryKey,
      String lang,
      BigDecimal number,
      Map<String, String> parameters) {
    if (entries == null) {
      return null;
    }
    String template;
    if (hasPluralForms(entries, entryKey)) {
      PluralCategory category =
          PluralResolver.resolveCategory(number != null ? number : BigDecimal.ZERO, lang);
      template = getPluralForm(entries, entryKey, category);
      if (template == null && category != PluralCategory.OTHER) {
        template = getPluralForm(entries, entryKey, PluralCategory.OTHER);
      }
    } else {
      template = getValue(entries, entryKey);
    }
    if (template == null) {
      return null;
    }
    return ParameterReplacer.replace(template, parameters, number);
  }
}
