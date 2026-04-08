package io.mantelabs.translaas.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.Map;

/**
 * Response body for {@code GET /sdk/v1/translations/project}.
 *
 * <p>The server returns either a <strong>nested</strong> layout ({@code groups}) or a
 * <strong>flat-json</strong> layout ({@code format=flat-json}) with composite keys {@code
 * group.entry} in {@link #getEntries() entries}.
 */
public final class ProjectTranslationsResponse {

  private final String project;
  private final String lang;
  private final int version;
  private final Instant generatedAt;
  private final Map<String, ProjectGroupPayload> groups;
  private final Map<String, JsonNode> entries;
  private final Map<String, Map<String, String>> entryContext;
  private final Map<String, Map<String, String>> groupEntryContext;

  /**
   * @param groups nested bundles keyed by group name (default project format)
   * @param entries flat composite-key map when {@code format=flat-json}
   * @param entryContext optional contexts for flat layout, or {@code null}
   * @param groupEntryContext optional group-level context for flat layout, or {@code null}
   */
  @JsonCreator
  public ProjectTranslationsResponse(
      @JsonProperty("project") String project,
      @JsonProperty("lang") String lang,
      @JsonProperty("version") int version,
      @JsonProperty("generatedAt") Instant generatedAt,
      @JsonProperty("groups") Map<String, ProjectGroupPayload> groups,
      @JsonProperty("entries") Map<String, JsonNode> entries,
      @JsonProperty("entryContext") Map<String, Map<String, String>> entryContext,
      @JsonProperty("groupEntryContext") Map<String, Map<String, String>> groupEntryContext) {
    this.project = project;
    this.lang = lang;
    this.version = version;
    this.generatedAt = generatedAt;
    this.groups = groups;
    this.entries = entries;
    this.entryContext = entryContext;
    this.groupEntryContext = groupEntryContext;
  }

  public String getProject() {
    return project;
  }

  public String getLang() {
    return lang;
  }

  public int getVersion() {
    return version;
  }

  public Instant getGeneratedAt() {
    return generatedAt;
  }

  /** @return nested group bundles, or {@code null} when the response uses flat-json */
  public Map<String, ProjectGroupPayload> getGroups() {
    return groups;
  }

  /**
   * @return flat composite-key map ({@code group.entry}) for {@code format=flat-json}, or {@code
   *     null} for nested layout
   */
  public Map<String, JsonNode> getEntries() {
    return entries;
  }

  public Map<String, Map<String, String>> getEntryContext() {
    return entryContext;
  }

  public Map<String, Map<String, String>> getGroupEntryContext() {
    return groupEntryContext;
  }
}
