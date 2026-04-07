package io.mantelabs.translaas.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.Map;

/**
 * Response body for {@code GET /sdk/v1/translations/project}. The OpenAPI snapshot in
 * {@code api-specs.json} does not yet declare a schema for this response; this type matches the
 * group bundle shape and should be updated if the documented schema diverges.
 */
public final class ProjectTranslationsResponse extends AbstractTranslationBundlePayload {

  /**
   * @param project project key
   * @param lang language ISO code for the bundle
   * @param version server bundle version
   * @param generatedAt generation timestamp
   * @param entries translation entries (values may be strings or structured plural payloads)
   * @param entryContext optional per-entry context maps, or {@code null}
   */
  @JsonCreator
  public ProjectTranslationsResponse(
      @JsonProperty("project") String project,
      @JsonProperty("lang") String lang,
      @JsonProperty("version") int version,
      @JsonProperty("generatedAt") Instant generatedAt,
      @JsonProperty("entries") Map<String, JsonNode> entries,
      @JsonProperty("entryContext") Map<String, Map<String, String>> entryContext) {
    super(project, lang, version, generatedAt, entries, entryContext);
  }
}
