package io.translaas.caching.file;

import io.translaas.caching.TranslaasCacheEntry;
import io.translaas.caching.TranslaasCacheProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File-backed {@link TranslaasCacheProvider} with safe concurrent access per cache key.
 *
 * <h2>On-disk layout</h2>
 *
 * Under the configured {@linkplain FileCacheOptions#getRoot() root}:
 *
 * <pre>
 *   {@code <root>/v1/<sha256-hex>}        — raw cached bytes (payload)
 *   {@code <root>/v1/<sha256-hex>.meta}   — Java {@link Properties}: optional {@code etag},
 *   {@code expiresAtMillis}
 * </pre>
 *
 * The directory name {@code v1} reserves room for a future layout version without colliding with
 * existing files. Cache keys are hashed with SHA-256 so arbitrary-length opaque keys map to
 * bounded file names.
 */
public final class FileCacheProvider implements TranslaasCacheProvider {

  private static final String VERSION_DIR = "v1";

  private final Path root;
  private final ConcurrentHashMap<String, Object> keyLocks = new ConcurrentHashMap<>();

  public FileCacheProvider(Path root) {
    this(FileCacheOptions.builder().root(root).build());
  }

  public FileCacheProvider(FileCacheOptions options) {
    Objects.requireNonNull(options, "options");
    this.root = options.getRoot();
  }

  @Override
  public Optional<TranslaasCacheEntry> get(String key) {
    Objects.requireNonNull(key, "key");
    String digest = sha256Hex(key);
    Object lock = keyLocks.computeIfAbsent(digest, d -> new Object());
    synchronized (lock) {
      try {
        Path data = dataPath(digest);
        Path meta = metaPath(digest);
        if (!Files.isRegularFile(data)) {
          return Optional.empty();
        }
        byte[] bytes = Files.readAllBytes(data);
        Properties p = new Properties();
        if (Files.isRegularFile(meta)) {
          try (InputStream in = Files.newInputStream(meta)) {
            p.load(in);
          }
        }
        String etag = p.getProperty("etag");
        if (etag != null && etag.isEmpty()) {
          etag = null;
        }
        String expRaw = p.getProperty("expiresAtMillis");
        Instant expiresAt = null;
        if (expRaw != null && !expRaw.isEmpty()) {
          expiresAt = Instant.ofEpochMilli(Long.parseLong(expRaw.trim()));
        }
        TranslaasCacheEntry entry = new TranslaasCacheEntry(bytes, etag, expiresAt);
        Instant now = Instant.now();
        if (entry.isExpiredAt(now)) {
          removeFiles(digest);
          return Optional.empty();
        }
        return Optional.of(entry);
      } catch (IOException | RuntimeException e) {
        throw new FileCacheIOException("Failed to read cache entry for key hash " + digest, e);
      }
    }
  }

  @Override
  public void put(String key, TranslaasCacheEntry entry) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(entry, "entry");
    String digest = sha256Hex(key);
    Object lock = keyLocks.computeIfAbsent(digest, d -> new Object());
    synchronized (lock) {
      try {
        Files.createDirectories(versionDir());
        Path data = dataPath(digest);
        Path meta = metaPath(digest);
        Path dataTmp = data.resolveSibling(data.getFileName().toString() + ".tmp");
        Path metaTmp = meta.resolveSibling(meta.getFileName().toString() + ".tmp");
        writeAtomic(dataTmp, data, entry.getValue());
        writeMeta(metaTmp, meta, entry);
      } catch (IOException e) {
        throw new FileCacheIOException("Failed to write cache entry for key hash " + digest, e);
      }
    }
  }

  @Override
  public void remove(String key) {
    Objects.requireNonNull(key, "key");
    String digest = sha256Hex(key);
    Object lock = keyLocks.computeIfAbsent(digest, d -> new Object());
    synchronized (lock) {
      removeFiles(digest);
    }
  }

  @Override
  public void clear() {
    Path dir = versionDir();
    try {
      if (!Files.isDirectory(dir)) {
        return;
      }
      try (var stream = Files.list(dir)) {
        for (Path p : (Iterable<Path>) stream::iterator) {
          Files.deleteIfExists(p);
        }
      }
    } catch (IOException e) {
      throw new FileCacheIOException("Failed to clear file cache under " + dir, e);
    }
  }

  private Path versionDir() {
    return root.resolve(VERSION_DIR);
  }

  private Path dataPath(String digest) {
    return versionDir().resolve(digest);
  }

  private Path metaPath(String digest) {
    return versionDir().resolve(digest + ".meta");
  }

  private void removeFiles(String digest) {
    try {
      Files.deleteIfExists(dataPath(digest));
      Files.deleteIfExists(metaPath(digest));
    } catch (IOException e) {
      throw new FileCacheIOException("Failed to remove cache files for hash " + digest, e);
    }
  }

  private static void writeAtomic(Path tmp, Path target, byte[] value) throws IOException {
    Files.write(tmp, value);
    moveReplace(tmp, target);
  }

  private static void writeMeta(Path tmp, Path target, TranslaasCacheEntry entry)
      throws IOException {
    Properties p = new Properties();
    entry.getEtag().ifPresent(v -> p.setProperty("etag", v));
    entry.getExpiresAt().ifPresent(v -> p.setProperty("expiresAtMillis", Long.toString(v.toEpochMilli())));
    try (OutputStream out = Files.newOutputStream(tmp)) {
      p.store(out, "Translaas file cache meta");
    }
    moveReplace(tmp, target);
  }

  private static void moveReplace(Path tmp, Path target) throws IOException {
    try {
      Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException e) {
      Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  static String sha256Hex(String key) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] raw = md.digest(key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      return toHexLower(raw);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private static final char[] HEX_LOWER = "0123456789abcdef".toCharArray();

  static String toHexLower(byte[] raw) {
    char[] out = new char[raw.length * 2];
    for (int i = 0; i < raw.length; i++) {
      int v = raw[i] & 0xff;
      out[i * 2] = HEX_LOWER[v >>> 4];
      out[i * 2 + 1] = HEX_LOWER[v & 0xf];
    }
    return new String(out);
  }
}
