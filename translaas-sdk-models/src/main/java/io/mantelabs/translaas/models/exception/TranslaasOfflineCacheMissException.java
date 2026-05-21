package io.mantelabs.translaas.models.exception;

import java.util.Optional;

/** Thrown when an offline cache lookup misses in cache-only or fallback paths. */
public class TranslaasOfflineCacheMissException extends TranslaasException {

  private final String project;
  private final String lang;
  private final String group;
  private final String entry;

  public TranslaasOfflineCacheMissException(
      String message, String project, String lang, String group, String entry) {
    super(message);
    this.project = project;
    this.lang = lang;
    this.group = group;
    this.entry = entry;
  }

  public static TranslaasOfflineCacheMissException forEntry(
      String project, String lang, String group, String entry) {
    return new TranslaasOfflineCacheMissException(
        "Translation entry '"
            + entry
            + "' in group '"
            + group
            + "' for project '"
            + project
            + "' and language '"
            + lang
            + "' was not found in the offline cache.",
        project,
        lang,
        group,
        entry);
  }

  public static TranslaasOfflineCacheMissException forGroup(
      String project, String lang, String group) {
    return new TranslaasOfflineCacheMissException(
        "Translation group '"
            + group
            + "' for project '"
            + project
            + "' and language '"
            + lang
            + "' was not found in the offline cache.",
        project,
        lang,
        group,
        null);
  }

  public static TranslaasOfflineCacheMissException forProject(String project, String lang) {
    return new TranslaasOfflineCacheMissException(
        "Project '" + project + "' for language '" + lang + "' was not found in the offline cache.",
        project,
        lang,
        null,
        null);
  }

  public Optional<String> getProject() {
    return Optional.ofNullable(project);
  }

  public Optional<String> getLang() {
    return Optional.ofNullable(lang);
  }

  public Optional<String> getGroup() {
    return Optional.ofNullable(group);
  }

  public Optional<String> getEntry() {
    return Optional.ofNullable(entry);
  }
}
