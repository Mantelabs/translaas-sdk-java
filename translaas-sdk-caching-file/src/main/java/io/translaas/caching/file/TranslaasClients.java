package io.translaas.caching.file;

import io.translaas.client.OfflineCacheOptions;
import io.translaas.client.TranslaasClient;
import io.translaas.client.TranslaasOptions;
import io.translaas.client.TranslaasTranslationClient;
import io.translaas.caching.file.offline.IOfflineCacheProvider;
import io.translaas.caching.file.offline.SpecFileCacheProvider;
import io.translaas.models.exception.TranslaasConfigurationException;
import java.util.Objects;

/** Factory for HTTP clients with optional offline decoration. */
public final class TranslaasClients {

  private TranslaasClients() {}

  public static TranslaasTranslationClient create(TranslaasOptions options) {
    Objects.requireNonNull(options, "options");
    TranslaasClient inner = new TranslaasClient(options);
    OfflineCacheOptions offline = options.getOfflineCache();
    if (offline == null || !offline.isEnabled()) {
      return inner;
    }
    String projectId = offline.getDefaultProjectId();
    if (projectId == null || projectId.isBlank()) {
      projectId = options.getDefaultProject().orElse(null);
    }
    if (projectId == null || projectId.isBlank()) {
      throw new TranslaasConfigurationException(
          "offlineCache.defaultProjectId or defaultProject is required when offline cache is enabled");
    }
    IOfflineCacheProvider fileProvider = new SpecFileCacheProvider(offline);
    return new CachingTranslaasClient(inner, fileProvider, offline, projectId);
  }

  public static SpecFileCacheProvider createOfflineCacheProvider(OfflineCacheOptions offline) {
    return new SpecFileCacheProvider(offline);
  }
}
