package io.translaas;

import io.translaas.caching.file.TranslaasClients;
import io.translaas.client.TranslaasRequestContext;
import io.translaas.client.TranslaasTranslationClient;
import io.translaas.models.GroupTranslationsResponse;
import io.translaas.models.OfflineCacheDownloadResult;
import io.translaas.models.ProjectLocalesResponse;
import io.translaas.models.ProjectTranslationsResponse;
import io.translaas.models.ReportMissingKeysRequest;
import io.translaas.models.ValidateApiKeyResponse;
import io.translaas.models.exception.TranslaasConfigurationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Convenience API over {@link TranslaasClient} for single-string translations, matching the root
 * {@code README.md} quick start ({@code t(...)} overloads and {@link LanguageResolver} for automatic
 * language selection).
 *
 * <p>Async methods use the JDK default asynchronous pipeline: when no {@link Executor} is passed,
 * {@link TranslaasClient} completes work on {@link java.util.concurrent.ForkJoinPool#commonPool()}.
 */
public final class TranslaasService {

  private final TranslaasTranslationClient client;
  private final io.translaas.client.TranslaasOptions clientOptions;
  private final List<LanguageResolver> languageResolvers;

  /**
   * Builds a service with an internal {@link TranslaasClient} from {@link TranslaasOptions#asClientOptions()}.
   */
  public TranslaasService(TranslaasOptions options) {
    this(options, Collections.emptyList());
  }

  /**
   * Same as {@link #TranslaasService(TranslaasOptions)} with an ordered list of {@link
   * LanguageResolver} instances (first non-empty language wins, then {@link
   * io.translaas.client.TranslaasOptions#getDefaultLanguage()}).
   */
  public TranslaasService(TranslaasOptions options, List<LanguageResolver> languageResolvers) {
    this(
        TranslaasClients.create(Objects.requireNonNull(options, "options").asClientOptions()),
        options.asClientOptions(),
        languageResolvers);
  }

  /**
   * Varargs convenience for {@link #TranslaasService(TranslaasOptions, List)}.
   */
  public TranslaasService(TranslaasOptions options, LanguageResolver... languageResolvers) {
    this(
        options,
        languageResolvers == null || languageResolvers.length == 0
            ? Collections.emptyList()
            : Arrays.asList(languageResolvers));
  }

  /**
   * For tests and advanced composition: supply a pre-built {@link TranslaasClient} and the matching
   * configuration (used for default language fallback).
   */
  public TranslaasService(
      TranslaasTranslationClient client,
      io.translaas.client.TranslaasOptions clientOptions,
      List<LanguageResolver> languageResolvers) {
    this.client = Objects.requireNonNull(client, "client");
    this.clientOptions = Objects.requireNonNull(clientOptions, "clientOptions");
    this.languageResolvers =
        Collections.unmodifiableList(new ArrayList<>(languageResolvers != null ? languageResolvers : List.of()));
  }

  public CompletableFuture<String> t(String group, String entry, String lang) {
    return getEntry(group, entry, lang, null, null, null, null);
  }

  public CompletableFuture<String> t(String group, String entry) {
    return getEntry(group, entry, resolveLanguage(), null, null, null, null);
  }

  public CompletableFuture<String> t(String group, String entry, long pluralN) {
    return getEntry(group, entry, resolveLanguage(), BigDecimal.valueOf(pluralN), null, null, null);
  }

  public CompletableFuture<String> t(String group, String entry, BigDecimal pluralN) {
    return getEntry(group, entry, resolveLanguage(), pluralN, null, null, null);
  }

  public CompletableFuture<String> t(String group, String entry, Map<String, String> parameters) {
    return getEntry(group, entry, resolveLanguage(), null, parameters, null, null);
  }

  public CompletableFuture<String> t(
      String group, String entry, long pluralN, Map<String, String> parameters) {
    return getEntry(
        group, entry, resolveLanguage(), BigDecimal.valueOf(pluralN), parameters, null, null);
  }

  public CompletableFuture<String> t(
      String group, String entry, BigDecimal pluralN, Map<String, String> parameters) {
    return getEntry(group, entry, resolveLanguage(), pluralN, parameters, null, null);
  }

  public CompletableFuture<String> t(String group, String entry, String lang, long pluralN) {
    return getEntry(group, entry, lang, BigDecimal.valueOf(pluralN), null, null, null);
  }

  public CompletableFuture<String> t(String group, String entry, String lang, BigDecimal pluralN) {
    return getEntry(group, entry, lang, pluralN, null, null, null);
  }

  public CompletableFuture<String> t(String group, String entry, String lang, Map<String, String> parameters) {
    return getEntry(group, entry, lang, null, parameters, null, null);
  }

  public CompletableFuture<String> t(
      String group, String entry, String lang, BigDecimal pluralN, Map<String, String> parameters) {
    return getEntry(group, entry, lang, pluralN, parameters, null, null);
  }

  public CompletableFuture<String> t(
      String group,
      String entry,
      String lang,
      BigDecimal pluralN,
      Map<String, String> parameters,
      Executor executor) {
    return getEntry(group, entry, lang, pluralN, parameters, null, executor);
  }

  public CompletableFuture<String> t(String group, String entry, TranslaasRequestContext context) {
    return getEntry(group, entry, resolveLanguage(), null, null, context, null);
  }

  public CompletableFuture<String> t(String group, String entry, String lang, TranslaasRequestContext context) {
    return getEntry(group, entry, lang, null, null, context, null);
  }

  public CompletableFuture<String> t(
      String group,
      String entry,
      String lang,
      BigDecimal pluralN,
      Map<String, String> parameters,
      TranslaasRequestContext context,
      Executor executor) {
    return getEntry(group, entry, lang, pluralN, parameters, context, executor);
  }

  /** Returns the underlying client (plain HTTP or offline-decorated). */
  public TranslaasTranslationClient getClient() {
    return client;
  }

  public CompletableFuture<GroupTranslationsResponse> getGroupTranslations(
      String project, String group, String lang) {
    return client.getGroupTranslations(project, group, lang);
  }

  public CompletableFuture<ProjectTranslationsResponse> getProjectTranslations(
      String project, String lang) {
    return client.getProjectTranslations(project, lang);
  }

  public CompletableFuture<ProjectLocalesResponse> getProjectLocales(String project) {
    return client.getProjectLocales(project);
  }

  public CompletableFuture<OfflineCacheDownloadResult> getOfflineCache(String project) {
    return client.getOfflineCache(project);
  }

  public CompletableFuture<Void> reportMissingKeys(ReportMissingKeysRequest request) {
    return client.reportMissingKeys(request);
  }

  public CompletableFuture<ValidateApiKeyResponse> validateApiKey() {
    return client.validateApiKey();
  }

  public List<LanguageResolver> getLanguageResolvers() {
    return languageResolvers;
  }

  /**
   * Resolves the target language using the same {@link LanguageResolver} chain and defaults as {@link
   * #t(String, String)} when no explicit language is provided.
   */
  public String resolveLanguage() {
    return resolveLanguageForRequest();
  }

  private CompletableFuture<String> getEntry(
      String group,
      String entry,
      String lang,
      BigDecimal pluralN,
      Map<String, String> parameters,
      TranslaasRequestContext context,
      Executor executor) {
    Objects.requireNonNull(group, "group");
    Objects.requireNonNull(entry, "entry");
    Objects.requireNonNull(lang, "lang");
    return client.getEntry(group, entry, lang, pluralN, parameters, context, executor);
  }

  private String resolveLanguageForRequest() {
    for (LanguageResolver r : languageResolvers) {
      Optional<String> lang = r.resolveLanguage();
      if (lang.isPresent()) {
        String v = lang.get().trim();
        if (!v.isEmpty()) {
          return v;
        }
      }
    }
    return clientOptions
        .getDefaultLanguage()
        .filter(s -> !s.isBlank())
        .map(String::trim)
        .orElseThrow(
            () ->
                new TranslaasConfigurationException(
                    "No language resolved: configure defaultLanguage on TranslaasOptions and/or register LanguageResolver instances"));
  }
}
