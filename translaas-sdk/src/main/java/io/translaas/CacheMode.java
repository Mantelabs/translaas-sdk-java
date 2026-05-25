package io.translaas;

/**
 * Cache granularity for translation lookups (convenience facade over {@link
 * io.translaas.client.CacheMode}; see root {@code README.md}).
 */
public enum CacheMode {
  NONE(io.translaas.client.CacheMode.NONE),
  ENTRY(io.translaas.client.CacheMode.ENTRY),
  GROUP(io.translaas.client.CacheMode.GROUP),
  PROJECT(io.translaas.client.CacheMode.PROJECT);

  private final io.translaas.client.CacheMode client;

  CacheMode(io.translaas.client.CacheMode client) {
    this.client = client;
  }

  io.translaas.client.CacheMode toClient() {
    return client;
  }
}
