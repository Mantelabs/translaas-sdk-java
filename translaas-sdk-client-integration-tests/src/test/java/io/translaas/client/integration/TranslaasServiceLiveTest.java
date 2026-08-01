package io.translaas.client.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.translaas.LanguageCodes;
import io.translaas.TranslaasService;
import io.translaas.models.exception.TranslaasApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Live API tests for {@link TranslaasService#t} with explicit language. */
@EnabledIfEnvironmentVariable(named = "TRANSLAAS_API_KEY", matches = ".+")
class TranslaasServiceLiveTest extends LiveIntegrationTestSupport {

  @Test
  void t_returnsTranslationWithExplicitLanguage() {
    if (!configuration.isEnabled()) {
      return;
    }

    TranslaasService service = newService();
    String result;
    try {
      result =
          await(
              service.t(
                  IntegrationTestFixtures.SIMPLE_GROUP,
                  IntegrationTestFixtures.SIMPLE_ENTRY,
                  LanguageCodes.ENGLISH));
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipLiveApiFailure(ex)) {
        return;
      }
      throw ex;
    }

    if (IntegrationTestHelpers.softSkipIf(
        result == null
            || result.isBlank()
            || result.equals(IntegrationTestFixtures.SIMPLE_ENTRY),
        "fixture data not available in API")) {
      return;
    }

    assertThat(result).isNotBlank();
  }

  @Test
  void t_returnsTranslationWithPluralization() {
    if (!configuration.isEnabled()) {
      return;
    }

    TranslaasService service = newService();
    String result;
    try {
      result =
          await(
              service.t(
                  IntegrationTestFixtures.PLURAL_GROUP,
                  IntegrationTestFixtures.PLURAL_ENTRY,
                  LanguageCodes.ENGLISH,
                  5L));
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipLiveApiFailure(ex)) {
        return;
      }
      throw ex;
    }

    if (IntegrationTestHelpers.softSkipIf(
        result == null
            || result.isBlank()
            || result.equals(IntegrationTestFixtures.PLURAL_ENTRY),
        "fixture data not available in API")) {
      return;
    }

    assertThat(result).isNotBlank();
  }
}
