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
  void getValue_returnsStringEntry() {
    assertThat(TranslationEntries.getValue(Map.of("k", new TextNode("v")), "k")).isEqualTo("v");
  }
}
