package io.mantelabs.translaas;

/**
 * Cache granularity for translation lookups (convenience facade over {@link
 * io.mantelabs.translaas.client.CacheMode}; see root {@code README.md}).
 */
public enum CacheMode {
  NONE(io.mantelabs.translaas.client.CacheMode.NONE),
  ENTRY(io.mantelabs.translaas.client.CacheMode.ENTRY),
  GROUP(io.mantelabs.translaas.client.CacheMode.GROUP),
  PROJECT(io.mantelabs.translaas.client.CacheMode.PROJECT);

  private final io.mantelabs.translaas.client.CacheMode client;

  CacheMode(io.mantelabs.translaas.client.CacheMode client) {
    this.client = client;
  }

  io.mantelabs.translaas.client.CacheMode toClient() {
    return client;
  }
}
