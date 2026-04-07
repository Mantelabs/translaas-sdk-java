package io.mantelabs.translaas.client.http;

import io.mantelabs.translaas.client.TranslaasOptions;
import io.mantelabs.translaas.client.TranslaasRequestContext;
import io.mantelabs.translaas.models.exception.TranslaasConfigurationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Base URL normalization and URI building for Translaas HTTP calls. */
public final class TranslaasUris {

  private TranslaasUris() {}

  /**
   * Reduces a user-supplied base URL to the API <strong>origin</strong> only (scheme, host, port).
   * Any path, query, or fragment in the input is discarded so routes are not duplicated when the
   * client appends {@code /sdk/v1/...} or {@code /api/v1/...}.
   */
  public static URI normalizeApiOrigin(URI uri) {
    Objects.requireNonNull(uri, "uri");
    if (uri.getScheme() == null || uri.getHost() == null) {
      throw new TranslaasConfigurationException(
          "baseUrl must be an absolute URI with scheme and host (e.g. https://api.mantelabs.io)");
    }
    String scheme = uri.getScheme();
    String host = uri.getHost();
    int port = uri.getPort();
    try {
      return new URI(scheme, null, host, port, null, null, null);
    } catch (URISyntaxException e) {
      throw new TranslaasConfigurationException("Invalid baseUrl: " + uri, e);
    }
  }

  /**
   * Builds a URI from a normalized API origin, a path starting with {@code /}, and optional query
   * parameters (UTF-8 encoded).
   */
  public static URI buildUri(URI baseOrigin, String path, Map<String, String> queryParams) {
    Objects.requireNonNull(baseOrigin, "baseOrigin");
    Objects.requireNonNull(path, "path");
    String normalizedPath = path.startsWith("/") ? path : "/" + path;
    URI withPath = baseOrigin.resolve(normalizedPath);
    if (queryParams == null || queryParams.isEmpty()) {
      return withPath;
    }
    String query = buildQueryString(queryParams);
    try {
      return new URI(
          withPath.getScheme(),
          withPath.getRawAuthority(),
          withPath.getPath(),
          query,
          null);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Could not build URI from " + withPath + " ? " + query, e);
    }
  }

  static String buildQueryString(Map<String, String> queryParams) {
    StringBuilder q = new StringBuilder();
    for (Map.Entry<String, String> e : queryParams.entrySet()) {
      if (e.getKey() == null || e.getValue() == null) {
        continue;
      }
      if (q.length() > 0) {
        q.append('&');
      }
      q.append(encodeQueryParam(e.getKey())).append('=').append(encodeQueryParam(e.getValue()));
    }
    return q.toString();
  }

  /**
   * Merges query parameters: defaults from options, overrides from context, then explicit entries
   * (explicit wins on key collision). Order is stable for testing.
   */
  public static LinkedHashMap<String, String> mergeQueryParams(
      TranslaasOptions options, TranslaasRequestContext context, Map<String, String> explicit) {

    LinkedHashMap<String, String> out = new LinkedHashMap<>();

    options.getChannel().ifPresent(v -> out.put("channel", v));
    options.getSnapshotVersion().ifPresent(v -> out.put("v", v));
    options.getDefaultProject().ifPresent(v -> out.put("project", v));
    options
        .getIncludeContextDefault()
        .ifPresent(b -> out.put("includeContext", booleanQueryValue(b)));

    if (context != null) {
      context.getChannel().ifPresent(v -> out.put("channel", v));
      context.getVersion().ifPresent(v -> out.put("v", v));
      context.getProject().ifPresent(v -> out.put("project", v));
      context
          .getIncludeContext()
          .ifPresent(b -> out.put("includeContext", booleanQueryValue(b)));
    }

    if (explicit != null) {
      out.putAll(explicit);
    }
    return out;
  }

  private static String booleanQueryValue(boolean b) {
    return b ? "true" : "false";
  }

  private static String encodeQueryParam(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }
}
