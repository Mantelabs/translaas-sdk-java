package io.mantelabs.translaas.caching.file.offline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.mantelabs.translaas.models.ProjectGroupPayload;
import io.mantelabs.translaas.models.ProjectTranslationsResponse;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Wrapper for {@code {project}/{lang}/project.json}. */
public final class CachedProject {

  private final String cachedAt;
  private final String expiresAt;
  private final ProjectTranslationsResponse data;

  @JsonCreator
  public CachedProject(
      @JsonProperty("cachedAt") String cachedAt,
      @JsonProperty("expiresAt") String expiresAt,
      @JsonProperty("data") JsonNode dataNode) {
    this.cachedAt = cachedAt != null ? cachedAt : Instant.now().toString();
    this.expiresAt = expiresAt;
    this.data = dataNode != null ? projectFromStorage(dataNode) : null;
  }

  public CachedProject(ProjectTranslationsResponse data) {
    this(Instant.now().toString(), null, data);
  }

  private CachedProject(String cachedAt, String expiresAt, ProjectTranslationsResponse data) {
    this.cachedAt = cachedAt;
    this.expiresAt = expiresAt;
    this.data = data;
  }

  public String getCachedAt() {
    return cachedAt;
  }

  public String getExpiresAt() {
    return expiresAt;
  }

  public ProjectTranslationsResponse getData() {
    return data;
  }

  public JsonNode toDataNode() {
    return OfflineProjectJson.toStorageNode(data);
  }

  private static ProjectTranslationsResponse projectFromStorage(JsonNode dataNode) {
    if (!dataNode.isObject()) {
      return null;
    }
    Map<String, ProjectGroupPayload> groups = new LinkedHashMap<>();
    Map<String, Map<String, String>> groupEntryContext = null;
    var fields = dataNode.fields();
    while (fields.hasNext()) {
      var e = fields.next();
      if ("entryContext".equals(e.getKey()) && e.getValue().isObject()) {
        groupEntryContext = OfflineProjectJson.readGroupEntryContext(e.getValue());
        continue;
      }
      if (e.getValue().isObject()) {
        groups.put(e.getKey(), OfflineProjectJson.groupFromNode(e.getValue()));
      }
    }
    return new ProjectTranslationsResponse(
        null, null, 0, Instant.EPOCH, groups, null, null, groupEntryContext);
  }
}
