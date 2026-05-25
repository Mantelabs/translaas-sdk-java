package io.translaas.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

/**
 * One translation group inside a nested {@link ProjectTranslationsResponse#getGroups() project
 * bundle}.
 */
public final class ProjectGroupPayload {

  private final Map<String, JsonNode> entries;
  private final Map<String, Map<String, String>> entryContext;
  private final Map<String, Map<String, String>> groupEntryContext;

  /**
   * @param entries translation entries for this group
   * @param entryContext optional per-entry context maps, or {@code null}
   * @param groupEntryContext optional group-level context map, or {@code null}
   */
  @JsonCreator
  public ProjectGroupPayload(
      @JsonProperty("entries") Map<String, JsonNode> entries,
      @JsonProperty("entryContext") Map<String, Map<String, String>> entryContext,
      @JsonProperty("groupEntryContext") Map<String, Map<String, String>> groupEntryContext) {
    this.entries = entries;
    this.entryContext = entryContext;
    this.groupEntryContext = groupEntryContext;
  }

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
