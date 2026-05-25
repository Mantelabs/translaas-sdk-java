package io.translaas.client;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Extracts {@code filename} / {@code filename*} from an HTTP {@code Content-Disposition} header.
 */
final class ContentDispositionFilenames {

  private ContentDispositionFilenames() {}

  /**
   * @param contentDisposition raw header value, or {@code null}
   * @return decoded filename when present, otherwise {@code null}
   */
  static String parseFilename(String contentDisposition) {
    if (contentDisposition == null || contentDisposition.isBlank()) {
      return null;
    }
    String star = extractFilenameStar(contentDisposition);
    if (star != null) {
      return star;
    }
    return extractFilenameQuotedOrBare(contentDisposition);
  }

  private static String extractFilenameStar(String header) {
    int idx = header.indexOf("filename*=");
    if (idx < 0) {
      return null;
    }
    String rest = header.substring(idx + "filename*=".length()).trim();
    int tick = rest.indexOf("''");
    if (tick < 0) {
      return null;
    }
    String encoded = rest.substring(tick + 2);
    int semi = indexOfOutsideQuotes(encoded, ';');
    if (semi >= 0) {
      encoded = encoded.substring(0, semi);
    }
    encoded = encoded.trim();
    if (encoded.isEmpty()) {
      return null;
    }
    return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
  }

  private static String extractFilenameQuotedOrBare(String header) {
    int idx = header.indexOf("filename=");
    if (idx < 0) {
      return null;
    }
    String rest = header.substring(idx + "filename=".length()).trim();
    if (rest.startsWith("\"")) {
      int end = rest.indexOf('"', 1);
      if (end > 1) {
        return rest.substring(1, end);
      }
      return null;
    }
    int semi = indexOfOutsideQuotes(rest, ';');
    return semi < 0 ? rest : rest.substring(0, semi).trim();
  }

  private static int indexOfOutsideQuotes(String s, char ch) {
    boolean inQuotes = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '"') {
        inQuotes = !inQuotes;
      } else if (c == ch && !inQuotes) {
        return i;
      }
    }
    return -1;
  }
}
