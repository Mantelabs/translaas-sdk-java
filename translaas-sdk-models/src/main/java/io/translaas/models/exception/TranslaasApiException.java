package io.translaas.models.exception;

import java.util.Optional;

/**
 * Thrown when an HTTP call to the Translaas API fails or returns an unexpected status.
 */
public class TranslaasApiException extends TranslaasException {

  private static final long serialVersionUID = 1L;

  private final int httpStatus;
  private final String responseBodySnippet;

  /**
   * @param httpStatus HTTP status code from the response
   * @param responseBodySnippet optional excerpt of the response body for diagnostics (may be truncated)
   * @param message human-readable description
   */
  public TranslaasApiException(int httpStatus, String responseBodySnippet, String message) {
    super(message);
    this.httpStatus = httpStatus;
    this.responseBodySnippet = responseBodySnippet;
  }

  /**
   * @param httpStatus HTTP status code from the response
   * @param responseBodySnippet optional excerpt of the response body for diagnostics (may be truncated)
   * @param message human-readable description
   * @param cause original cause, if any
   */
  public TranslaasApiException(
      int httpStatus, String responseBodySnippet, String message, Throwable cause) {
    super(message, cause);
    this.httpStatus = httpStatus;
    this.responseBodySnippet = responseBodySnippet;
  }

  /**
   * @return HTTP status code (e.g. 404, 500)
   */
  public int getHttpStatus() {
    return httpStatus;
  }

  /**
   * @return optional snippet of the error response body, for logging or support tickets
   */
  public Optional<String> getResponseBodySnippet() {
    return Optional.ofNullable(responseBodySnippet);
  }
}
