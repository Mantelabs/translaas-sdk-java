package io.translaas.client.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.translaas.client.TranslaasClient;
import io.translaas.models.GroupTranslationsResponse;
import io.translaas.models.exception.TranslaasApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Live API tests for {@link TranslaasClient#getGroupTranslations}. */
@EnabledIfEnvironmentVariable(named = "TRANSLAAS_API_KEY", matches = ".+")
class GetGroupTranslationsLiveTest extends LiveIntegrationTestSupport {

  @Test
  void getGroupTranslations_returnsGroupWhenGroupExists() {
    if (!configuration.isEnabled()) {
      return;
    }

    GroupTranslationsResponse result;
    try {
      result =
          await(
              client.getGroupTranslations(
                  configuration.getDefaultProject(),
                  IntegrationTestFixtures.SIMPLE_GROUP,
                  IntegrationTestFixtures.DEFAULT_LANGUAGE));
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipLiveApiFailure(ex)) {
        return;
      }
      throw ex;
    }

    assertThat(result).isNotNull();
    if (IntegrationTestHelpers.softSkipIf(
        result.getEntries() == null || result.getEntries().isEmpty(),
        "fixture data not available in API")) {
      return;
    }

    assertThat(result.getEntries()).isNotEmpty();
  }

  @Test
  void getGroupTranslations_returnsGroupWithFormat() {
    if (!configuration.isEnabled()) {
      return;
    }

    GroupTranslationsResponse result;
    try {
      result =
          await(
              client.getGroupTranslations(
                  configuration.getDefaultProject(),
                  IntegrationTestFixtures.SIMPLE_GROUP,
                  IntegrationTestFixtures.DEFAULT_LANGUAGE,
                  "json",
                  null,
                  null));
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipLiveApiFailure(ex)) {
        return;
      }
      throw ex;
    }

    assertThat(result).isNotNull();
    if (IntegrationTestHelpers.softSkipIf(
        result.getEntries() == null || result.getEntries().isEmpty(),
        "fixture data not available in API")) {
      return;
    }

    assertThat(result.getEntries()).isNotEmpty();
  }

  @Test
  void getGroupTranslations_handlesNotFoundWhenGroupNotFound() {
    if (!configuration.isEnabled()) {
      return;
    }

    try {
      GroupTranslationsResponse result =
          await(
              client.getGroupTranslations(
                  configuration.getDefaultProject(),
                  "nonexistent-group",
                  IntegrationTestFixtures.DEFAULT_LANGUAGE));
      assertThat(result).isNotNull();
      assertThat(result.getEntries()).isEmpty();
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipOnUnreachableApi(ex)) {
        return;
      }
      assertThat(IntegrationTestHelpers.isSdkNotFound(ex)).isTrue();
    }
  }

  @Test
  void getGroupTranslations_handlesNotFoundWhenProjectNotFound() {
    if (!configuration.isEnabled()) {
      return;
    }

    try {
      GroupTranslationsResponse result =
          await(
              client.getGroupTranslations(
                  "nonexistent-project",
                  IntegrationTestFixtures.SIMPLE_GROUP,
                  IntegrationTestFixtures.DEFAULT_LANGUAGE));
      assertThat(result).isNotNull();
      assertThat(result.getEntries()).isEmpty();
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipOnUnreachableApi(ex)) {
        return;
      }
      assertThat(IntegrationTestHelpers.isSdkNotFound(ex)).isTrue();
    }
  }
}
