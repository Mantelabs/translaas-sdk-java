package io.translaas.client.integration;

import io.translaas.TranslaasOptions;
import io.translaas.TranslaasService;
import io.translaas.client.TranslaasClient;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Builds configured clients for live API tests. Tests are gated by {@code TRANSLAAS_API_KEY}; when
 * unset, callers should return early without failing the build.
 */
class LiveIntegrationTestSupport {

  protected final IntegrationTestConfiguration configuration = new IntegrationTestConfiguration();
  protected final TranslaasClient client;

  LiveIntegrationTestSupport() {
    if (!configuration.isEnabled()) {
      client = null;
      return;
    }
    client =
        new TranslaasClient(
            io.translaas.client.TranslaasOptions.builder()
                .apiKey(configuration.getApiKey())
                .baseUrl(configuration.getBaseUrl())
                .defaultProject(configuration.getDefaultProject())
                .timeout(Duration.ofSeconds(30))
                .build());
  }

  /** Unwraps {@link CompletionException} from {@link CompletableFuture#join()} for typed handling. */
  protected static <T> T await(CompletableFuture<T> future) {
    try {
      return future.join();
    } catch (CompletionException ex) {
      Throwable cause = ex.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      if (cause instanceof Error) {
        throw (Error) cause;
      }
      throw ex;
    }
  }

  protected static Throwable unwrapCompletion(Throwable throwable) {
    if (throwable instanceof CompletionException) {
      CompletionException completion = (CompletionException) throwable;
      if (completion.getCause() != null) {
        return completion.getCause();
      }
    }
    return throwable;
  }

  protected TranslaasClient newClient(String apiKey) {
    return new TranslaasClient(
        io.translaas.client.TranslaasOptions.builder()
            .apiKey(apiKey)
            .baseUrl(configuration.getBaseUrl())
            .defaultProject(configuration.getDefaultProject())
            .timeout(Duration.ofSeconds(30))
            .build());
  }

  protected TranslaasClient newClientWithTimeout(Duration timeout) {
    return new TranslaasClient(
        io.translaas.client.TranslaasOptions.builder()
            .apiKey(configuration.getApiKey())
            .baseUrl(configuration.getBaseUrl())
            .defaultProject(configuration.getDefaultProject())
            .timeout(timeout)
            .build());
  }

  protected TranslaasClient newClientWithBaseUrl(String baseUrl) {
    return new TranslaasClient(
        io.translaas.client.TranslaasOptions.builder()
            .apiKey(configuration.getApiKey())
            .baseUrl(java.net.URI.create(baseUrl))
            .defaultProject(configuration.getDefaultProject())
            .timeout(Duration.ofSeconds(30))
            .build());
  }

  protected TranslaasService newService() {
    return new TranslaasService(
        TranslaasOptions.builder()
            .apiKey(configuration.getApiKey())
            .baseUrl(configuration.getBaseUrl())
            .defaultProject(configuration.getDefaultProject())
            .timeout(Duration.ofSeconds(30))
            .build());
  }
}
