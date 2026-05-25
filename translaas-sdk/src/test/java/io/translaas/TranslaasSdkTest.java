package io.translaas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TranslaasSdkTest {

  @Test
  void aggregatorModuleLoads() {
    assertThat(TranslaasSdk.class.getName()).isEqualTo("io.translaas.TranslaasSdk");
  }
}
