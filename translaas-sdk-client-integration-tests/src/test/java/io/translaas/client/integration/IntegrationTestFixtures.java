package io.translaas.client.integration;

/**
 * Canonical fixture ids for live API integration tests. Aligned with <a
 * href="https://github.com/Mantelabs/translaas-sdk-examples">translaas-sdk-examples</a> ({@code
 * dotnet/docs/translaas_sdk_samples_strings.csv}) and local Mantelabs Docker seed data.
 */
final class IntegrationTestFixtures {

  /** Default project id for scoped reads (local Docker dogfoods this project). */
  static final String DEFAULT_PROJECT = "translaas-sdk-samples";

  /** Group for simple entry reads. */
  static final String SIMPLE_GROUP = "common";

  /** Simple entry key. */
  static final String SIMPLE_ENTRY = "welcome.message";

  /** Group for plural entry reads. */
  static final String PLURAL_GROUP = "messages";

  /** Plural entry key. */
  static final String PLURAL_ENTRY = "item";

  /** Default language code. */
  static final String DEFAULT_LANGUAGE = "en";

  private IntegrationTestFixtures() {}
}
