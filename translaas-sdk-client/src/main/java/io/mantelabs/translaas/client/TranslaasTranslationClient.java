package io.mantelabs.translaas.client;

import io.mantelabs.translaas.models.GroupTranslationsResponse;
import io.mantelabs.translaas.models.OfflineCacheDownloadResult;
import io.mantelabs.translaas.models.ProjectLocalesResponse;
import io.mantelabs.translaas.models.ProjectTranslationsResponse;
import io.mantelabs.translaas.models.ReportMissingKeysRequest;
import io.mantelabs.translaas.models.ValidateApiKeyResponse;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/** Common surface for {@link TranslaasClient} and offline {@code CachingTranslaasClient}. */
public interface TranslaasTranslationClient {

  CompletableFuture<String> getEntry(String group, String entry, String lang);

  CompletableFuture<String> getEntry(
      String group, String entry, String lang, TranslaasRequestContext context);

  CompletableFuture<String> getEntry(
      String group,
      String entry,
      String lang,
      BigDecimal n,
      Map<String, String> parameters,
      TranslaasRequestContext context,
      Executor executor);

  CompletableFuture<ProjectLocalesResponse> getProjectLocales(String project);

  CompletableFuture<ProjectLocalesResponse> getProjectLocales(
      String project, TranslaasRequestContext context, Executor executor);

  CompletableFuture<GroupTranslationsResponse> getGroupTranslations(
      String project, String group, String lang);

  CompletableFuture<GroupTranslationsResponse> getGroupTranslations(
      String project,
      String group,
      String lang,
      String format,
      TranslaasRequestContext context,
      Executor executor);

  CompletableFuture<ProjectTranslationsResponse> getProjectTranslations(
      String project, String lang);

  CompletableFuture<ProjectTranslationsResponse> getProjectTranslations(
      String project,
      String lang,
      String format,
      TranslaasRequestContext context,
      Executor executor);

  CompletableFuture<Void> reportMissingKeys(ReportMissingKeysRequest request);

  CompletableFuture<OfflineCacheDownloadResult> getOfflineCache(String project);

  CompletableFuture<OfflineCacheDownloadResult> getOfflineCache(
      String project, TranslaasRequestContext context, Executor executor);

  CompletableFuture<ValidateApiKeyResponse> validateApiKey();
}
