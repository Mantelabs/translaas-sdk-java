package io.translaas.client;

import static org.assertj.core.api.Assertions.assertThat;

import io.translaas.models.GroupTranslationsResponse;
import io.translaas.models.ProjectTranslationsResponse;
import org.junit.jupiter.api.Test;

class TranslationResponseParsingTest {

  @Test
  void parseGroupResponse_bareEntriesMap() throws Exception {
    String json = "{\"title\":\"Hi\",\"count\":{\"other\":\"{n}\"}}";
    GroupTranslationsResponse r = TranslationResponseParsing.parseGroupResponse(json, null);
    assertThat(r.getEntries()).containsKeys("title", "count");
    assertThat(r.getProject()).isNull();
  }

  @Test
  void parseProjectResponse_flatCompositeKeys() throws Exception {
    String json =
        "{"
            + "\"project\":\"demo\","
            + "\"lang\":\"en\","
            + "\"version\":1,"
            + "\"generatedAt\":\"2026-04-07T12:00:00Z\","
            + "\"common.welcome\":\"Hello\","
            + "\"ui.btn\":\"OK\""
            + "}";
    ProjectTranslationsResponse r =
        TranslationResponseParsing.parseProjectResponse(json, TranslaasClient.FORMAT_FLAT_JSON);
    assertThat(r.getGroups()).containsKeys("common", "ui");
    assertThat(r.getGroups().get("common").getEntries().get("welcome").asText()).isEqualTo("Hello");
  }

  @Test
  void parseProjectResponse_excludesRootMetadataFromFlatCompositeGroups() throws Exception {
    String json =
        "{"
            + "\"project\":\"translaas-sdk-samples\","
            + "\"lang\":\"en\","
            + "\"version\":245734752,"
            + "\"generatedAt\":\"2026-01-15T12:00:00Z\","
            + "\"groupEntryContext\":{\"common\":{\"welcome.message\":{\"note\":\"ctx\"}}},"
            + "\"common.welcome.message\":\"Welcome\","
            + "\"messages.item\":\"1 item\""
            + "}";
    ProjectTranslationsResponse r =
        TranslationResponseParsing.parseProjectResponse(json, TranslaasClient.FORMAT_FLAT_JSON);

    assertThat(r.getProject()).isEqualTo("translaas-sdk-samples");
    assertThat(r.getLang()).isEqualTo("en");
    assertThat(r.getVersion()).isEqualTo(245734752);
    assertThat(r.getGroups()).containsKeys("common", "messages");
    assertThat(r.getGroups()).doesNotContainKeys("project", "lang", "version", "generatedAt", "groupEntryContext");
    assertThat(r.getGroups().get("common").getEntries().get("welcome.message").asText())
        .isEqualTo("Welcome");
  }

  @Test
  void parseProjectResponse_envelopeWithEmptyEntries() throws Exception {
    String json =
        "{\"project\":\"demo\",\"lang\":\"en\",\"version\":0,"
            + "\"generatedAt\":\"2026-04-07T12:00:00Z\",\"entries\":{}}";
    ProjectTranslationsResponse r =
        TranslationResponseParsing.parseProjectResponse(json, TranslaasClient.FORMAT_FLAT_JSON);
    assertThat(r.getEntries()).isEmpty();
  }
}
