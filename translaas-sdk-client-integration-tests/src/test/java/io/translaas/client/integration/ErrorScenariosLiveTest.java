package io.translaas.client.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.translaas.client.TranslaasClient;
import io.translaas.models.exception.TranslaasApiException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Live API error-scenario tests (invalid key, invalid URL, timeout, not found). */
@EnabledIfEnvironmentVariable(named = "TRANSLAAS_API_KEY", matches = ".+")
class ErrorScenariosLiveTest extends LiveIntegrationTestSupport {

  @Test
  void client_throwsWhenApiKeyIsInvalid() {
    if (!configuration.isEnabled()) {
      return;
    }

    TranslaasClient invalidClient = newClient("invalid-api-key-12345");

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
    TranslaasApiException exception = (TranslaasApiException) thrown;
    assertThat(exception.getHttpStatus()).isIn(401, 403);
  }

  @Test
  void client_throwsWhenBaseUrlIsInvalid() {
    if (!configuration.isEnabled()) {
      return;
    }

    TranslaasClient invalidClient =
        newClientWithBaseUrl("https://invalid-url-that-does-not-exist-12345.com");

    assertThatThrownBy(
            () ->
                await(
                    invalidClient.getEntry(
                        IntegrationTestFixtures.SIMPLE_GROUP,
                        IntegrationTestFixtures.SIMPLE_ENTRY,
                        IntegrationTestFixtures.DEFAULT_LANGUAGE)))
        .isInstanceOf(Exception.class);
  }

  @Test
  void client_throwsWhenRequestTimesOut() {
    if (!configuration.isEnabled()) {
      return;
    }

    TranslaasClient timeoutClient = newClientWithTimeout(Duration.ofMillis(1));

    Throwable thrown =
        catchThrowable(
            () ->
                await(
                    timeoutClient.getEntry(
                        IntegrationTestFixtures.SIMPLE_GROUP,
                        IntegrationTestFixtures.SIMPLE_ENTRY,
                        IntegrationTestFixtures.DEFAULT_LANGUAGE)));
    thrown = unwrapCompletion(thrown);
    assertThat(thrown).isInstanceOf(TranslaasApiException.class);
    TranslaasApiException exception = (TranslaasApiException) thrown;
    assertThat(exception.getHttpStatus()).isZero();
    assertThat(exception.getMessage()).containsIgnoringCase("HTTP");
  }

  @Test
  void client_handlesNotFoundWhenResourceDoesNotExist() {
    if (!configuration.isEnabled()) {
      return;
    }

    try {
      String result =
          await(client.getEntry("nonexistent-group", "nonexistent-entry", "nonexistent-lang"));
      assertThat(result).isEqualTo("nonexistent-entry");
    } catch (TranslaasApiException ex) {
      if (IntegrationTestHelpers.softSkipOnUnreachableApi(ex)) {
        return;
      }
      assertThat(IntegrationTestHelpers.isSdkNotFound(ex)).isTrue();
    }
  }
}
