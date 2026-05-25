package io.translaas.client.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TranslaasApiErrorMessagesTest {

  @Test
  void formatsCodeAndMessageFromJson() {
    String msg =
        TranslaasApiErrorMessages.fromStatusAndBody(
            400, "{\"code\":\"VALIDATION\",\"message\":\"bad request\"}", "http://x");
    assertThat(msg).isEqualTo("[VALIDATION] bad request");
  }

  @Test
  void fallsBackToHttpStatusWhenBodyEmpty() {
    assertThat(TranslaasApiErrorMessages.fromStatusAndBody(500, null, "http://x"))
        .isEqualTo("HTTP 500 http://x");
  }

  @Test
  void formatsTitleAndDetailFromProblemDetails() {
    String msg =
        TranslaasApiErrorMessages.fromStatusAndBody(
            404, "{\"title\":\"Not Found\",\"detail\":\"missing\"}", "http://x");
    assertThat(msg).isEqualTo("Not Found: missing");
  }

  @Test
  void returnsMessageOnlyWhenCodeAbsent() {
    assertThat(
            TranslaasApiErrorMessages.fromStatusAndBody(
                400, "{\"message\":\"only message\"}", "http://x"))
        .isEqualTo("only message");
  }

  @Test
  void ignoresNonObjectJson() {
    assertThat(TranslaasApiErrorMessages.fromStatusAndBody(400, "[1,2]", "http://x"))
        .isEqualTo("HTTP 400 http://x");
  }
}
