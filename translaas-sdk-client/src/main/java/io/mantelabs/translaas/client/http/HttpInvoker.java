package io.mantelabs.translaas.client.http;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Abstraction over {@link HttpClient#send(HttpRequest, HttpResponse.BodyHandler)} for tests and
 * custom wiring.
 */
@FunctionalInterface
public interface HttpInvoker {

  <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
      throws IOException, InterruptedException;

  static HttpInvoker from(HttpClient client) {
    return client::send;
  }
}
