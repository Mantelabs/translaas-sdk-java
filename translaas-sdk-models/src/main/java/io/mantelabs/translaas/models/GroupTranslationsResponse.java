package io.mantelabs.translaas.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.Map;

/**
 * Response body for {@code GET /sdk/v1/translations/group} (OpenAPI component
 * {@code GetGroupTranslations.GetGroupTranslationsResponse}).
 */
public final class GroupTranslationsResponse extends AbstractTranslationBundlePayload {

  /**
   * @param project project key
   * @param lang language ISO code for the bundle
   * @param version server bundle version
   * @param generatedAt generation timestamp
   * @param entries translation entries (values may be strings or structured plural payloads)
   * @param entryContext optional per-entry context maps, or {@code null}
   */
  @JsonCreator
  public GroupTranslationsResponse(
      @JsonProperty("project") String project,
      @JsonProperty("lang") String lang,
      @JsonProperty("version") int version,
      @JsonProperty("generatedAt") Instant generatedAt,
      @JsonProperty("entries") Map<String, JsonNode> entries,
      @JsonProperty("entryContext") Map<String, Map<String, String>> entryContext) {
    super(project, lang, version, generatedAt, entries, entryContext);
  }
}
