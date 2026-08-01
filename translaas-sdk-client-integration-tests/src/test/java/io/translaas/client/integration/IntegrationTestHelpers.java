package io.translaas.client.integration;

import io.translaas.models.exception.TranslaasApiException;

/**
 * Shared helpers for live API integration tests (Mantelabs HTTP 404 vs legacy 204 semantics).
 */
final class IntegrationTestHelpers {

  /** Hint when the configured project or resource is missing on the Mantelabs platform. */
  static final String SDK_NOT_FOUND_SKIP_MESSAGE =
      "SDK resource not found (HTTP 404) — set TRANSLAAS_DEFAULT_PROJECT to an existing project id"
          + " (default: translaas-sdk-samples)";

  private IntegrationTestHelpers() {}

  static boolean isSdkNotFound(TranslaasApiException exception) {
    return exception.getHttpStatus() == 404;
  }

  static boolean softSkipIf(boolean condition, String message) {
    if (condition) {
      System.err.println("skipping: " + message);
    }
    return condition;
  }

  static boolean softSkipOnSdkNotFound(TranslaasApiException exception) {
    if (!isSdkNotFound(exception)) {
      return false;
    }
    softSkipIf(true, SDK_NOT_FOUND_SKIP_MESSAGE);
    return true;
  }

  static boolean softSkipOnUnreachableApi(TranslaasApiException exception) {
    if (exception.getHttpStatus() != 0) {
      return false;
    }
    return softSkipIf(
        true,
        "API unreachable at configured TRANSLAAS_BASE_URL — start local Docker or fix"
            + " TRANSLAAS_BASE_URL");
  }

  static boolean softSkipLiveApiFailure(TranslaasApiException exception) {
    return softSkipOnSdkNotFound(exception) || softSkipOnUnreachableApi(exception);
  }
}
