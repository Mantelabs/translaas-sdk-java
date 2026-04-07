package io.mantelabs.translaas.models.exception;

/**
 * Base unchecked exception for all Translaas SDK failures.
 */
public class TranslaasException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * @param message human-readable description
   */
  public TranslaasException(String message) {
    super(message);
  }

  /**
   * @param message human-readable description
   * @param cause original cause, if any
   */
  public TranslaasException(String message, Throwable cause) {
    super(message, cause);
  }
}
