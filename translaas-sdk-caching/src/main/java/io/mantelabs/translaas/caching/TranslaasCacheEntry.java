package io.mantelabs.translaas.caching;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * A cached payload plus optional HTTP {@code ETag} and expiration metadata.
 *
 * <p>Thread-safety of the byte array is the same as for any shared {@code byte[]}: callers should
 * treat values as immutable after {@link #put(String, TranslaasCacheEntry)} unless they defensively
 * copy.
 */
public final class TranslaasCacheEntry {

  private final byte[] value;
  private final String etag;
  private final Instant expiresAt;

  /**
   * @param value cached bytes (must not be {@code null}; may be zero-length)
   * @param etag optional ETag for conditional requests; {@code null} if not used
   * @param expiresAt optional absolute expiration; {@code null} if the entry does not expire
   */
  public TranslaasCacheEntry(byte[] value, String etag, Instant expiresAt) {
    this.value = Objects.requireNonNull(value, "value");
    this.etag = etag;
    this.expiresAt = expiresAt;
  }

  public byte[] getValue() {
    return value;
  }

  public Optional<String> getEtag() {
    return Optional.ofNullable(etag);
  }

  public Optional<Instant> getExpiresAt() {
    return Optional.ofNullable(expiresAt);
  }

  /** Returns whether {@code now} is at or after {@link #getExpiresAt()} when that instant exists. */
  public boolean isExpiredAt(Instant now) {
    return expiresAt != null && !expiresAt.isAfter(now);
  }

  /** Defensive copy of {@link #getValue()}. */
  public byte[] copyValue() {
    return Arrays.copyOf(value, value.length);
  }
}
