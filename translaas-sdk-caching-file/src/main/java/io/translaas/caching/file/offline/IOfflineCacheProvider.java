package io.translaas.caching.file.offline;

import io.translaas.models.GroupTranslationsResponse;
import io.translaas.models.ProjectLocalesResponse;
import io.translaas.models.ProjectTranslationsResponse;
import java.util.Map;
import java.util.Optional;

/** Semantic offline cache API (spec section 7.6 on-disk layout). */
public interface IOfflineCacheProvider {

  Optional<ProjectTranslationsResponse> getProject(String project, String lang);

  Optional<GroupTranslationsResponse> getGroup(String project, String group, String lang);

  Optional<ProjectLocalesResponse> getProjectLocales(String project);

  void saveProject(String project, String lang, ProjectTranslationsResponse data);

  void saveProjectLocales(String project, ProjectLocalesResponse locales);

  boolean isCached(String project, String lang);

  void clearAll();

  void clearProject(String project);

  CacheManifest getManifest();

  void applyOfflineBundle(
      String project,
      ProjectLocalesResponse locales,
      Map<String, ProjectTranslationsResponse> projectsByLang);
}
