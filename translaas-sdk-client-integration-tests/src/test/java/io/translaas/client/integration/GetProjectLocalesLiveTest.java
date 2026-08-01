package io.translaas.client.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.translaas.client.TranslaasClient;
import io.translaas.models.ProjectLocalesResponse;
import io.translaas.models.exception.TranslaasApiException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Live API tests for {@link TranslaasClient#getProjectLocales}. */
@EnabledIfEnvironmentVariable(named = "TRANSLAAS_API_KEY", matches = ".+")
class GetProjectLocalesLiveTest extends LiveIntegrationTestSupport {

  private static final List<String> COMMON_LOCALES = List.of("en", "fr", "es", "de");

  @Test
  void getProjectLocales_returnsLocalesWhenProjectExists() {
    if (!configuration.isEnabled()) {
      return;
    }

    ProjectLocalesResponse result;
    try {
      result = await(client.getProjectLocales(configuration.getDefaultProject()));
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipLiveApiFailure(ex)) {
        return;
      }
      throw ex;
    }

    assertThat(result).isNotNull();
    if (IntegrationTestHelpers.softSkipIf(
        result.getLocales() == null || result.getLocales().isEmpty(),
        "fixture data not available in API")) {
      return;
    }

    assertThat(result.getLocales()).isNotEmpty();
  }

  @Test
  void getProjectLocales_returnsMultipleLocalesWhenProjectHasMultipleLocales() {
    if (!configuration.isEnabled()) {
      return;
    }

    ProjectLocalesResponse result;
    try {
      result = await(client.getProjectLocales(configuration.getDefaultProject()));
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipLiveApiFailure(ex)) {
        return;
      }
      throw ex;
    }

    assertThat(result).isNotNull();
    if (IntegrationTestHelpers.softSkipIf(
        result.getLocales() == null || result.getLocales().isEmpty(),
        "fixture data not available in API")) {
      return;
    }

    boolean hasCommonLocale =
        result.getLocales().stream().anyMatch(COMMON_LOCALES::contains);
    if (IntegrationTestHelpers.softSkipIf(
        !hasCommonLocale, "expected at least one common locale in fixture API")) {
      return;
    }

    assertThat(hasCommonLocale).isTrue();
  }

  @Test
  void getProjectLocales_handlesNotFoundWhenProjectNotFound() {
    if (!configuration.isEnabled()) {
      return;
    }

    try {
      ProjectLocalesResponse result = await(client.getProjectLocales("nonexistent-project"));
      assertThat(result).isNotNull();
      assertThat(result.getLocales()).isEmpty();
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipOnUnreachableApi(ex)) {
        return;
      }
      assertThat(IntegrationTestHelpers.isSdkNotFound(ex)).isTrue();
    }
  }
}
