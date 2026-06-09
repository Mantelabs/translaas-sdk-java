package io.translaas.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ParameterReplacerTest {

  @Test
  void replace_singlePlaceholder() {
    assertThat(ParameterReplacer.replace("Hello {userName}!", Map.of("userName", "John"), null))
        .isEqualTo("Hello John!");
  }

  @Test
  void replace_caseInsensitiveKeys() {
    assertThat(ParameterReplacer.replace("Value {n}", Map.of("N", "42"), null))
        .isEqualTo("Value 42");
  }

  @Test
  void replace_numberInjection() {
    assertThat(ParameterReplacer.replace("You have {N} items", null, BigDecimal.valueOf(5)))
        .isEqualTo("You have 5 items");
  }

  @Test
  void replace_explicitNWins() {
    assertThat(
            ParameterReplacer.replace(
                "You have {N} items", Map.of("N", "9"), BigDecimal.valueOf(5)))
        .isEqualTo("You have 9 items");
  }

  @Test
  void replace_combinedNumberAndParams() {
    assertThat(
            ParameterReplacer.replace(
                "Hello {userName}, you have {N} items and {pending} pending",
                Map.of("userName", "John", "pending", "3"),
                BigDecimal.valueOf(5)))
        .isEqualTo("Hello John, you have 5 items and 3 pending");
  }

  @Test
  void replace_leavesUnknownPlaceholders() {
    assertThat(ParameterReplacer.replace("Hello {name}", null, null)).isEqualTo("Hello {name}");
  }

  @Test
  void replace_doesNotSupportPercentFormat() {
    assertThat(ParameterReplacer.replace("Hi %userName%", Map.of("userName", "John"), null))
        .isEqualTo("Hi %userName%");
  }

  @Test
  void replace_matchesInnerSingleBraceTokensInDoubleBraceText() {
    assertThat(ParameterReplacer.replace("Hi {{userName}}", Map.of("userName", "John"), null))
        .isEqualTo("Hi {John}");
  }
}
