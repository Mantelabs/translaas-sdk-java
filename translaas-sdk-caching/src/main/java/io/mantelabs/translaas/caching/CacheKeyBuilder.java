package io.mantelabs.translaas.caching;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Builds consistent cache keys for Translaas translation data (parity with .NET {@code
 * CacheKeyBuilder}).
 */
public final class CacheKeyBuilder {

  private static final String SEP = ":";

  private CacheKeyBuilder() {}

  public static String buildEntryKey(
      String group,
      String entry,
      String lang,
      BigDecimal number,
      Map<String, String> parameters,
      String project,
      String channel,
      String version) {
    Objects.requireNonNull(group, "group");
    Objects.requireNonNull(entry, "entry");
    Objects.requireNonNull(lang, "lang");

    StringBuilder key = new StringBuilder("entry").append(SEP).append(group).append(SEP).append(entry).append(SEP).append(lang);

    if (number != null) {
      key.append(SEP).append(number.toPlainString());
    }

    if (parameters != null && !parameters.isEmpty()) {
      parameters.entrySet().stream()
          .filter(e -> e.getKey() != null && e.getValue() != null)
          .sorted(Comparator.comparing(e -> e.getKey().toLowerCase(Locale.ROOT)))
          .forEach(
              e ->
                  key.append(SEP)
                      .append(e.getKey().toLowerCase(Locale.ROOT))
                      .append("=")
                      .append(e.getValue()));
    }

    appendSnapshotSuffix(key, project, channel, version, null);
    return key.toString();
  }

  public static String buildGroupKey(
      String project,
      String group,
      String lang,
      String format,
      String channel,
      String version,
      Boolean includeContext) {
    Objects.requireNonNull(project, "project");
    Objects.requireNonNull(group, "group");
    Objects.requireNonNull(lang, "lang");

    StringBuilder key =
        new StringBuilder("group").append(SEP).append(project).append(SEP).append(group).append(SEP).append(lang);

    if (format != null && !format.isBlank()) {
      key.append(SEP).append(format);
    }

    appendSnapshotSuffix(key, null, channel, version, includeContext);
    return key.toString();
  }

  public static String buildProjectKey(
      String project,
      String lang,
      String format,
      String channel,
      String version,
      Boolean includeContext) {
    Objects.requireNonNull(project, "project");
    Objects.requireNonNull(lang, "lang");

    StringBuilder key = new StringBuilder("project").append(SEP).append(project).append(SEP).append(lang);

    if (format != null && !format.isBlank()) {
      key.append(SEP).append(format);
    }

    appendSnapshotSuffix(key, null, channel, version, includeContext);
    return key.toString();
  }

  public static String buildLocalesKey(String project, String channel, String version) {
    Objects.requireNonNull(project, "project");
    StringBuilder key = new StringBuilder("locales").append(SEP).append(project);
    appendSnapshotSuffix(key, null, channel, version, null);
    return key.toString();
  }

  public static String buildOfflineCacheKey(
      String project, String channel, String version, Boolean includeContext) {
    Objects.requireNonNull(project, "project");
    StringBuilder key = new StringBuilder("offline").append(SEP).append(project);
    appendSnapshotSuffix(key, null, channel, version, includeContext);
    return key.toString();
  }

  private static void appendSnapshotSuffix(
      StringBuilder key,
      String project,
      String channel,
      String version,
      Boolean includeContext) {
    if (project != null && !project.isBlank()) {
      key.append(SEP).append("proj=").append(project);
    }
    if (channel != null && !channel.isBlank()) {
      key.append(SEP).append("ch=").append(channel);
    }
    if (version != null && !version.isBlank()) {
      key.append(SEP).append("v=").append(version);
    }
    if (includeContext != null) {
      key.append(SEP).append(includeContext ? "ic=1" : "ic=0");
    }
  }
}
