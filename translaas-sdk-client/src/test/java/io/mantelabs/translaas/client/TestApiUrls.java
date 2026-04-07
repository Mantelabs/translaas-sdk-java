package io.mantelabs.translaas.client;

/**
 * Canonical test API origin for client tests (no real HTTP; URI shape only).
 */
public final class TestApiUrls {

  public static final String ORIGIN = "https://api.translaas.local";

  public static final String HOST = "api.translaas.local";

  /** Same host with a non-default HTTPS port (normalization must preserve port). */
  public static final String ORIGIN_PORT_8443 = "https://api.translaas.local:8443";

  private TestApiUrls() {}
}
