package io.translaas.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.translaas.models.json.TranslaasJson;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TranslationEntriesTest {

  @Test
  void resolveEntryText_pluralAndParameters() {
    ObjectNode plural = TranslaasJson.mapper().createObjectNode();
    plural.put("one", "1 item");
    plural.put("other", "{N} items");
    String text =
        TranslationEntries.resolveEntryText(
            Map.of("count", plural),
            "count",
            "en",
            new BigDecimal("2"),
            Map.of("N", "2"));
    assertThat(text).isEqualTo("2 items");
  }

  @Test
  void resolveEntryText_selectsFewForPolish() {
    String text =
        TranslationEntries.resolveEntryText(
            Map.of("items", allForms()),
            "items",
            "pl",
            BigDecimal.valueOf(2),
            null);
    assertThat(text).isEqualTo("FORM:few");
  }

  @Test
  void resolveEntryText_selectsOneForFrenchZero() {
    String text =
        TranslationEntries.resolveEntryText(
            Map.of("items", allForms()),
            "items",
            "fr",
            BigDecimal.ZERO,
            null);
    assertThat(text).isEqualTo("FORM:one");
  }

  @Test
  void resolveEntryText_fallsBackToOtherWhenCategoryMissing() {
    ObjectNode otherOnly = TranslaasJson.mapper().createObjectNode();
    otherOnly.put("other", "FORM:other-only");
    String text =
        TranslationEntries.resolveEntryText(
            Map.of("items", otherOnly),
            "items",
            "ar",
            BigDecimal.valueOf(2),
            null);
    assertThat(text).isEqualTo("FORM:other-only");
  }

  @Test
  void resolveEntryText_nullNumberUsesOtherCategory() {
    String text =
        TranslationEntries.resolveEntryText(
            Map.of("items", allForms()), "items", "fr", null, null);
    assertThat(text).isEqualTo("FORM:other");
  }

  @Test
  void getValue_returnsStringEntry() {
    assertThat(TranslationEntries.getValue(Map.of("k", new TextNode("v")), "k")).isEqualTo("v");
  }

  private static ObjectNode allForms() {
    ObjectNode plural = TranslaasJson.mapper().createObjectNode();
    plural.put("zero", "FORM:zero");
    plural.put("one", "FORM:one");
    plural.put("two", "FORM:two");
    plural.put("few", "FORM:few");
    plural.put("many", "FORM:many");
    plural.put("other", "FORM:other");
    return plural;
  }
}
