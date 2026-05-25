package io.translaas.models;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/** Factory methods for empty SDK translation responses (.NET parity on 204/304). */
public final class TranslationResponses {

  private TranslationResponses() {}

  public static GroupTranslationsResponse emptyGroup(String project, String group, String lang) {
    return new GroupTranslationsResponse(
        project, lang, 0, Instant.EPOCH, Collections.emptyMap(), null, null);
  }

  public static ProjectTranslationsResponse emptyProject(String project, String lang) {
    return new ProjectTranslationsResponse(
        project, lang, 0, Instant.EPOCH, Collections.emptyMap(), null, null, null);
  }

  public static ProjectLocalesResponse emptyLocales(String project) {
    return new ProjectLocalesResponse(project, List.of(), null);
  }
}
