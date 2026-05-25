package io.translaas.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.translaas.models.GroupTranslationsResponse;
import io.translaas.models.ProjectGroupPayload;
import io.translaas.models.ProjectTranslationsResponse;
import io.translaas.models.exception.TranslaasApiException;
import io.translaas.models.json.TranslaasJson;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Parses SDK translation JSON with parity to {@code sdkParsing.ts} (envelope, bare maps, flat-json).
 */
public final class TranslationResponseParsing {

  private static final Set<String> PROJECT_ROOT_METADATA =
      Set.of("project", "lang", "version", "generatedAt", "groupEntryContext", "groups", "entries", "entryContext");

  private static final ObjectMapper MAPPER = TranslaasJson.mapper();

  private TranslationResponseParsing() {}

  public static GroupTranslationsResponse parseGroupResponse(String body, String formatHint)
      throws TranslaasApiException {
    try {
      JsonNode root = MAPPER.readTree(body == null || body.isBlank() ? "{}" : body);
      if (!root.isObject()) {
        throw parseError("group", "expected object");
      }
      if (root.has("entries") && root.get("entries").isObject()) {
        return MAPPER.treeToValue(root, GroupTranslationsResponse.class);
      }
      Map<String, JsonNode> entries = new LinkedHashMap<>();
      Iterator<Map.Entry<String, JsonNode>> it = root.fields();
      while (it.hasNext()) {
        Map.Entry<String, JsonNode> e = it.next();
        entries.put(e.getKey(), e.getValue());
      }
      return new GroupTranslationsResponse(null, null, 0, Instant.EPOCH, entries, null, null);
    } catch (IOException e) {
      throw new TranslaasApiException(0, null, "Failed to parse group translations JSON", e);
    }
  }

  public static ProjectTranslationsResponse parseProjectResponse(String body, String formatHint)
      throws TranslaasApiException {
    try {
      JsonNode root = MAPPER.readTree(body == null || body.isBlank() ? "{}" : body);
      if (!root.isObject()) {
        throw parseError("project", "expected object");
      }
      if (root.has("groups") && root.get("groups").isObject()) {
        return MAPPER.treeToValue(root, ProjectTranslationsResponse.class);
      }
      if (root.has("entries") && root.get("entries").isObject()) {
        return MAPPER.treeToValue(root, ProjectTranslationsResponse.class);
      }
      if (FORMAT_FLAT_JSON.equals(formatHint) && isLikelyFlatCompositeProjectShape(root)) {
        return flatCompositeToProject(root);
      }
      if (isLikelyFlatCompositeProjectShape(root)) {
        return flatCompositeToProject(root);
      }
      return MAPPER.treeToValue(root, ProjectTranslationsResponse.class);
    } catch (IOException e) {
      throw new TranslaasApiException(0, null, "Failed to parse project translations JSON", e);
    }
  }

  private static final String FORMAT_FLAT_JSON = TranslaasClient.FORMAT_FLAT_JSON;

  private static ProjectTranslationsResponse flatCompositeToProject(JsonNode root) {
    Map<String, Map<String, JsonNode>> nested = new LinkedHashMap<>();
    Iterator<Map.Entry<String, JsonNode>> it = root.fields();
    while (it.hasNext()) {
      Map.Entry<String, JsonNode> e = it.next();
      if (PROJECT_ROOT_METADATA.contains(e.getKey())) {
        continue;
      }
      if (!e.getValue().isTextual()) {
        continue;
      }
      int dot = e.getKey().indexOf('.');
      if (dot <= 0) {
        continue;
      }
      String groupName = e.getKey().substring(0, dot);
      String entryName = e.getKey().substring(dot + 1);
      nested.computeIfAbsent(groupName, g -> new LinkedHashMap<>()).put(entryName, e.getValue());
    }
    Map<String, ProjectGroupPayload> groups = new LinkedHashMap<>();
    for (Map.Entry<String, Map<String, JsonNode>> g : nested.entrySet()) {
      groups.put(g.getKey(), new ProjectGroupPayload(g.getValue(), null, null));
    }
    String project = textOrNull(root, "project");
    String lang = textOrNull(root, "lang");
    int version = root.has("version") ? root.get("version").asInt(0) : 0;
    Instant generatedAt =
        root.has("generatedAt")
            ? Instant.parse(root.get("generatedAt").asText())
            : Instant.EPOCH;
    return new ProjectTranslationsResponse(
        project, lang, version, generatedAt, groups, null, null, null);
  }

  private static boolean isLikelyFlatCompositeProjectShape(JsonNode root) {
    boolean any = false;
    Iterator<Map.Entry<String, JsonNode>> it = root.fields();
    while (it.hasNext()) {
      Map.Entry<String, JsonNode> e = it.next();
      if (PROJECT_ROOT_METADATA.contains(e.getKey())) {
        continue;
      }
      any = true;
      if (!e.getKey().contains(".") || !e.getValue().isTextual()) {
        return false;
      }
    }
    return any;
  }

  private static String textOrNull(JsonNode root, String field) {
    return root.has(field) && root.get(field).isTextual() ? root.get(field).asText() : null;
  }

  private static TranslaasApiException parseError(String endpoint, String detail) {
    return new TranslaasApiException(0, null, "Invalid " + endpoint + " response: " + detail);
  }
}
