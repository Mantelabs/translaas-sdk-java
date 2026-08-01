package io.translaas.client.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.translaas.models.ValidateApiKeyResponse;
import io.translaas.models.exception.TranslaasApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Live smoke test against a real Translaas API. Skipped unless {@code TRANSLAAS_API_KEY} is set
 * (never commit secrets; use env or CI secrets only).
 */
@EnabledIfEnvironmentVariable(named = "TRANSLAAS_API_KEY", matches = ".+")
class ValidateApiKeyLiveTest extends LiveIntegrationTestSupport {

  @Test
  void validateApiKey_returnsParsableResponse() {
    if (!configuration.isEnabled()) {
      return;
    }

    ValidateApiKeyResponse response;
    try {
      response = await(client.validateApiKey());
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipLiveApiFailure(ex)) {
        return;
      }
      throw ex;
    }

    assertThat(response).isNotNull();
    assertThat(response.isValid())
        .as("TRANSLAAS_API_KEY must be accepted by the server for this integration check")
        .isTrue();
  }
}
