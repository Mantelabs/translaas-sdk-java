package io.mantelabs.translaas.caching.file.offline;

/** Sanitizes project and language ids for use as filesystem segments. */
public final class ProjectSanitizer {

  private static final String INVALID = "<>:\"/\\|?*";

  private ProjectSanitizer() {}

  public static String sanitize(String projectId) {
    if (projectId == null) {
      return "";
    }
    StringBuilder out = new StringBuilder(projectId.length());
    for (int i = 0; i < projectId.length(); i++) {
      char ch = projectId.charAt(i);
      out.append(INVALID.indexOf(ch) >= 0 ? '_' : ch);
    }
    return out.toString();
  }
}
