package io.translaas.client;

import java.net.URI;

/**
 * Canonical test API origin for client tests (no real HTTP; URI shape only).
 */
public final class TestApiUrls {

  public static final String ORIGIN = "https://api.translaas.local";

  public static final String HOST = "api.translaas.local";

  /** Same host with a non-default HTTPS port (normalization must preserve port). */
  public static final String ORIGIN_PORT_8443 = "https://api.translaas.local:8443";

  /**
   * HTTP origin for a local server (e.g. WireMock) on a dynamic port.
   *
   * <p>Uses numeric loopback ({@code 127.0.0.1}) so the address always resolves—CI runners and
   * clean machines often have no {@code hosts} entry for {@value #HOST}. WireMock binds on
   * loopback; this matches that listener without extra environment setup.
   */
  public static URI httpOrigin(int port) {
    return URI.create("http://127.0.0.1:" + port);
  }

  private TestApiUrls() {}
}
