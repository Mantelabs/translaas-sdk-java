package io.mantelabs.translaas.caching.file.offline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.mantelabs.translaas.models.ProjectLocalesResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Wrapper for {@code {project}/locales.json}. */
public final class CachedLocales {

  private final String cachedAt;
  private final String expiresAt;
  private final ProjectLocalesResponse data;

  @JsonCreator
  public CachedLocales(
      @JsonProperty("cachedAt") String cachedAt,
      @JsonProperty("expiresAt") String expiresAt,
      @JsonProperty("data") LocalesData data) {
    this.cachedAt = cachedAt != null ? cachedAt : Instant.now().toString();
    this.expiresAt = expiresAt;
    this.data =
        data != null
            ? new ProjectLocalesResponse(data.project, data.locales, data.lastModifiedUtc)
            : null;
  }

  public CachedLocales(ProjectLocalesResponse data) {
    this(
        Instant.now().toString(),
        null,
        new LocalesData(data.getProject(), data.getLocales(), data.getLastModifiedUtc()));
  }

  public String getCachedAt() {
    return cachedAt;
  }

  public String getExpiresAt() {
    return expiresAt;
  }

  public ProjectLocalesResponse getData() {
    return data;
  }

  public LocalesData toData() {
    if (data == null) {
      return null;
    }
    return new LocalesData(data.getProject(), data.getLocales(), data.getLastModifiedUtc());
  }

  public static final class LocalesData {
    public String project;
    public List<String> locales;
    public Instant lastModifiedUtc;

    @JsonCreator
    public LocalesData(
        @JsonProperty("project") String project,
        @JsonProperty("locales") List<String> locales,
        @JsonProperty("lastModifiedUtc") Instant lastModifiedUtc) {
      this.project = project;
      this.locales = locales != null ? new ArrayList<>(locales) : new ArrayList<>();
      this.lastModifiedUtc = lastModifiedUtc;
    }
  }
}
