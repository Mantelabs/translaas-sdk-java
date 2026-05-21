package io.mantelabs.translaas.caching.file.offline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mantelabs.translaas.models.ProjectGroupPayload;
import io.mantelabs.translaas.models.ProjectTranslationsResponse;
import io.mantelabs.translaas.models.json.TranslaasJson;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** Serializes offline project bundles to/from on-disk JSON. */
final class OfflineProjectJson {

  private static final ObjectMapper MAPPER = TranslaasJson.mapper();

  private OfflineProjectJson() {}

  static JsonNode toStorageNode(ProjectTranslationsResponse project) {
    ObjectNode root = MAPPER.createObjectNode();
    if (project == null || project.getGroups() == null) {
      return root;
    }
    for (Map.Entry<String, ProjectGroupPayload> e : project.getGroups().entrySet()) {
      root.set(e.getKey(), entriesNode(e.getValue()));
    }
    if (project.getGroupEntryContext() != null) {
      root.set("entryContext", MAPPER.valueToTree(project.getGroupEntryContext()));
    }
    return root;
  }

  static ProjectGroupPayload groupFromNode(JsonNode groupNode) {
    Map<String, JsonNode> entries = new LinkedHashMap<>();
    Map<String, Map<String, String>> entryContext = null;
    Iterator<Map.Entry<String, JsonNode>> it = groupNode.fields();
    while (it.hasNext()) {
      Map.Entry<String, JsonNode> field = it.next();
      if ("entryContext".equals(field.getKey())) {
        entryContext = readEntryContext(field.getValue());
      } else {
        entries.put(field.getKey(), field.getValue());
      }
    }
    return new ProjectGroupPayload(entries, entryContext, null);
  }

  static Map<String, Map<String, String>> readGroupEntryContext(JsonNode node) {
    Map<String, Map<String, String>> out = new HashMap<>();
    if (!node.isObject()) {
      return out;
    }
    Iterator<Map.Entry<String, JsonNode>> it = node.fields();
    while (it.hasNext()) {
      Map.Entry<String, JsonNode> e = it.next();
      if (e.getValue().isObject()) {
        Map<String, String> ctx = new HashMap<>();
        e.getValue().fields().forEachRemaining(f -> ctx.put(f.getKey(), f.getValue().asText()));
        out.put(e.getKey(), ctx);
      }
    }
    return out;
  }

  private static Map<String, Map<String, String>> readEntryContext(JsonNode node) {
    Map<String, Map<String, String>> out = new HashMap<>();
    if (!node.isObject()) {
      return out;
    }
    node.fields()
        .forEachRemaining(
            e -> {
              if (e.getValue().isObject()) {
                Map<String, String> ctx = new HashMap<>();
                e.getValue()
                    .fields()
                    .forEachRemaining(f -> ctx.put(f.getKey(), f.getValue().asText()));
                out.put(e.getKey(), ctx);
              }
            });
    return out;
  }

  private static ObjectNode entriesNode(ProjectGroupPayload group) {
    ObjectNode node = MAPPER.createObjectNode();
    if (group.getEntries() != null) {
      group.getEntries().forEach(node::set);
    }
    if (group.getEntryContext() != null) {
      node.set("entryContext", MAPPER.valueToTree(group.getEntryContext()));
    }
    return node;
  }
}
