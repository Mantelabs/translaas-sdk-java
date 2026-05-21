package io.mantelabs.translaas.client.http;

import com.fasterxml.jackson.databind.JsonNode;
import io.mantelabs.translaas.models.json.TranslaasJson;
import java.util.Optional;

/** Builds human-readable API error messages from HTTP status and JSON bodies. */
final class TranslaasApiErrorMessages {

  private TranslaasApiErrorMessages() {}

  static String fromStatusAndBody(int statusCode, String bodySnippet, String fallbackUri) {
    String fromJson = messageFromJson(bodySnippet);
    if (fromJson != null) {
      return fromJson;
    }
    return "HTTP " + statusCode + " " + fallbackUri;
  }

  private static String messageFromJson(String bodySnippet) {
    if (bodySnippet == null || bodySnippet.isBlank()) {
      return null;
    }
    try {
      JsonNode root = TranslaasJson.mapper().readTree(bodySnippet);
      if (!root.isObject()) {
        return null;
      }
      String message =
          Optional.ofNullable(root.get("message"))
              .filter(JsonNode::isTextual)
              .map(JsonNode::asText)
              .orElse(null);
      String code =
          Optional.ofNullable(root.get("code"))
              .filter(JsonNode::isTextual)
              .map(JsonNode::asText)
              .orElse(null);
      if (message == null && code == null) {
        String title =
            Optional.ofNullable(root.get("title"))
                .filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .orElse(null);
        String detail =
            Optional.ofNullable(root.get("detail"))
                .filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .orElse(null);
        if (title != null || detail != null) {
          if (title != null && detail != null) {
            return title + ": " + detail;
          }
          return title != null ? title : detail;
        }
        return null;
      }
      if (code != null && !code.isBlank()) {
        return "[" + code + "] " + (message != null ? message : "");
      }
      return message;
    } catch (Exception e) {
      return null;
    }
  }
}
