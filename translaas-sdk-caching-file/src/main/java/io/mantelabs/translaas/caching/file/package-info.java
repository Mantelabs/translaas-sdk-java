/**
 * File-based and hybrid cache providers. Payload files live under {@code <root>/v1/<sha256-hex>};
 * sidecar {@code .meta} holds optional ETag and expiry (see {@link
 * io.mantelabs.translaas.caching.file.FileCacheProvider}).
 */
package io.mantelabs.translaas.caching.file;
