package io.mantelabs.translaas.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.mantelabs.translaas.TranslaasService;
import io.mantelabs.translaas.thymeleaf.TranslaasDialect;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

@SpringBootTest(classes = TranslaasThymeleafAutoConfigurationIT.MinimalApp.class)
class TranslaasThymeleafAutoConfigurationIT {

  @Autowired private SpringTemplateEngine templateEngine;

  @MockBean private TranslaasService translaasService;

  @Test
  void autoConfiguredEngine_rendersTranslaasTextTag() {
    when(translaasService.t("common", "welcome")).thenReturn(CompletableFuture.completedFuture("Hi from mock"));

    String template =
        "<!DOCTYPE html><html xmlns:translaas=\"" + TranslaasDialect.NAMESPACE_URI + "\">"
            + "<body><span><translaas:text group=\"common\" entry=\"welcome\"/></span></body></html>";

    String html = templateEngine.process(template, new Context());
    assertThat(html).contains("Hi from mock");
    verify(translaasService).t("common", "welcome");
  }

  @Test
  void autoConfiguredEngine_passesLangPluralAndJsonParamsToService() {
    when(translaasService.t(
            eq("g"), eq("e"), eq("fr"), eq(BigDecimal.valueOf(2)), eq(Map.of("k", "v"))))
        .thenReturn(CompletableFuture.completedFuture("Plural OK"));

    String template =
        "<!DOCTYPE html><html xmlns:translaas=\"" + TranslaasDialect.NAMESPACE_URI + "\">"
            + "<body><translaas:text group=\"g\" entry=\"e\" lang=\"fr\" number=\"2\" params='{\"k\":\"v\"}'/></body></html>";

    String html = templateEngine.process(template, new Context());
    assertThat(html).contains("Plural OK");
    verify(translaasService)
        .t(eq("g"), eq("e"), eq("fr"), eq(BigDecimal.valueOf(2)), eq(Map.of("k", "v")));
  }

  @Configuration
  @EnableAutoConfiguration
  static class MinimalApp {}
}
