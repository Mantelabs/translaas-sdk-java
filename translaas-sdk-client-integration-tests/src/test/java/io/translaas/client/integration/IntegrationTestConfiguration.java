package io.translaas.client.integration;

import java.net.URI;

/**
 * Configuration for live API integration tests. Reads {@code TRANSLAAS_API_KEY}, {@code
 * TRANSLAAS_BASE_URL}, and {@code TRANSLAAS_DEFAULT_PROJECT} from the environment.
 */
final class IntegrationTestConfiguration {

  private static final String DEFAULT_BASE_URL = "https://api.translaas.local";

  private final String apiKey;
  private final String baseUrl;
  private final String defaultProject;
  private final boolean enabled;

  IntegrationTestConfiguration() {
    apiKey = trimToNull(System.getenv("TRANSLAAS_API_KEY"));
    String configuredBaseUrl = trimToNull(System.getenv("TRANSLAAS_BASE_URL"));
    baseUrl = configuredBaseUrl != null ? configuredBaseUrl : DEFAULT_BASE_URL;
    String configuredProject = trimToNull(System.getenv("TRANSLAAS_DEFAULT_PROJECT"));
    defaultProject =
        configuredProject != null ? configuredProject : IntegrationTestFixtures.DEFAULT_PROJECT;
    enabled = apiKey != null;
  }

  String getApiKey() {
    return apiKey;
  }

  URI getBaseUrl() {
    return URI.create(baseUrl);
  }

  String getDefaultProject() {
    return defaultProject;
  }

  boolean isEnabled() {
    return enabled;
  }

  private static String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
