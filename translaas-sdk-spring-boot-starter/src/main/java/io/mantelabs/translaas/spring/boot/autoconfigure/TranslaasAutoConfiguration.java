package io.mantelabs.translaas.spring.boot.autoconfigure;

import io.mantelabs.translaas.LanguageResolver;
import io.mantelabs.translaas.TranslaasService;
import io.mantelabs.translaas.caching.MemoryTranslaasCacheProvider;
import io.mantelabs.translaas.caching.TranslaasCacheProvider;
import io.mantelabs.translaas.client.TranslaasClient;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(TranslaasClient.class)
@EnableConfigurationProperties(TranslaasProperties.class)
@ConditionalOnProperty(prefix = "translaas", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TranslaasAutoConfiguration {

  @Bean
  @ConditionalOnProperty(prefix = "translaas.caching.memory", name = "enabled", havingValue = "true")
  @ConditionalOnMissingBean(TranslaasCacheProvider.class)
  public MemoryTranslaasCacheProvider translaasMemoryCacheProvider(TranslaasProperties properties) {
    return properties.newMemoryCacheProvider();
  }

  @Bean
  @ConditionalOnMissingBean(io.mantelabs.translaas.TranslaasOptions.class)
  public io.mantelabs.translaas.TranslaasOptions translaasOptions(
      TranslaasProperties properties, ObjectProvider<TranslaasCacheProvider> cacheProvider) {
    return properties.toFacadeOptions(cacheProvider.getIfAvailable());
  }

  @Bean
  @ConditionalOnMissingBean(TranslaasClient.class)
  public TranslaasClient translaasClient(io.mantelabs.translaas.TranslaasOptions translaasOptions) {
    return new TranslaasClient(translaasOptions.asClientOptions());
  }

  @Bean
  @ConditionalOnMissingBean(TranslaasService.class)
  public TranslaasService translaasService(
      TranslaasClient translaasClient,
      io.mantelabs.translaas.TranslaasOptions translaasOptions,
      ObjectProvider<LanguageResolver> languageResolvers) {
    List<LanguageResolver> list = new ArrayList<>();
    languageResolvers.orderedStream().forEach(list::add);
    return new TranslaasService(translaasClient, translaasOptions.asClientOptions(), list);
  }

  @Bean
  @ConditionalOnProperty(prefix = "translaas.locale", name = "use-spring-locale-context", havingValue = "true")
  public LanguageResolver translaasLocaleContextLanguageResolver() {
    return new TranslaasLanguageResolver();
  }
}
