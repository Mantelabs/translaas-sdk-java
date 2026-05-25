package io.translaas;

import java.util.Optional;

/**
 * Pluggable language resolution for {@link TranslaasService#t(String, String)} overloads that omit an
 * explicit language. Resolvers are consulted in order; the first non-empty value wins before {@link
 * io.translaas.client.TranslaasOptions#getDefaultLanguage()}.
 */
@FunctionalInterface
public interface LanguageResolver {

  /**
   * @return the resolved language tag, or empty if this resolver cannot supply one
   */
  Optional<String> resolveLanguage();
}
