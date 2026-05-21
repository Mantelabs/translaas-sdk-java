package io.mantelabs.translaas.caching.file.offline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Root {@code manifest.json} for the offline cache directory. */
public final class CacheManifest {

  public static final String MANIFEST_VERSION = "1.0";

  private final String version;
  private final String sdkVersion;
  private final String createdAt;
  private final String lastSyncAt;
  private final Map<String, ProjectCacheInfo> projects;

  @JsonCreator
  public CacheManifest(
      @JsonProperty("version") String version,
      @JsonProperty("sdkVersion") String sdkVersion,
      @JsonProperty("createdAt") String createdAt,
      @JsonProperty("lastSyncAt") String lastSyncAt,
      @JsonProperty("projects") Map<String, ProjectCacheInfo> projects) {
    this.version = version != null ? version : MANIFEST_VERSION;
    this.sdkVersion = sdkVersion != null ? sdkVersion : "";
    this.createdAt = createdAt != null ? createdAt : Instant.now().toString();
    this.lastSyncAt = lastSyncAt;
    this.projects = projects != null ? new LinkedHashMap<>(projects) : new LinkedHashMap<>();
  }

  public static CacheManifest empty(String sdkVersion) {
    return new CacheManifest(
        MANIFEST_VERSION, sdkVersion, Instant.now().toString(), null, new LinkedHashMap<>());
  }

  public String getVersion() {
    return version;
  }

  public String getSdkVersion() {
    return sdkVersion;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getLastSyncAt() {
    return lastSyncAt;
  }

  public Map<String, ProjectCacheInfo> getProjects() {
    return projects;
  }
}
