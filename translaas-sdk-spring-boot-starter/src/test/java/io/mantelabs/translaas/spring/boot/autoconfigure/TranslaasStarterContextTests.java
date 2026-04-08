package io.mantelabs.translaas.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.mantelabs.translaas.TranslaasService;
import io.mantelabs.translaas.client.TranslaasClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = TranslaasStarterContextTests.MinimalBootApp.class)
class TranslaasStarterContextTests {

  @Autowired private TranslaasClient translaasClient;
  @Autowired private TranslaasService translaasService;

  @Test
  void contextLoads_andExposesClientAndService() {
    assertThat(translaasClient).isNotNull();
    assertThat(translaasService).isNotNull();
    assertThat(translaasService.getClient()).isSameAs(translaasClient);
  }

  @Configuration
  @EnableAutoConfiguration
  static class MinimalBootApp {}
}
