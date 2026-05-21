package io.mantelabs.translaas.caching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CacheKeyBuilderTest {

  @Test
  void buildEntryKey_basic() {
    assertThat(CacheKeyBuilder.buildEntryKey("ui", "button.save", "en", null, null, null, null, null))
        .isEqualTo("entry:ui:button.save:en");
  }

  @Test
  void buildEntryKey_withNumberAndParameters() {
    Map<String, String> params = new LinkedHashMap<>();
    params.put("userName", "John");
    params.put("count", "5");
    assertThat(
            CacheKeyBuilder.buildEntryKey(
                "messages", "greeting", "en", new BigDecimal("5"), params, null, null, null))
        .isEqualTo("entry:messages:greeting:en:5:count=5:username=John");
  }

  @Test
  void buildGroupKey_withFormatAndSnapshot() {
    assertThat(
            CacheKeyBuilder.buildGroupKey(
                "my-project", "ui", "en", "flat-json", "beta", "snap1", true))
        .isEqualTo("group:my-project:ui:en:flat-json:ch=beta:v=snap1:ic=1");
  }

  @Test
  void buildEntryKey_rejectsNullGroup() {
    assertThatThrownBy(() -> CacheKeyBuilder.buildEntryKey(null, "e", "en", null, null, null, null, null))
        .isInstanceOf(NullPointerException.class);
  }
}
