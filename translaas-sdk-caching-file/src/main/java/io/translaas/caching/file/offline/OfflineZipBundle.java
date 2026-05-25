package io.translaas.caching.file.offline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.translaas.models.ProjectLocalesResponse;
import io.translaas.models.ProjectTranslationsResponse;
import io.translaas.models.json.TranslaasJson;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Parses offline translation ZIP bundles (HTTP spec section 7.6). */
public final class OfflineZipBundle {

  private static final ObjectMapper MAPPER = TranslaasJson.mapper();

  private OfflineZipBundle() {}

  public static OfflineBundle parseOfflineZip(byte[] content) throws IOException {
    if (content == null || content.length == 0) {
      throw new IllegalArgumentException("ZIP content is empty");
    }
    Map<String, Object> manifest = new LinkedHashMap<>();
    Map<String, ProjectLocalesResponse> localesByProject = new LinkedHashMap<>();
    Map<String, Map<String, ProjectTranslationsResponse>> projects = new LinkedHashMap<>();

    try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }
        String name = entry.getName();
        byte[] raw = zip.readAllBytes();
        if ("manifest.json".equals(name)) {
          manifest = readManifest(raw);
          continue;
        }
        String[] parts = name.split("/");
        if (parts.length < 2) {
          continue;
        }
        String projectSegment = parts[0];
        String fileName = parts[parts.length - 1];
        if ("locales.json".equals(fileName) && parts.length == 2) {
          ProjectLocalesResponse locales = parseLocalesWrapper(raw);
          if (locales != null) {
            localesByProject.put(projectSegment, locales);
          }
        } else if ("project.json".equals(fileName) && parts.length == 3) {
          String langSegment = parts[1];
          ProjectTranslationsResponse project = parseProjectWrapper(raw);
          if (project != null) {
            projects.computeIfAbsent(projectSegment, k -> new LinkedHashMap<>()).put(langSegment, project);
          }
        }
      }
    }
    return new OfflineBundle(manifest, localesByProject, projects);
  }

  public static String resolveProjectKey(OfflineBundle bundle, String project) {
    String sanitized = ProjectSanitizer.sanitize(project);
    if (bundle.getProjectsByProjectLang().containsKey(sanitized)
        || bundle.getLocalesByProject().containsKey(sanitized)) {
      return sanitized;
    }
    if (bundle.getProjectsByProjectLang().containsKey(project)
        || bundle.getLocalesByProject().containsKey(project)) {
      return project;
    }
    Object manifestProjects = bundle.getManifest().get("projects");
    if (manifestProjects instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) manifestProjects;
      if (map.containsKey(sanitized)) {
        return sanitized;
      }
      if (map.containsKey(project)) {
        return project;
      }
    }
    return sanitized;
  }

  private static Map<String, Object> readManifest(byte[] raw) throws IOException {
    JsonNode node = MAPPER.readTree(raw);
    if (!node.isObject()) {
      throw new IOException("Expected JSON object in manifest.json");
    }
    return MAPPER.convertValue(node, Map.class);
  }

  private static ProjectLocalesResponse parseLocalesWrapper(byte[] raw) throws IOException {
    CachedLocales cached = MAPPER.readValue(raw, CachedLocales.class);
    return cached != null ? cached.getData() : null;
  }

  private static ProjectTranslationsResponse parseProjectWrapper(byte[] raw) throws IOException {
    CachedProject cached = MAPPER.readValue(raw, CachedProject.class);
    return cached != null ? cached.getData() : null;
  }
}
