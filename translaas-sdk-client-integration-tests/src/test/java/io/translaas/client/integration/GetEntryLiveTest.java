package io.translaas.client.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.translaas.client.TranslaasClient;
import io.translaas.models.exception.TranslaasApiException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Live API tests for {@link io.translaas.client.TranslaasClient#getEntry}. */
@EnabledIfEnvironmentVariable(named = "TRANSLAAS_API_KEY", matches = ".+")
class GetEntryLiveTest extends LiveIntegrationTestSupport {

  @Test
  void getEntry_returnsTranslationWhenEntryExists() {
    if (!configuration.isEnabled()) {
      return;
    }

    String result;
    try {
      result =
          await(
              client.getEntry(
                  IntegrationTestFixtures.SIMPLE_GROUP,
                  IntegrationTestFixtures.SIMPLE_ENTRY,
                  IntegrationTestFixtures.DEFAULT_LANGUAGE));
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
  void getEntry_returnsTranslationWithPluralization() {
    if (!configuration.isEnabled()) {
      return;
    }

    String result;
    try {
      result =
          await(
              client.getEntry(
                  IntegrationTestFixtures.PLURAL_GROUP,
                  IntegrationTestFixtures.PLURAL_ENTRY,
                  IntegrationTestFixtures.DEFAULT_LANGUAGE,
                  BigDecimal.valueOf(5),
                  null,
                  null));
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

  @Test
  void getEntry_handlesNotFoundWhenEntryNotFound() {
    if (!configuration.isEnabled()) {
      return;
    }

    try {
      String result =
          await(
              client.getEntry(
                  "nonexistent", "nonexistent.entry", IntegrationTestFixtures.DEFAULT_LANGUAGE));
      assertThat(result).isEqualTo("nonexistent.entry");
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipOnUnreachableApi(ex)) {
        return;
      }
      assertThat(IntegrationTestHelpers.isSdkNotFound(ex)).isTrue();
    }
  }

  @Test
  void getEntry_throwsWhenInvalidApiKey() {
    if (!configuration.isEnabled()) {
      return;
    }

    TranslaasClient invalidClient = newClient("invalid-api-key");

    Throwable thrown =
        catchThrowable(
            () ->
                await(
                    invalidClient.getEntry(
                        IntegrationTestFixtures.SIMPLE_GROUP,
                        IntegrationTestFixtures.SIMPLE_ENTRY,
                        IntegrationTestFixtures.DEFAULT_LANGUAGE)));
    thrown = unwrapCompletion(thrown);
    if (thrown instanceof TranslaasApiException) {
      TranslaasApiException ex = (TranslaasApiException) thrown;
      if (IntegrationTestHelpers.softSkipOnUnreachableApi(ex)) {
        return;
      }
    }
    assertThat(thrown).isInstanceOf(TranslaasApiException.class);
  }
}
