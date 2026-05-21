package io.mantelabs.translaas.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

class TranslationCacheKeysTest {

  @Test
  void forTextPath_explicitEntryWithPluralAndInterpolation() {
    LinkedHashMap<String, String> q = new LinkedHashMap<>();
    q.put("group", "ui");
    q.put("lang", "en");
    q.put("entry", "save");
    q.put("n", "2");
    q.put("userName", "Ada");
    assertThat(TranslationCacheKeys.forTextPath(q))
        .isEqualTo("entry:ui:save:en:2:username=Ada");
  }

  @Test
  void forTextPath_shorthandEntryKey() {
    LinkedHashMap<String, String> q = new LinkedHashMap<>();
    q.put("group", "ui");
    q.put("lang", "en");
    q.put("save", "");
    assertThat(TranslationCacheKeys.forTextPath(q)).isEqualTo("entry:ui:save:en");
  }

  @Test
  void forGroupPath_includesFormatAndIncludeContext() {
    LinkedHashMap<String, String> q = new LinkedHashMap<>();
    q.put("project", "demo");
    q.put("group", "ui");
    q.put("lang", "en");
    q.put("format", "flat-json");
    q.put("channel", "beta");
    q.put("v", "snap1");
    q.put("includeContext", "true");
    assertThat(TranslationCacheKeys.forGroupPath(q))
        .isEqualTo("group:demo:ui:en:flat-json:ch=beta:v=snap1:ic=1");
  }

  @Test
  void forProjectPath_basic() {
    LinkedHashMap<String, String> q = new LinkedHashMap<>();
    q.put("project", "demo");
    q.put("lang", "en");
    assertThat(TranslationCacheKeys.forProjectPath(q)).isEqualTo("project:demo:en");
  }

  @Test
  void forLocalesPath_withSnapshot() {
    LinkedHashMap<String, String> q = new LinkedHashMap<>();
    q.put("project", "demo");
    q.put("channel", "web");
    q.put("v", "1");
    assertThat(TranslationCacheKeys.forLocalesPath(q))
        .isEqualTo("locales:demo:ch=web:v=1");
  }

  @Test
  void forOfflinePath_includeContextFalse() {
    LinkedHashMap<String, String> q = new LinkedHashMap<>();
    q.put("project", "demo");
    q.put("includeContext", "false");
    assertThat(TranslationCacheKeys.forOfflinePath(q)).isEqualTo("offline:demo:ic=0");
  }

  @Test
  void resolveEntryKey_prefersExplicitOverShorthand() {
    LinkedHashMap<String, String> q = new LinkedHashMap<>();
    q.put("entry", "explicit");
    q.put("shorthand", "");
    assertThat(TranslationCacheKeys.resolveEntryKey(q)).isEqualTo("explicit");
  }

  @Test
  void resolveEntryKey_blankExplicitUsesFirstNonReservedKey() {
    LinkedHashMap<String, String> q = new LinkedHashMap<>();
    q.put("group", "g");
    q.put("lang", "en");
    q.put("entry", "  ");
    q.put("myKey", "");
    assertThat(TranslationCacheKeys.resolveEntryKey(q)).isEqualTo("myKey");
  }

  @Test
  void forTextPath_invalidPluralIgnored() {
    LinkedHashMap<String, String> q = new LinkedHashMap<>();
    q.put("group", "ui");
    q.put("lang", "en");
    q.put("entry", "x");
    q.put("n", "not-a-number");
    assertThat(TranslationCacheKeys.forTextPath(q)).isEqualTo("entry:ui:x:en");
  }
}
