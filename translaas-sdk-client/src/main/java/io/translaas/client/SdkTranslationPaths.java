package io.translaas.client;

import io.translaas.models.exception.TranslaasConfigurationException;
import java.util.Objects;

/** Resolves SDK translation endpoint paths from {@link TranslaasOptions}. */
public final class SdkTranslationPaths {

  public static final String DEFAULT_PREFIX = "/sdk/v1/translations";

  private final String prefix;

  public SdkTranslationPaths(TranslaasOptions options) {
    this(normalizePrefix(options.getSdkTranslationsPathPrefix()));
  }

  SdkTranslationPaths(String normalizedPrefix) {
    this.prefix = Objects.requireNonNull(normalizedPrefix, "prefix");
  }

  public String prefix() {
    return prefix;
  }

  public String text() {
    return prefix + "/text";
  }

  public String locales() {
    return prefix + "/locales";
  }

  public String group() {
    return prefix + "/group";
  }

  public String project() {
    return prefix + "/project";
  }

  public String reportMissing() {
    return prefix + "/report-missing";
  }

  public String offlineCache() {
    return prefix + "/offline-cache";
  }

  public static String normalizePrefix(String raw) {
    if (raw == null || raw.isBlank()) {
      return DEFAULT_PREFIX;
    }
    String trimmed = raw.trim();
    if (!trimmed.startsWith("/")) {
      trimmed = "/" + trimmed;
    }
    while (trimmed.endsWith("/")) {
      trimmed = trimmed.substring(0, trimmed.length() - 1);
    }
    if (trimmed.isEmpty()) {
      throw new TranslaasConfigurationException("sdkTranslationsPathPrefix must not be empty");
    }
    return trimmed;
  }
}
