package io.translaas.caching.file.offline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/** Per-project entry in {@code manifest.json}. */
public final class ProjectCacheInfo {

  private final List<String> languages;
  private final String lastSyncAt;
  private final CacheSyncStatus status;

  @JsonCreator
  public ProjectCacheInfo(
      @JsonProperty("languages") List<String> languages,
      @JsonProperty("lastSyncAt") String lastSyncAt,
      @JsonProperty("status") String status) {
    this.languages = languages != null ? new ArrayList<>(languages) : new ArrayList<>();
    this.lastSyncAt = lastSyncAt;
    this.status = CacheSyncStatus.fromWire(status);
  }

  public ProjectCacheInfo() {
    this(new ArrayList<>(), null, CacheSyncStatus.SYNCED.wireName());
  }

  public List<String> getLanguages() {
    return languages;
  }

  public String getLastSyncAt() {
    return lastSyncAt;
  }

  public CacheSyncStatus getStatus() {
    return status;
  }
}
