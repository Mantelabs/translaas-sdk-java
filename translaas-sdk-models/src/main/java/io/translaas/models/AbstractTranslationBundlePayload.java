package io.translaas.models;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.Map;

/**
 * Shared fields for group-scoped translation bundle JSON (OpenAPI {@code
 * GetGroupTranslationsResponse}).
 */
abstract class AbstractTranslationBundlePayload {

  private final String project;
  private final String lang;
  private final int version;
  private final Instant generatedAt;
  private final Map<String, JsonNode> entries;
  private final Map<String, Map<String, String>> entryContext;
  private final Map<String, Map<String, String>> groupEntryContext;

  protected AbstractTranslationBundlePayload(
      String project,
      String lang,
      int version,
      Instant generatedAt,
      Map<String, JsonNode> entries,
      Map<String, Map<String, String>> entryContext,
      Map<String, Map<String, String>> groupEntryContext) {
    this.project = project;
    this.lang = lang;
    this.version = version;
    this.generatedAt = generatedAt;
    this.entries = entries;
    this.entryContext = entryContext;
    this.groupEntryContext = groupEntryContext;
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

  /**
   * @return optional group-level entry context (when {@code includeContext} is enabled), or {@code
   *     null} if absent
   */
  public Map<String, Map<String, String>> getGroupEntryContext() {
    return groupEntryContext;
  }
}
