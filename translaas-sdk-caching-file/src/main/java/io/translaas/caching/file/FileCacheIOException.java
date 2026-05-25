package io.translaas.caching.file;

/** Unchecked wrapper for I/O failures from {@link FileCacheProvider}. */
public final class FileCacheIOException extends RuntimeException {

  public FileCacheIOException(String message, Throwable cause) {
    super(message, cause);
  }
}
