package io.mantelabs.translaas.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TranslationTextQueriesTest {

  @Test
  void withExplicitEntry_injectsNWhenPluralSet() {
    Map<String, String> q =
        TranslationTextQueries.withExplicitEntry("g", "en", "item", new BigDecimal("2.5"), null);
    assertThat(q).containsEntry("n", "2.5");
    assertThat(q).containsEntry("N", "2.5");
  }

  @Test
  void withShorthandEntryKey_usesEntryAsParameterName() {
    Map<String, String> q =
        TranslationTextQueries.withShorthandEntryKey("g", "en", "welcome", null, null);
    assertThat(q).containsEntry("welcome", "");
    assertThat(q).doesNotContainKey("entry");
  }

  @Test
  void withShorthandEntryKey_rejectsReservedEntryKey() {
    assertThatThrownBy(
            () -> TranslationTextQueries.withShorthandEntryKey("g", "en", "group", null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("reserved");
  }

  @Test
  void withExplicitEntry_allowsUppercaseNInterpolation() {
    Map<String, String> q =
        TranslationTextQueries.withExplicitEntry(
            "g", "en", "item", new BigDecimal("1"), Map.of("N", "custom"));
    assertThat(q).containsEntry("N", "custom");
    assertThat(q).containsEntry("n", "1");
  }

  @Test
  void withExplicitEntry_rejectsReservedInterpolationName() {
    assertThatThrownBy(
            () ->
                TranslationTextQueries.withExplicitEntry(
                    "g", "en", "item", null, Map.of("group", "x")))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
