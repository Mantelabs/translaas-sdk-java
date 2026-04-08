package io.mantelabs.translaas.spring.boot.autoconfigure;

import io.mantelabs.translaas.LanguageResolver;
import java.util.Optional;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Resolves the active language from Spring's {@link LocaleContextHolder} (aligned with the servlet
 * request locale when running Spring MVC / Spring Web).
 */
public final class TranslaasLanguageResolver implements LanguageResolver {

  @Override
  public Optional<String> resolveLanguage() {
    return Optional.of(LocaleContextHolder.getLocale().toLanguageTag());
  }
}
