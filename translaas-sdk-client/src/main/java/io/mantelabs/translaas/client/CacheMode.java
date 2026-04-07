package io.mantelabs.translaas.client;

/**
 * Cache granularity for translation lookups (aligned with README / other SDKs).
 */
public enum CacheMode {
  NONE,
  ENTRY,
  GROUP,
  PROJECT
}
