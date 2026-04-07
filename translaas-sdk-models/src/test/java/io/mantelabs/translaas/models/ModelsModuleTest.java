package io.mantelabs.translaas.models;

import static org.assertj.core.api.Assertions.assertThat;

import io.mantelabs.translaas.models.exception.TranslaasApiException;
import io.mantelabs.translaas.models.json.TranslaasJson;
import org.junit.jupiter.api.Test;

class ModelsModuleTest {

  @Test
  void sharedJsonMapper_isAvailable() {
    assertThat(TranslaasJson.mapper()).isNotNull();
  }

  @Test
  void translaasApiException_exposesStatusAndSnippet() {
    TranslaasApiException ex = new TranslaasApiException(401, "{\"detail\":\"x\"}", "unauthorized");
    assertThat(ex.getHttpStatus()).isEqualTo(401);
    assertThat(ex.getResponseBodySnippet().orElseThrow()).contains("\"detail\"");
  }
}
