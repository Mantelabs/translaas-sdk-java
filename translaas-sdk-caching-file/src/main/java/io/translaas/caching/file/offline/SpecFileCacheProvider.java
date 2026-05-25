package io.translaas.caching.file.offline;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.translaas.client.OfflineCacheOptions;
import io.translaas.models.GroupTranslationsResponse;
import io.translaas.models.ProjectGroupPayload;
import io.translaas.models.ProjectLocalesResponse;
import io.translaas.models.ProjectTranslationsResponse;
import io.translaas.models.exception.TranslaasOfflineCacheException;
import io.translaas.models.json.TranslaasJson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Offline cache using {@code manifest.json} and per-project locale trees (HTTP spec section 7.6). */
public final class SpecFileCacheProvider implements IOfflineCacheProvider {

  private static final String MANIFEST_FILE = "manifest.json";
  private static final String LOCALES_FILE = "locales.json";
  private static final String PROJECT_FILE = "project.json";
  private static final String SDK_VERSION = "0.3.0-beta";

  private static final ObjectMapper MAPPER = TranslaasJson.mapper();

  private final Path cacheDirectory;

  public SpecFileCacheProvider(OfflineCacheOptions options) {
    this(resolveDirectory(options.getCacheDirectory()));
  }

  public SpecFileCacheProvider(String cacheDirectory) {
    this(resolveDirectory(cacheDirectory));
  }

  public SpecFileCacheProvider(Path cacheDirectory) {
    this.cacheDirectory = cacheDirectory;
    ensureDirectory();
  }

  public Path cacheDirectory() {
    return cacheDirectory;
  }

  @Override
  public Optional<ProjectTranslationsResponse> getProject(String project, String lang) {
    validateProjectLang(project, lang);
    Path path = projectFilePath(project, lang);
    CachedProject cached = readWrapper(path, CachedProject.class, true);
    if (cached == null || cached.getData() == null) {
      return Optional.empty();
    }
    if (isExpired(cached.getExpiresAt())) {
      deleteQuietly(path);
      return Optional.empty();
    }
    return Optional.of(cached.getData());
  }

  @Override
  public Optional<GroupTranslationsResponse> getGroup(String project, String group, String lang) {
    if (group == null || group.isBlank()) {
      throw new IllegalArgumentException("group is required");
    }
    return getProject(project, lang)
        .flatMap(
            p -> {
              ProjectGroupPayload payload =
                  p.getGroups() != null ? p.getGroups().get(group) : null;
              if (payload == null) {
                return Optional.empty();
              }
              return Optional.of(
                  new GroupTranslationsResponse(
                      p.getProject(),
                      p.getLang(),
                      p.getVersion(),
                      p.getGeneratedAt(),
                      payload.getEntries(),
                      payload.getEntryContext(),
                      payload.getGroupEntryContext()));
            });
  }

  @Override
  public Optional<ProjectLocalesResponse> getProjectLocales(String project) {
    if (project == null || project.isBlank()) {
      throw new IllegalArgumentException("project is required");
    }
    CachedLocales cached = readWrapper(localesFilePath(project), CachedLocales.class, false);
    if (cached == null || cached.getData() == null || isExpired(cached.getExpiresAt())) {
      return Optional.empty();
    }
    return Optional.of(cached.getData());
  }

  @Override
  public void saveProject(String project, String lang, ProjectTranslationsResponse data) {
    validateProjectLang(project, lang);
    Path path = projectFilePath(project, lang);
    Map<String, Object> wrapper = new LinkedHashMap<>();
    wrapper.put("cachedAt", Instant.now().toString());
    wrapper.put("expiresAt", null);
    wrapper.put("data", OfflineProjectJson.toStorageNode(data));
    writeJsonAtomic(path, wrapper);
    updateManifest(project, lang, CacheSyncStatus.SYNCED);
  }

  @Override
  public void saveProjectLocales(String project, ProjectLocalesResponse locales) {
    if (project == null || project.isBlank()) {
      throw new IllegalArgumentException("project is required");
    }
    writeJsonAtomic(localesFilePath(project), new CachedLocales(locales));
  }

  @Override
  public boolean isCached(String project, String lang) {
    return getProject(project, lang).isPresent();
  }

  @Override
  public void clearAll() {
    try {
      if (Files.exists(cacheDirectory)) {
        Files.walk(cacheDirectory)
            .sorted((a, b) -> b.compareTo(a))
            .filter(p -> !p.equals(cacheDirectory))
            .forEach(this::deleteQuietly);
      }
      ensureDirectory();
    } catch (IOException e) {
      throw new TranslaasOfflineCacheException("Failed to clear offline cache", e);
    }
  }

  @Override
  public void clearProject(String project) {
    if (project == null || project.isBlank()) {
      throw new IllegalArgumentException("project is required");
    }
    deleteQuietly(projectDirectory(project));
    CacheManifest manifest = getManifest();
    String sanitized = ProjectSanitizer.sanitize(project);
    manifest.getProjects().remove(sanitized);
    manifest.getProjects().remove(project);
    writeJsonAtomic(manifestPath(), manifest);
  }

  @Override
  public CacheManifest getManifest() {
    Path path = manifestPath();
    if (!Files.exists(path)) {
      return CacheManifest.empty(SDK_VERSION);
    }
    try {
      return MAPPER.readValue(path.toFile(), CacheManifest.class);
    } catch (IOException e) {
      return CacheManifest.empty(SDK_VERSION);
    }
  }

  @Override
  public void applyOfflineBundle(
      String project,
      ProjectLocalesResponse locales,
      Map<String, ProjectTranslationsResponse> projectsByLang) {
    if (locales != null) {
      saveProjectLocales(project, locales);
    }
    if (projectsByLang != null) {
      for (Map.Entry<String, ProjectTranslationsResponse> e : projectsByLang.entrySet()) {
        saveProject(project, e.getKey(), e.getValue());
      }
    }
  }

  private void updateManifest(String project, String lang, CacheSyncStatus status) {
    CacheManifest manifest = getManifest();
    String key = ProjectSanitizer.sanitize(project);
    ProjectCacheInfo existing = manifest.getProjects().get(key);
    java.util.List<String> languages =
        existing != null
            ? new java.util.ArrayList<>(existing.getLanguages())
            : new java.util.ArrayList<>();
    if (!languages.contains(lang)) {
      languages.add(lang);
    }
    manifest
        .getProjects()
        .put(
            key,
            new ProjectCacheInfo(languages, Instant.now().toString(), status.wireName()));
    writeJsonAtomic(manifestPath(), manifest);
  }

  private void writeJsonAtomic(Path path, Object value) {
    try {
      Files.createDirectories(path.getParent());
      Path temp = Path.of(path.toString() + ".tmp");
      MAPPER.writerWithDefaultPrettyPrinter().writeValue(temp.toFile(), value);
      Files.move(temp, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new TranslaasOfflineCacheException("Failed to write cache file " + path.getFileName(), e);
    }
  }

  private <T> T readWrapper(Path path, Class<T> type, boolean removeOnError) {
    if (!Files.exists(path)) {
      return null;
    }
    try {
      return MAPPER.readValue(path.toFile(), type);
    } catch (IOException e) {
      if (removeOnError) {
        deleteQuietly(path);
      }
      return null;
    }
  }

  private static boolean isExpired(String expiresAt) {
    if (expiresAt == null || expiresAt.isBlank()) {
      return false;
    }
    try {
      return Instant.now().isAfter(Instant.parse(expiresAt));
    } catch (DateTimeParseException e) {
      try {
        return Instant.now()
            .isAfter(Instant.parse(expiresAt.replace("Z", "+00:00")));
      } catch (DateTimeParseException ex) {
        return false;
      }
    }
  }

  private Path manifestPath() {
    return cacheDirectory.resolve(MANIFEST_FILE);
  }

  private Path projectDirectory(String project) {
    return cacheDirectory.resolve(ProjectSanitizer.sanitize(project));
  }

  private Path languageDirectory(String project, String lang) {
    return projectDirectory(project).resolve(ProjectSanitizer.sanitize(lang));
  }

  private Path projectFilePath(String project, String lang) {
    return languageDirectory(project, lang).resolve(PROJECT_FILE);
  }

  private Path localesFilePath(String project) {
    return projectDirectory(project).resolve(LOCALES_FILE);
  }

  private void ensureDirectory() {
    try {
      Files.createDirectories(cacheDirectory);
    } catch (IOException e) {
      throw new TranslaasOfflineCacheException("Failed to create cache directory", e);
    }
  }

  private static Path resolveDirectory(String directory) {
    Path path = Path.of(directory);
    if (!path.isAbsolute()) {
      path = Path.of("").toAbsolutePath().resolve(path);
    }
    return path.normalize();
  }

  private static void validateProjectLang(String project, String lang) {
    if (project == null || project.isBlank()) {
      throw new IllegalArgumentException("project is required");
    }
    if (lang == null || lang.isBlank()) {
      throw new IllegalArgumentException("lang is required");
    }
  }

  private void deleteQuietly(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException ignored) {
      // best effort
    }
  }
}
