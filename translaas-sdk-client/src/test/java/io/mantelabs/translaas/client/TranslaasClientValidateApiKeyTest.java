package io.mantelabs.translaas.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.mantelabs.translaas.models.ValidateApiKeyResponse;
import io.mantelabs.translaas.models.exception.TranslaasApiException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

@WireMockTest
class TranslaasClientValidateApiKeyTest {

  @Test
  void validateApiKey_requestsApiV1Path_notSdkPrefix(WireMockRuntimeInfo wm) {
    String json =
        "{"
            + "\"isValid\":true,"
            + "\"tenantId\":\"01HZYD8YJ8K9QWERTY1234567\","
            + "\"projectId\":\"01HZYD8YJ8K9QWERTY7654321\","
            + "\"integrationName\":\"ci\","
            + "\"authenticatedAt\":\"2026-04-07T12:47:01Z\""
            + "}";

    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.API_KEYS_VALIDATE_PATH))
                .withHeader(TranslaasOptions.DEFAULT_API_KEY_HEADER, equalTo("test-key"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(json)));

    TranslaasClient client = clientForPort(wm.getHttpPort());
    ValidateApiKeyResponse r = client.validateApiKey().join();

    assertThat(r.isValid()).isTrue();
    assertThat(r.getTenantId()).isEqualTo("01HZYD8YJ8K9QWERTY1234567");
    assertThat(r.getProjectId()).isEqualTo("01HZYD8YJ8K9QWERTY7654321");
    assertThat(r.getIntegrationName()).isEqualTo("ci");
    assertThat(r.getAuthenticatedAt()).isEqualTo(Instant.parse("2026-04-07T12:47:01Z"));
  }

  @Test
  void validateApiKey_throwsTranslaasApiException_when401(WireMockRuntimeInfo wm) {
    wm.getWireMock()
        .register(
            get(urlPathEqualTo(TranslaasClient.API_KEYS_VALIDATE_PATH))
                .willReturn(aResponse().withStatus(401).withBody("unauthorized")));

    TranslaasClient client = clientForPort(wm.getHttpPort());

    assertThatThrownBy(() -> client.validateApiKey().join())
        .hasCauseInstanceOf(TranslaasApiException.class)
        .satisfies(
            t -> assertThat(((TranslaasApiException) t.getCause()).getHttpStatus()).isEqualTo(401));
  }

  private static TranslaasClient clientForPort(int port) {
    TranslaasOptions options =
        TranslaasOptions.builder()
            .apiKey("test-key")
            .baseUrl(TestApiUrls.httpOrigin(port))
            .build();
    return new TranslaasClient(options);
  }
}
