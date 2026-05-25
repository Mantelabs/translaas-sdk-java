package io.translaas.models.exception;

/**
 * Thrown when SDK options or configuration are invalid or inconsistent.
 */
public class TranslaasConfigurationException extends TranslaasException {

  private static final long serialVersionUID = 1L;

  /**
   * @param message describes what is misconfigured
   */
  public TranslaasConfigurationException(String message) {
    super(message);
  }

  /**
   * @param message describes what is misconfigured
   * @param cause original cause, if any
   */
  public TranslaasConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
