package io.mantelabs.translaas.client;

import io.mantelabs.translaas.client.http.TranslaasHttp;
import io.mantelabs.translaas.models.ProjectLocalesResponse;
import io.mantelabs.translaas.models.exception.TranslaasApiException;
import io.mantelabs.translaas.models.json.TranslaasJson;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Async HTTP client for the Translaas SDK API.
 *
 * <p>Single translation text: {@link #getEntry(String, String, String, BigDecimal, Map,
 * TranslaasRequestContext, Executor)} and overloads. On {@code 304 Not Modified}, the future
 * completes with an empty string and {@link TranslaasRequestContext#isNotModified()} is {@code
 * true} when a context instance was provided (parity with .NET).
 *
 * <p>Project locales JSON: {@link #getProjectLocales(String, TranslaasRequestContext, Executor)}.
 * On {@code 304 Not Modified}, the future completes with {@code null} and {@link
 * TranslaasRequestContext#isNotModified()} is {@code true} when a context instance was provided.
 */
public final class TranslaasClient {

  private static final int BODY_SNIPPET_MAX = 2048;

  /** Path for {@code GET} single translation as {@code text/plain}. */
  public static final String TRANSLATIONS_TEXT_PATH = "/sdk/v1/translations/text";

  /** Path for {@code GET} project locales as {@code application/json}. */
  public static final String TRANSLATIONS_LOCALES_PATH = "/sdk/v1/translations/locales";

  private final TranslaasHttp http;

  public TranslaasClient(TranslaasOptions options) {
    this(new TranslaasHttp(Objects.requireNonNull(options, "options")));
  }

  TranslaasClient(TranslaasHttp http) {
    this.http = Objects.requireNonNull(http, "http");
  }

  public CompletableFuture<String> getEntry(String group, String entry, String lang) {
    return getEntry(group, entry, lang, null, null, null, null);
  }

  public CompletableFuture<String> getEntry(
      String group, String entry, String lang, TranslaasRequestContext context) {
    return getEntry(group, entry, lang, null, null, context, null);
  }

  public CompletableFuture<String> getEntry(
      String group,
      String entry,
      String lang,
      BigDecimal n,
      Map<String, String> parameters,
      TranslaasRequestContext context) {
    return getEntry(group, entry, lang, n, parameters, context, null);
  }

  /**
   * Fetches a single translation string as {@code text/plain; charset=utf-8}.
   *
   * @param n optional plural / decimal selector for pluralized entries
   * @param parameters optional interpolation parameters (query keys must not appear in {@link
   *     TranslationTextQueries#RESERVED_QUERY_KEYS})
   * @param context optional per-request overrides and response metadata; cleared via {@link
   *     TranslaasRequestContext#clearResponseMetadata()} before the request
   * @param executor optional executor for the async task; defaults to the common pool
   * @return response body text, or empty string when the server returns {@code 304} (see {@link
   *     TranslaasRequestContext#isNotModified()})
   */
  public CompletableFuture<String> getEntry(
      String group,
      String entry,
      String lang,
      BigDecimal n,
      Map<String, String> parameters,
      TranslaasRequestContext context,
      Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(
        () ->
            getEntryBlocking(
                TranslationTextQueries.withExplicitEntry(group, lang, entry, n, parameters),
                context),
        exec);
  }

  /**
   * Same as {@link #getEntry(String, String, String, BigDecimal, Map, TranslaasRequestContext,
   * Executor)} but sends the entry key using <strong>shorthand</strong> (see {@link
   * TranslationTextQueries#withShorthandEntryKey}).
   */
  public CompletableFuture<String> getEntryUsingShorthand(
      String group,
      String entryKey,
      String lang,
      BigDecimal n,
      Map<String, String> interpolationParameters,
      TranslaasRequestContext context,
      Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(
        () ->
            getEntryBlocking(
                TranslationTextQueries.withShorthandEntryKey(
                    group, lang, entryKey, n, interpolationParameters),
                context),
        exec);
  }

  private String getEntryBlocking(Map<String, String> query, TranslaasRequestContext context)
      throws TranslaasApiException {
    if (context != null) {
      context.clearResponseMetadata();
    }
    HttpResponse<String> response = http.get(TRANSLATIONS_TEXT_PATH, query, context);
    if (response.statusCode() == 304) {
      return "";
    }
    String body = response.body();
    return body != null ? body : "";
  }

  /**
   * Fetches supported locale codes for a project ({@code application/json}).
   *
   * <p>Query parameters: required {@code project}; optional {@code channel} and {@code v} from
   * {@link TranslaasOptions} defaults and {@link TranslaasRequestContext} overrides.
   *
   * @param project required project key
   * @return deserialized body, or {@code null} when the server returns {@code 304} (see {@link
   *     TranslaasRequestContext#isNotModified()})
   */
  public CompletableFuture<ProjectLocalesResponse> getProjectLocales(String project) {
    return getProjectLocales(project, null, null);
  }

  /**
   * Same as {@link #getProjectLocales(String)} with optional per-request context (conditional GET
   * via {@link TranslaasRequestContext#setIfNoneMatch(String)} when {@link
   * TranslaasOptions#isUseConditionalRequests()} is true).
   */
  public CompletableFuture<ProjectLocalesResponse> getProjectLocales(
      String project, TranslaasRequestContext context) {
    return getProjectLocales(project, context, null);
  }

  /**
   * @param executor optional executor for the async task; defaults to the common pool
   */
  public CompletableFuture<ProjectLocalesResponse> getProjectLocales(
      String project, TranslaasRequestContext context, Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(
        () -> getProjectLocalesBlocking(project, context), exec);
  }

  private ProjectLocalesResponse getProjectLocalesBlocking(
      String project, TranslaasRequestContext context) throws TranslaasApiException {
    if (project == null || project.isBlank()) {
      throw new IllegalArgumentException("project is required");
    }
    if (context != null) {
      context.clearResponseMetadata();
    }
    LinkedHashMap<String, String> query = new LinkedHashMap<>();
    query.put("project", project);
    HttpResponse<String> response = http.get(TRANSLATIONS_LOCALES_PATH, query, context);
    if (response.statusCode() == 304) {
      return null;
    }
    String body = response.body();
    try {
      return TranslaasJson.mapper()
          .readValue(body != null ? body : "", ProjectLocalesResponse.class);
    } catch (IOException e) {
      throw new TranslaasApiException(
          response.statusCode(),
          snippet(body),
          "Failed to parse project locales JSON: " + response.uri(),
          e);
    }
  }

  private static String snippet(String body) {
    if (body == null) {
      return null;
    }
    return body.length() > BODY_SNIPPET_MAX ? body.substring(0, BODY_SNIPPET_MAX) : body;
  }
}
