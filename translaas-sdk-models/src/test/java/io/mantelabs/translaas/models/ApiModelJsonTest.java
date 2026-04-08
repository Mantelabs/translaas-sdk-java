package io.mantelabs.translaas.models;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mantelabs.translaas.models.json.TranslaasJson;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApiModelJsonTest {

  private final ObjectMapper mapper = TranslaasJson.mapper();

  @Test
  void projectLocalesResponse_deserializesFromFixture() throws Exception {
    String json =
        "{"
            + "\"project\":\"demo\","
            + "\"locales\":[\"en\",\"es-MX\"],"
            + "\"lastModifiedUtc\":\"2026-04-07T12:00:00Z\""
            + "}";

    ProjectLocalesResponse r = mapper.readValue(json, ProjectLocalesResponse.class);

    assertThat(r.getProject()).isEqualTo("demo");
    assertThat(r.getLocales()).containsExactly("en", "es-MX");
    assertThat(r.getLastModifiedUtc()).isEqualTo(Instant.parse("2026-04-07T12:00:00Z"));
  }

  @Test
  void groupTranslationsResponse_deserializesFromFixture() throws Exception {
    String json =
        "{"
            + "\"project\":\"demo\","
            + "\"lang\":\"en\","
            + "\"version\":7,"
            + "\"generatedAt\":\"2026-04-07T12:00:00Z\","
            + "\"entries\":{\"greeting\":\"Hello\",\"count\":{\"other\":\"{n} items\"}},"
            + "\"entryContext\":{\"greeting\":{\"hint\":\"title\"}}"
            + "}";

    GroupTranslationsResponse r = mapper.readValue(json, GroupTranslationsResponse.class);

    assertThat(r.getProject()).isEqualTo("demo");
    assertThat(r.getLang()).isEqualTo("en");
    assertThat(r.getVersion()).isEqualTo(7);
    assertThat(r.getGeneratedAt()).isEqualTo(Instant.parse("2026-04-07T12:00:00Z"));
    assertThat(r.getEntries()).containsKeys("greeting", "count");
    assertThat(r.getEntries().get("greeting").asText()).isEqualTo("Hello");
    assertThat(r.getEntryContext().get("greeting").get("hint")).isEqualTo("title");
    assertThat(r.getGroupEntryContext()).isNull();
  }

  @Test
  void groupTranslationsResponse_deserializesGroupEntryContext() throws Exception {
    String json =
        "{"
            + "\"project\":\"demo\","
            + "\"lang\":\"en\","
            + "\"version\":1,"
            + "\"generatedAt\":\"2026-04-07T12:00:00Z\","
            + "\"entries\":{\"k\":\"v\"},"
            + "\"groupEntryContext\":{\"k\":{\"note\":\"meta\"}}"
            + "}";

    GroupTranslationsResponse r = mapper.readValue(json, GroupTranslationsResponse.class);

    assertThat(r.getGroupEntryContext().get("k").get("note")).isEqualTo("meta");
  }

  @Test
  void projectTranslationsResponse_deserializesFlatJsonShape() throws Exception {
    String json =
        "{"
            + "\"project\":\"demo\","
            + "\"lang\":\"en\","
            + "\"version\":1,"
            + "\"generatedAt\":\"2026-04-07T12:00:00Z\","
            + "\"entries\":{\"common.welcome\":\"Hello\",\"ui.btn\":\"OK\"},"
            + "\"entryContext\":{\"common.welcome\":{\"hint\":\"greeting\"}},"
            + "\"groupEntryContext\":null"
            + "}";

    ProjectTranslationsResponse r = mapper.readValue(json, ProjectTranslationsResponse.class);

    assertThat(r.getGroups()).isNull();
    assertThat(r.getEntries().get("common.welcome").asText()).isEqualTo("Hello");
    assertThat(r.getEntryContext().get("common.welcome").get("hint")).isEqualTo("greeting");
    assertThat(r.getGroupEntryContext()).isNull();
  }

  @Test
  void projectTranslationsResponse_deserializesNestedGroupsShape() throws Exception {
    String json =
        "{"
            + "\"project\":\"demo\","
            + "\"lang\":\"en\","
            + "\"version\":2,"
            + "\"generatedAt\":\"2026-04-07T12:00:00Z\","
            + "\"groups\":{"
            + "\"common\":{\"entries\":{\"welcome\":\"Hello\"},\"entryContext\":{\"welcome\":{\"hint\":\"h\"}}},"
            + "\"ui\":{\"entries\":{\"btn\":\"OK\"}}"
            + "}"
            + "}";

    ProjectTranslationsResponse r = mapper.readValue(json, ProjectTranslationsResponse.class);

    assertThat(r.getEntries()).isNull();
    assertThat(r.getGroups()).containsKeys("common", "ui");
    assertThat(r.getGroups().get("common").getEntries().get("welcome").asText()).isEqualTo("Hello");
    assertThat(r.getGroups().get("common").getEntryContext().get("welcome").get("hint"))
        .isEqualTo("h");
    assertThat(r.getGroups().get("ui").getEntries().get("btn").asText()).isEqualTo("OK");
  }

  @Test
  void validateApiKeyResponse_deserializesFromFixture() throws Exception {
    String json =
        "{"
            + "\"isValid\":true,"
            + "\"tenantId\":\"01HZYD8YJ8K9QWERTY1234567\","
            + "\"projectId\":\"01HZYD8YJ8K9QWERTY7654321\","
            + "\"integrationName\":\"ci\","
            + "\"authenticatedAt\":\"2026-04-07T12:47:01Z\""
            + "}";

    ValidateApiKeyResponse r = mapper.readValue(json, ValidateApiKeyResponse.class);

    assertThat(r.isValid()).isTrue();
    assertThat(r.getTenantId()).isEqualTo("01HZYD8YJ8K9QWERTY1234567");
    assertThat(r.getProjectId()).isEqualTo("01HZYD8YJ8K9QWERTY7654321");
    assertThat(r.getIntegrationName()).isEqualTo("ci");
    assertThat(r.getAuthenticatedAt()).isEqualTo(Instant.parse("2026-04-07T12:47:01Z"));
  }

  @Test
  void reportMissingKeysRequest_roundTrips() throws Exception {
    ReportMissingKeysRequest req =
        new ReportMissingKeysRequest(
            List.of(
                new ReportMissingKeyItemRequest("common", "welcome", "en"),
                new ReportMissingKeyItemRequest("ui", "btn.ok", "es")));

    String wire = mapper.writeValueAsString(req);
    ReportMissingKeysRequest back = mapper.readValue(wire, ReportMissingKeysRequest.class);

    assertThat(back.getKeys()).hasSize(2);
    assertThat(back.getKeys().get(0).getGroupKey()).isEqualTo("common");
    assertThat(back.getKeys().get(0).getEntryKey()).isEqualTo("welcome");
    assertThat(back.getKeys().get(0).getLanguageIsoCode()).isEqualTo("en");

    JsonNode node = mapper.readTree(wire);
    assertThat(node.get("keys").isArray()).isTrue();
    assertThat(node.get("keys").get(0).get("groupKey").asText()).isEqualTo("common");
  }

  @Test
  void mapper_ignoresUnknownProperties() throws Exception {
    String json = "{\"project\":\"p\",\"locales\":[\"en\"],\"extraFutureField\":true}";
    ProjectLocalesResponse r = mapper.readValue(json, ProjectLocalesResponse.class);
    assertThat(r.getProject()).isEqualTo("p");
    assertThat(r.getLocales()).containsExactly("en");
  }
}
