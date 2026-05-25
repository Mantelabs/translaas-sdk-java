package io.translaas.caching.file.offline;

import io.translaas.models.ProjectLocalesResponse;
import io.translaas.models.ProjectTranslationsResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/** Parsed offline ZIP contents. */
public final class OfflineBundle {

  private final Map<String, Object> manifest;
  private final Map<String, ProjectLocalesResponse> localesByProject;
  private final Map<String, Map<String, ProjectTranslationsResponse>> projectsByProjectLang;

  public OfflineBundle(
      Map<String, Object> manifest,
      Map<String, ProjectLocalesResponse> localesByProject,
      Map<String, Map<String, ProjectTranslationsResponse>> projectsByProjectLang) {
    this.manifest = manifest != null ? new LinkedHashMap<>(manifest) : new LinkedHashMap<>();
    this.localesByProject =
        localesByProject != null ? new LinkedHashMap<>(localesByProject) : new LinkedHashMap<>();
    this.projectsByProjectLang =
        projectsByProjectLang != null
            ? new LinkedHashMap<>(projectsByProjectLang)
            : new LinkedHashMap<>();
  }

  public Map<String, Object> getManifest() {
    return manifest;
  }

  public Map<String, ProjectLocalesResponse> getLocalesByProject() {
    return localesByProject;
  }

  public Map<String, Map<String, ProjectTranslationsResponse>> getProjectsByProjectLang() {
    return projectsByProjectLang;
  }
}
