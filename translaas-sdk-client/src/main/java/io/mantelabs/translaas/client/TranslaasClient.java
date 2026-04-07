package io.mantelabs.translaas.client;

import io.mantelabs.translaas.client.http.TranslaasHttp;
import io.mantelabs.translaas.models.exception.TranslaasApiException;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
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
 */
public final class TranslaasClient {

  /** Path for {@code GET} single translation as {@code text/plain}. */
  public static final String TRANSLATIONS_TEXT_PATH = "/sdk/v1/translations/text";

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
}
