package io.translaas.client.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.translaas.client.TranslaasClient;
import io.translaas.models.ProjectTranslationsResponse;
import io.translaas.models.exception.TranslaasApiException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Live API tests for {@link TranslaasClient#getProjectTranslations}. */
@EnabledIfEnvironmentVariable(named = "TRANSLAAS_API_KEY", matches = ".+")
class GetProjectTranslationsLiveTest extends LiveIntegrationTestSupport {

  private static final Set<String> ROOT_METADATA_KEYS =
      Set.of("project", "lang", "version", "generatedat", "groupentrycontext", "entries", "entrycontext");

  @Test
  void getProjectTranslations_returnsProjectWhenProjectExists() {
    if (!configuration.isEnabled()) {
      return;
    }

    ProjectTranslationsResponse result;
    try {
      result =
          await(
              client.getProjectTranslations(
                  configuration.getDefaultProject(), IntegrationTestFixtures.DEFAULT_LANGUAGE));
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipLiveApiFailure(ex)) {
        return;
      }
      throw ex;
    }

    assertThat(result).isNotNull();
    if (IntegrationTestHelpers.softSkipIf(
        result.getGroups() == null || result.getGroups().isEmpty(),
        "fixture data not available in API")) {
      return;
    }

    assertThat(result.getGroups()).isNotEmpty();
    assertGroupsExcludeRootMetadata(result);
  }

  @Test
  void getProjectTranslations_returnsProjectWithFormat() {
    if (!configuration.isEnabled()) {
      return;
    }

    ProjectTranslationsResponse result;
    try {
      result =
          await(
              client.getProjectTranslations(
                  configuration.getDefaultProject(),
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
        result.getGroups() == null || result.getGroups().isEmpty(),
        "fixture data not available in API")) {
      return;
    }

    assertThat(result.getGroups()).isNotEmpty();
  }

  @Test
  void getProjectTranslations_handlesNotFoundWhenProjectNotFound() {
    if (!configuration.isEnabled()) {
      return;
    }

    try {
      ProjectTranslationsResponse result =
          await(
              client.getProjectTranslations(
                  "nonexistent-project", IntegrationTestFixtures.DEFAULT_LANGUAGE));
      assertThat(result).isNotNull();
      assertThat(result.getGroups()).isEmpty();
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipOnUnreachableApi(ex)) {
        return;
      }
      assertThat(IntegrationTestHelpers.isSdkNotFound(ex)).isTrue();
    }
  }

  @Test
  void getProjectTranslations_containsMultipleGroupsWhenProjectHasMultipleGroups() {
    if (!configuration.isEnabled()) {
      return;
    }

    ProjectTranslationsResponse result;
    try {
      result =
          await(
              client.getProjectTranslations(
                  configuration.getDefaultProject(), IntegrationTestFixtures.DEFAULT_LANGUAGE));
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipLiveApiFailure(ex)) {
        return;
      }
      throw ex;
    }

    assertThat(result).isNotNull();
    if (IntegrationTestHelpers.softSkipIf(
        result.getGroups() == null || result.getGroups().isEmpty(),
        "fixture data not available in API")) {
      return;
    }

    int walked = 0;
    for (var groupEntry : result.getGroups().entrySet()) {
      if (groupEntry.getValue().getEntries() == null || groupEntry.getValue().getEntries().isEmpty()) {
        continue;
      }
      assertThat(groupEntry.getValue().getEntries()).isNotEmpty();
      walked++;
    }

    if (IntegrationTestHelpers.softSkipIf(walked == 0, "fixture data not available in API")) {
      return;
    }
  }

  private static void assertGroupsExcludeRootMetadata(ProjectTranslationsResponse result) {
    for (String groupName : result.getGroups().keySet()) {
      assertThat(ROOT_METADATA_KEYS).doesNotContain(groupName.toLowerCase());
    }
  }
}
