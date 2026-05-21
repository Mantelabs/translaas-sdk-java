package io.mantelabs.translaas.spring.boot.autoconfigure;

import io.mantelabs.translaas.LanguageResolver;
import io.mantelabs.translaas.TranslaasService;
import io.mantelabs.translaas.caching.MemoryTranslaasCacheProvider;
import io.mantelabs.translaas.caching.TranslaasCacheProvider;
import io.mantelabs.translaas.caching.file.TranslaasClients;
import io.mantelabs.translaas.caching.file.offline.IOfflineCacheProvider;
import io.mantelabs.translaas.caching.file.offline.OfflineCacheSyncService;
import io.mantelabs.translaas.caching.file.offline.SpecFileCacheProvider;
import io.mantelabs.translaas.client.TranslaasClient;
import io.mantelabs.translaas.client.TranslaasTranslationClient;
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
  @ConditionalOnMissingBean(TranslaasTranslationClient.class)
  public TranslaasTranslationClient translaasTranslationClient(
      io.mantelabs.translaas.TranslaasOptions translaasOptions) {
    return TranslaasClients.create(translaasOptions.asClientOptions());
  }

  @Bean
  @ConditionalOnProperty(prefix = "translaas.offline", name = "enabled", havingValue = "true")
  @ConditionalOnMissingBean(IOfflineCacheProvider.class)
  public SpecFileCacheProvider translaasOfflineCacheProvider(
      io.mantelabs.translaas.TranslaasOptions translaasOptions) {
    return TranslaasClients.createOfflineCacheProvider(
        translaasOptions.asClientOptions().getOfflineCache());
  }

  @Bean
  @ConditionalOnProperty(prefix = "translaas.offline", name = "enabled", havingValue = "true")
  @ConditionalOnMissingBean(OfflineCacheSyncService.class)
  public OfflineCacheSyncService translaasOfflineCacheSyncService(
      TranslaasTranslationClient translaasTranslationClient,
      IOfflineCacheProvider offlineCacheProvider,
      io.mantelabs.translaas.TranslaasOptions translaasOptions) {
    OfflineCacheSyncService service =
        new OfflineCacheSyncService(
            translaasTranslationClient,
            offlineCacheProvider,
            translaasOptions.asClientOptions().getOfflineCache());
    if (translaasOptions.asClientOptions().getOfflineCache().isAutoSync()) {
      service.startBackgroundSync();
    }
    return service;
  }

  @Bean
  @ConditionalOnMissingBean(TranslaasService.class)
  public TranslaasService translaasService(
      TranslaasTranslationClient translaasTranslationClient,
      io.mantelabs.translaas.TranslaasOptions translaasOptions,
      ObjectProvider<LanguageResolver> languageResolvers) {
    List<LanguageResolver> list = new ArrayList<>();
    languageResolvers.orderedStream().forEach(list::add);
    return new TranslaasService(translaasTranslationClient, translaasOptions.asClientOptions(), list);
  }

  @Bean
  @ConditionalOnProperty(prefix = "translaas.locale", name = "use-spring-locale-context", havingValue = "true")
  public LanguageResolver translaasLocaleContextLanguageResolver() {
    return new TranslaasLanguageResolver();
  }
}
