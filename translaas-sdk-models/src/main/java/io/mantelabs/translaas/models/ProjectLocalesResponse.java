package io.mantelabs.translaas.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

/**
 * Response body for {@code GET /sdk/v1/translations/locales} (OpenAPI component
 * {@code GetProjectLocales.GetProjectLocalesResponse}).
 */
public final class ProjectLocalesResponse {

  private final String project;
  private final List<String> locales;
  private final Instant lastModifiedUtc;

  /**
   * @param project project key, or {@code null} if not set
   * @param locales supported locale codes, or {@code null}
   * @param lastModifiedUtc last modification time for locale metadata, or {@code null}
   */
  @JsonCreator
  public ProjectLocalesResponse(
      @JsonProperty("project") String project,
      @JsonProperty("locales") List<String> locales,
      @JsonProperty("lastModifiedUtc") Instant lastModifiedUtc) {
    this.project = project;
    this.locales = locales;
    this.lastModifiedUtc = lastModifiedUtc;
  }

  /** @return project key from the response, or {@code null} */
  public String getProject() {
    return project;
  }

  /** @return locale codes, or {@code null} */
  public List<String> getLocales() {
    return locales;
  }

  /** @return last modification instant, or {@code null} */
  public Instant getLastModifiedUtc() {
    return lastModifiedUtc;
  }
}
