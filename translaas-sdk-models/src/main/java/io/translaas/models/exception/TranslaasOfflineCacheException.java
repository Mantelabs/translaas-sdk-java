package io.translaas.models.exception;

/**
 * Thrown when offline cache bundle operations fail (for example download or unpack errors).
 */
public class TranslaasOfflineCacheException extends TranslaasException {

  private static final long serialVersionUID = 1L;

  /**
   * @param message human-readable description
   */
  public TranslaasOfflineCacheException(String message) {
    super(message);
  }

  /**
   * @param message human-readable description
   * @param cause original cause, if any
   */
  public TranslaasOfflineCacheException(String message, Throwable cause) {
    super(message, cause);
  }
}
