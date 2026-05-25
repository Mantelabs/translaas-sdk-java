package io.translaas.spring.boot.autoconfigure;

import io.translaas.TranslaasService;
import io.translaas.thymeleaf.TranslaasDialect;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.SpringTemplateEngine;

/**
 * Registers {@link TranslaasDialect} on the auto-configured {@link SpringTemplateEngine}. Loads only when
 * both {@link TranslaasService} and Thymeleaf are present (add {@code translaas-sdk-thymeleaf-spring-boot-starter}).
 *
 * <p>Processors resolve text on the servlet request thread via {@link java.util.concurrent.CompletableFuture#join()}
 * on {@link TranslaasService}. Timeouts and API errors surface as template processing failures unless you handle them
 * in global exception handling; for a silent fallback, resolve in the controller or use {@code th:if} with an alternate
 * fragment.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SpringTemplateEngine.class)
@ConditionalOnBean(TranslaasService.class)
@ConditionalOnProperty(prefix = "translaas", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(ThymeleafAutoConfiguration.class)
public class TranslaasThymeleafAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(TranslaasDialect.class)
  public TranslaasDialect translaasDialect(TranslaasService translaasService) {
    return new TranslaasDialect(translaasService);
  }

  /**
   * Side-effect bean: adds {@link TranslaasDialect} to the auto-configured {@link SpringTemplateEngine} (runs after
   * {@link ThymeleafAutoConfiguration}).
   */
  @Bean
  @ConditionalOnBean({SpringTemplateEngine.class, TranslaasDialect.class})
  TranslaasDialectRegistration translaasDialectRegistration(
      SpringTemplateEngine templateEngine, TranslaasDialect dialect) {
    return new TranslaasDialectRegistration(templateEngine, dialect);
  }

  static final class TranslaasDialectRegistration {
    TranslaasDialectRegistration(SpringTemplateEngine templateEngine, TranslaasDialect dialect) {
      templateEngine.addDialect(dialect);
    }
  }
}
