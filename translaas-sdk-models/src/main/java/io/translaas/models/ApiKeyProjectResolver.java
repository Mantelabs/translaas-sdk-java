package io.translaas.models;

import io.translaas.models.exception.TranslaasConfigurationException;
import java.util.List;

/** Resolves default project ids from validate API key responses. */
public final class ApiKeyProjectResolver {

  private ApiKeyProjectResolver() {}

  /**
   * Resolves the effective default project when the caller did not configure one explicitly.
   *
   * @param configuredProjectId configured default project id, or {@code null}
   * @param validate validate API key response
   * @return resolved project id or slug
   */
  public static String resolveDefaultProjectId(
      String configuredProjectId, ValidateApiKeyResponse validate) {
    if (configuredProjectId != null && !configuredProjectId.isBlank()) {
      return configuredProjectId.trim();
    }

    List<String> projectIds =
        validate.getProjectIds() == null ? List.of() : validate.getProjectIds();
    if (projectIds.isEmpty()) {
      throw new TranslaasConfigurationException(
          "Tenant-level API key requires defaultProject in SDK configuration.");
    }

    String fromValidate = firstNonBlank(validate.getDefaultProjectId(), validate.getProjectId());
    if (fromValidate != null) {
      return fromValidate;
    }

    String first = projectIds.get(0);
    if (first == null || first.isBlank()) {
      throw new TranslaasConfigurationException(
          "Could not resolve a default project from the validate API key response.");
    }
    return first.trim();
  }

  private static String firstNonBlank(String first, String second) {
    if (first != null && !first.isBlank()) {
      return first.trim();
    }
    if (second != null && !second.isBlank()) {
      return second.trim();
    }
    return null;
  }
}
