package io.translaas.client.http;

import io.translaas.client.TranslaasOptions;
import io.translaas.client.TranslaasRequestContext;
import io.translaas.models.exception.TranslaasApiException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Generic HTTP transport: {@link HttpClient} wiring, URI building, API key header, conditional
 * request headers, and mapping failures to {@link TranslaasApiException}. Endpoint-specific paths
 * are supplied by callers as strings.
 */
public final class TranslaasHttp {

  private static final int BODY_SNIPPET_MAX = 2048;

  private final TranslaasOptions options;
  private final HttpInvoker httpInvoker;

  public TranslaasHttp(TranslaasOptions options) {
    this(options, HttpInvoker.from(newClient(options)));
  }

  /**
   * @param httpInvoker test double or custom sender; must honor {@link TranslaasOptions#getTimeout()}
   *     if wrapping the JDK client
   */
  public TranslaasHttp(TranslaasOptions options, HttpInvoker httpInvoker) {
    this.options = Objects.requireNonNull(options, "options");
    this.httpInvoker = Objects.requireNonNull(httpInvoker, "httpInvoker");
  }

  public TranslaasOptions getOptions() {
    return options;
  }

  private static HttpClient newClient(TranslaasOptions options) {
    return io.translaas.client.JdkHttpClientFactory.create(options);
  }

  /**
   * GET request: merges {@code query} with defaults from options and {@link TranslaasRequestContext}.
   */
  public HttpResponse<String> get(
      String path, Map<String, String> query, TranslaasRequestContext context)
      throws TranslaasApiException {
    return get(path, query, context, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  /**
   * GET request with a binary body (e.g. {@code application/zip}): same query merge and headers as
   * {@link #get(String, Map, TranslaasRequestContext)}.
   */
  public HttpResponse<byte[]> getBytes(
      String path, Map<String, String> query, TranslaasRequestContext context)
      throws TranslaasApiException {
    return get(path, query, context, HttpResponse.BodyHandlers.ofByteArray());
  }

  private <T> HttpResponse<T> get(
      String path,
      Map<String, String> query,
      TranslaasRequestContext context,
      HttpResponse.BodyHandler<T> bodyHandler)
      throws TranslaasApiException {
    LinkedHashMap<String, String> merged =
        TranslaasUris.mergeQueryParams(options, context, query);
    URI uri = TranslaasUris.buildUri(options.getBaseUrl(), path, merged);
    HttpRequest.Builder b = HttpRequest.newBuilder(uri).GET();
    applyHeaderPairs(b, collectHeaders(context));
    return sendRequest(b, context, bodyHandler);
  }

  /**
   * POST with a UTF-8 string body (e.g. JSON). Uses {@code Content-Type: application/json} when
   * {@code contentType} is null.
   */
  public HttpResponse<String> post(
      String path,
      Map<String, String> query,
      String body,
      String contentType,
      TranslaasRequestContext context)
      throws TranslaasApiException {
    LinkedHashMap<String, String> merged =
        TranslaasUris.mergeQueryParams(options, context, query);
    URI uri = TranslaasUris.buildUri(options.getBaseUrl(), path, merged);
    String ct = contentType != null ? contentType : "application/json";
    HttpRequest.Builder b =
        HttpRequest.newBuilder(uri)
            .POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8))
            .header("Content-Type", ct);
    applyHeaderPairs(b, collectHeaders(context));
    return sendRequest(b, context, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private <T> HttpResponse<T> sendRequest(
      HttpRequest.Builder requestBuilder,
      TranslaasRequestContext context,
      HttpResponse.BodyHandler<T> bodyHandler)
      throws TranslaasApiException {
    Duration timeout = options.getTimeout();
    if (timeout != null) {
      requestBuilder.timeout(timeout);
    }
    return send(requestBuilder.build(), context, bodyHandler);
  }

  private <T> HttpResponse<T> send(
      HttpRequest request, TranslaasRequestContext context, HttpResponse.BodyHandler<T> bodyHandler)
      throws TranslaasApiException {
    try {
      HttpResponse<T> response = httpInvoker.send(request, bodyHandler);
      applyResponseMetadata(response, context);
      if (response.statusCode() == 304) {
        return response;
      }
      if (response.statusCode() >= 400) {
        String snippet = errorBodySnippet(response.body());
        throw new TranslaasApiException(
            response.statusCode(),
            snippet,
            TranslaasApiErrorMessages.fromStatusAndBody(
                response.statusCode(), snippet, response.uri().toString()));
      }
      return response;
    } catch (IOException e) {
      throw new TranslaasApiException(0, null, "Translaas HTTP request failed: " + request.uri(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new TranslaasApiException(0, null, "Translaas HTTP request interrupted", e);
    }
  }

  private void applyResponseMetadata(HttpResponse<?> response, TranslaasRequestContext context) {
    if (context == null) {
      return;
    }
    if (response.statusCode() == 304) {
      context.setNotModified(true);
      response.headers().firstValue("ETag").ifPresent(context::setResponseETag);
      return;
    }
    context.setNotModified(false);
    if (response.statusCode() >= 200 && response.statusCode() < 300) {
      response.headers().firstValue("ETag").ifPresent(context::setResponseETag);
    }
  }

  private List<String> collectHeaders(TranslaasRequestContext context) {
    ArrayList<String> pairs = new ArrayList<>();
    pairs.add(options.getApiKeyHeader());
    pairs.add(options.getApiKey());
    if (context != null
        && options.isUseConditionalRequests()
        && context.getIfNoneMatch().isPresent()) {
      pairs.add("If-None-Match");
      pairs.add(context.getIfNoneMatch().get());
    }
    return pairs;
  }

  private static void applyHeaderPairs(HttpRequest.Builder rb, List<String> pairs) {
    for (int i = 0; i < pairs.size(); i += 2) {
      rb.header(pairs.get(i), pairs.get(i + 1));
    }
  }

  private static String snippet(String body) {
    if (body == null) {
      return null;
    }
    return body.length() > BODY_SNIPPET_MAX ? body.substring(0, BODY_SNIPPET_MAX) : body;
  }

  private static String errorBodySnippet(Object body) {
    if (body == null) {
      return null;
    }
    if (body instanceof String) {
      return snippet((String) body);
    }
    if (body instanceof byte[]) {
      return snippet(new String((byte[]) body, StandardCharsets.UTF_8));
    }
    return null;
  }
}
