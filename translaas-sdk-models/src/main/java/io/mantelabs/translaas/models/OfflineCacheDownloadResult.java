package io.mantelabs.translaas.models;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Result of downloading the offline translation cache as {@code application/zip} (parity with .NET
 * {@code OfflineCacheDownloadResult}).
 *
 * <p><strong>ZIP layout</strong> (for consumers or future parser helpers): {@code manifest.json} at
 * the archive root; {@code {sanitizedProjectId}/locales.json}; {@code
 * {sanitizedProjectId}/{locale}/project.json} for each locale. The client returns raw bytes only;
 * validation or unpacking is the caller's responsibility.
 *
 * <p>When the server responds with {@code 304 Not Modified}, {@link
 * io.mantelabs.translaas.client.TranslaasClient#getOfflineCache} completes with {@code null} and
 * {@link io.mantelabs.translaas.client.TranslaasRequestContext#isNotModified()} is {@code true} when
 * a context instance was provided.
 */
public final class OfflineCacheDownloadResult {

  private final byte[] zipBytes;
  private final String contentDispositionFilename;

  /**
   * @param zipBytes ZIP payload (not {@code null}; use zero-length array for empty body)
   * @param contentDispositionFilename optional filename from {@code Content-Disposition}, for
   *     logging or saving to disk
   */
  public OfflineCacheDownloadResult(byte[] zipBytes, String contentDispositionFilename) {
    this.zipBytes = Objects.requireNonNull(zipBytes, "zipBytes");
    this.contentDispositionFilename = contentDispositionFilename;
  }

  /** @return the downloaded ZIP bytes */
  public byte[] getZipBytes() {
    return zipBytes;
  }

  /**
   * @return filename suggested by the server in {@code Content-Disposition}, when present (e.g.
   *     for logging)
   */
  public Optional<String> getContentDispositionFilename() {
    return Optional.ofNullable(contentDispositionFilename).filter(s -> !s.isBlank());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OfflineCacheDownloadResult that = (OfflineCacheDownloadResult) o;
    return Arrays.equals(zipBytes, that.zipBytes)
        && Objects.equals(contentDispositionFilename, that.contentDispositionFilename);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(zipBytes);
    result = 31 * result + Objects.hashCode(contentDispositionFilename);
    return result;
  }
}
