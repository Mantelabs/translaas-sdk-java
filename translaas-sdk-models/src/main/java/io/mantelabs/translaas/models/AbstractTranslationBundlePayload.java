package io.mantelabs.translaas.models;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.Map;

/**
 * Shared fields for group- and project-scoped translation bundle JSON (OpenAPI
 * {@code GetGroupTranslationsResponse}; project route is not yet modeled separately in
 * {@code api-specs.json} but matches this shape at runtime).
 */
abstract class AbstractTranslationBundlePayload {

  private final String project;
  private final String lang;
  private final int version;
  private final Instant generatedAt;
  private final Map<String, JsonNode> entries;
  private final Map<String, Map<String, String>> entryContext;

  protected AbstractTranslationBundlePayload(
      String project,
      String lang,
      int version,
      Instant generatedAt,
      Map<String, JsonNode> entries,
      Map<String, Map<String, String>> entryContext) {
    this.project = project;
    this.lang = lang;
    this.version = version;
    this.generatedAt = generatedAt;
    this.entries = entries;
    this.entryContext = entryContext;
  }

  /** @return project key from the response */
  public String getProject() {
    return project;
  }

  /** @return language code for this bundle */
  public String getLang() {
    return lang;
  }

  /** @return bundle version counter from the server */
  public int getVersion() {
    return version;
  }

  /** @return when this bundle was generated */
  public Instant getGeneratedAt() {
    return generatedAt;
  }

  /**
   * @return map of entry key to JSON value (shape depends on pluralization and value type)
   */
  public Map<String, JsonNode> getEntries() {
    return entries;
  }

  /**
   * @return optional nested context strings keyed by entry key, or {@code null} if absent
   */
  public Map<String, Map<String, String>> getEntryContext() {
    return entryContext;
  }
}
