package io.translaas.client.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.translaas.client.TranslaasClient;
import io.translaas.client.TranslaasOptions;
import io.translaas.models.ValidateApiKeyResponse;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Live smoke test against a real Translaas API. Skipped unless both {@code TRANSLAAS_BASE_URL} and
 * {@code TRANSLAAS_API_KEY} are set (never commit secrets; use env or CI secrets only).
 */
@EnabledIfEnvironmentVariable(named = "TRANSLAAS_BASE_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "TRANSLAAS_API_KEY", matches = ".+")
class ValidateApiKeyLiveTest {

  @Test
  void validateApiKey_returnsParsableResponse() {
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey(System.getenv("TRANSLAAS_API_KEY"))
            .baseUrl(URI.create(System.getenv("TRANSLAAS_BASE_URL")))
            .build();
    TranslaasClient client = new TranslaasClient(options);

    ValidateApiKeyResponse response = client.validateApiKey().join();

    assertThat(response).isNotNull();
    assertThat(response.isValid())
        .as("TRANSLAAS_API_KEY must be accepted by the server for this integration check")
        .isTrue();
  }
}
