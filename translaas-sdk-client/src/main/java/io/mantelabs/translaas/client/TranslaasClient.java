package io.mantelabs.translaas.client;

import io.mantelabs.translaas.client.http.TranslaasHttp;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.mantelabs.translaas.models.GroupTranslationsResponse;
import io.mantelabs.translaas.models.ProjectLocalesResponse;
import io.mantelabs.translaas.models.ProjectTranslationsResponse;
import io.mantelabs.translaas.models.ReportMissingKeysRequest;
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
 *
 * <p>Group and project translation bundles: {@link #getGroupTranslations(String, String, String,
 * TranslaasRequestContext, Executor)} and {@link #getProjectTranslations(String, String, String,
 * TranslaasRequestContext, Executor)}. On {@code 304 Not Modified}, JSON bundle methods complete
 * with {@code null} and {@link TranslaasRequestContext#isNotModified()} is {@code true} when a
 * context instance was provided.
 *
 * <p>Missing keys: {@link #reportMissingKeys(ReportMissingKeysRequest, TranslaasRequestContext,
 * Executor)} — {@code POST} JSON; expects {@code 202 Accepted}. Requires a project-scoped API key
 * (see {@link #reportMissingKeys(ReportMissingKeysRequest, TranslaasRequestContext, Executor)}).
 */
public final class TranslaasClient {

  private static final int BODY_SNIPPET_MAX = 2048;

  /** Path for {@code GET} single translation as {@code text/plain}. */
  public static final String TRANSLATIONS_TEXT_PATH = "/sdk/v1/translations/text";

  /** Path for {@code GET} project locales as {@code application/json}. */
  public static final String TRANSLATIONS_LOCALES_PATH = "/sdk/v1/translations/locales";

  /** Path for {@code GET} all translations in one group as {@code application/json}. */
  public static final String TRANSLATIONS_GROUP_PATH = "/sdk/v1/translations/group";

  /** Path for {@code GET} all translations in a project as {@code application/json}. */
  public static final String TRANSLATIONS_PROJECT_PATH = "/sdk/v1/translations/project";

  /** Path for {@code POST} report missing keys as {@code application/json}. */
  public static final String TRANSLATIONS_REPORT_MISSING_PATH = "/sdk/v1/translations/report-missing";

  /**
   * Value for the {@code format} query parameter to request composite-key flat bundles ({@code
   * group.entry} keys on project; plain entry keys within the group).
   */
  public static final String FORMAT_FLAT_JSON = "flat-json";

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

  /**
   * Fetches all entries for a single group ({@code application/json}).
   *
   * <p>Query parameters: required {@code project}, {@code group}, {@code lang}; optional {@code
   * channel}, {@code v}, {@code includeContext} from {@link TranslaasOptions} and {@link
   * TranslaasRequestContext}.
   *
   * @return deserialized body, or {@code null} when the server returns {@code 304} (see {@link
   *     TranslaasRequestContext#isNotModified()})
   */
  public CompletableFuture<GroupTranslationsResponse> getGroupTranslations(
      String project, String group, String lang) {
    return getGroupTranslations(project, group, lang, null, null, null);
  }

  /**
   * Same as {@link #getGroupTranslations(String, String, String)} with optional per-request context
   * (conditional GET when {@link TranslaasOptions#isUseConditionalRequests()} is true).
   */
  public CompletableFuture<GroupTranslationsResponse> getGroupTranslations(
      String project, String group, String lang, TranslaasRequestContext context) {
    return getGroupTranslations(project, group, lang, null, context, null);
  }

  /**
   * @param executor optional executor for the async task; defaults to the common pool
   */
  public CompletableFuture<GroupTranslationsResponse> getGroupTranslations(
      String project, String group, String lang, TranslaasRequestContext context, Executor executor) {
    return getGroupTranslations(project, group, lang, null, context, executor);
  }

  /**
   * @param format optional response shape ({@link #FORMAT_FLAT_JSON}); {@code null} omits the query
   *     parameter
   * @param executor optional executor for the async task; defaults to the common pool
   */
  public CompletableFuture<GroupTranslationsResponse> getGroupTranslations(
      String project,
      String group,
      String lang,
      String format,
      TranslaasRequestContext context,
      Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(
        () -> getGroupTranslationsBlocking(project, group, lang, format, context), exec);
  }

  private GroupTranslationsResponse getGroupTranslationsBlocking(
      String project, String group, String lang, String format, TranslaasRequestContext context)
      throws TranslaasApiException {
    requireNonBlank(project, "project");
    requireNonBlank(group, "group");
    requireNonBlank(lang, "lang");
    if (context != null) {
      context.clearResponseMetadata();
    }
    LinkedHashMap<String, String> query = new LinkedHashMap<>();
    query.put("project", project);
    query.put("group", group);
    query.put("lang", lang);
    if (format != null && !format.isBlank()) {
      query.put("format", format);
    }
    HttpResponse<String> response = http.get(TRANSLATIONS_GROUP_PATH, query, context);
    if (response.statusCode() == 304) {
      return null;
    }
    String body = response.body();
    try {
      return TranslaasJson.mapper()
          .readValue(body != null ? body : "", GroupTranslationsResponse.class);
    } catch (IOException e) {
      throw new TranslaasApiException(
          response.statusCode(),
          snippet(body),
          "Failed to parse group translations JSON: " + response.uri(),
          e);
    }
  }

  /**
   * Fetches all translations for a project ({@code application/json}), nested or flat-json per
   * {@code format}.
   *
   * <p>Query parameters: required {@code project}, {@code lang}; optional {@code format}, {@code
   * channel}, {@code v}, {@code includeContext} from options and context.
   *
   * @return deserialized body, or {@code null} when the server returns {@code 304}
   */
  public CompletableFuture<ProjectTranslationsResponse> getProjectTranslations(
      String project, String lang) {
    return getProjectTranslations(project, lang, null, null, null);
  }

  public CompletableFuture<ProjectTranslationsResponse> getProjectTranslations(
      String project, String lang, TranslaasRequestContext context) {
    return getProjectTranslations(project, lang, null, context, null);
  }

  /**
   * @param executor optional executor for the async task; defaults to the common pool
   */
  public CompletableFuture<ProjectTranslationsResponse> getProjectTranslations(
      String project, String lang, TranslaasRequestContext context, Executor executor) {
    return getProjectTranslations(project, lang, null, context, executor);
  }

  /**
   * @param format optional {@link #FORMAT_FLAT_JSON}; {@code null} uses the API default (nested
   *     groups)
   * @param executor optional executor for the async task; defaults to the common pool
   */
  public CompletableFuture<ProjectTranslationsResponse> getProjectTranslations(
      String project,
      String lang,
      String format,
      TranslaasRequestContext context,
      Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.supplyAsync(
        () -> getProjectTranslationsBlocking(project, lang, format, context), exec);
  }

  private ProjectTranslationsResponse getProjectTranslationsBlocking(
      String project, String lang, String format, TranslaasRequestContext context)
      throws TranslaasApiException {
    requireNonBlank(project, "project");
    requireNonBlank(lang, "lang");
    if (context != null) {
      context.clearResponseMetadata();
    }
    LinkedHashMap<String, String> query = new LinkedHashMap<>();
    query.put("project", project);
    query.put("lang", lang);
    if (format != null && !format.isBlank()) {
      query.put("format", format);
    }
    HttpResponse<String> response = http.get(TRANSLATIONS_PROJECT_PATH, query, context);
    if (response.statusCode() == 304) {
      return null;
    }
    String body = response.body();
    try {
      return TranslaasJson.mapper()
          .readValue(body != null ? body : "", ProjectTranslationsResponse.class);
    } catch (IOException e) {
      throw new TranslaasApiException(
          response.statusCode(),
          snippet(body),
          "Failed to parse project translations JSON: " + response.uri(),
          e);
    }
  }

  /**
   * Reports missing translation keys ({@code Content-Type: application/json}). The server responds
   * with {@code 202 Accepted} on success.
   *
   * <p><strong>Authentication:</strong> requires a <em>project-scoped</em> API key; otherwise the
   * server typically returns {@code 401 Unauthorized}.
   *
   * @param request body with {@code keys} (see {@link ReportMissingKeysRequest})
   */
  public CompletableFuture<Void> reportMissingKeys(ReportMissingKeysRequest request) {
    return reportMissingKeys(request, null, null);
  }

  /**
   * Same as {@link #reportMissingKeys(ReportMissingKeysRequest)} with optional per-request query
   * overrides (e.g. {@link TranslaasRequestContext#setProject(String)} for project scope).
   */
  public CompletableFuture<Void> reportMissingKeys(
      ReportMissingKeysRequest request, TranslaasRequestContext context) {
    return reportMissingKeys(request, context, null);
  }

  /**
   * @param executor optional executor for the async task; defaults to the common pool
   */
  public CompletableFuture<Void> reportMissingKeys(
      ReportMissingKeysRequest request, TranslaasRequestContext context, Executor executor) {
    Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
    return CompletableFuture.runAsync(() -> reportMissingKeysBlocking(request, context), exec);
  }

  private void reportMissingKeysBlocking(ReportMissingKeysRequest request, TranslaasRequestContext context)
      throws TranslaasApiException {
    Objects.requireNonNull(request, "request");
    if (context != null) {
      context.clearResponseMetadata();
    }
    String json;
    try {
      json = TranslaasJson.mapper().writeValueAsString(request);
    } catch (JsonProcessingException e) {
      throw new TranslaasApiException(0, null, "Failed to serialize report-missing request body", e);
    }
    http.post(TRANSLATIONS_REPORT_MISSING_PATH, null, json, null, context);
  }

  private static void requireNonBlank(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(name + " is required");
    }
  }

  private static String snippet(String body) {
    if (body == null) {
      return null;
    }
    return body.length() > BODY_SNIPPET_MAX ? body.substring(0, BODY_SNIPPET_MAX) : body;
  }
}
