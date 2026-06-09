package io.translaas.client;

import java.net.http.HttpClient;
import java.time.Duration;

/** Builds JDK {@link HttpClient} instances from {@link TranslaasOptions}. */
public final class JdkHttpClientFactory {

  private JdkHttpClientFactory() {}

  public static HttpClient create(TranslaasOptions options) {
    HttpClient.Builder builder = HttpClient.newBuilder();
    Duration timeout = options.getTimeout();
    if (timeout != null) {
      builder.connectTimeout(timeout);
    }
    if (options.isPreferHttp11()) {
      builder.version(HttpClient.Version.HTTP_1_1);
    }
    return builder.build();
  }
}
