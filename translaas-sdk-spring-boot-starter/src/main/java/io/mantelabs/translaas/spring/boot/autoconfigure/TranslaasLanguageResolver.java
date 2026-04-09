package io.mantelabs.translaas.spring.boot.autoconfigure;

import io.mantelabs.translaas.LanguageResolver;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Resolves the active language from Spring's {@link LocaleContextHolder} (aligned with the servlet
 * request locale when running Spring MVC / Spring Web).
 *
 * <p>Uses the ISO-639 primary language code ({@link Locale#getLanguage()}) so {@code en_US} becomes
 * {@code en}, matching Translaas sample locales and avoiding API {@code 404} when the backend expects
 * short tags like {@code en} rather than BCP-47 {@code en-US}.
 */
public final class TranslaasLanguageResolver implements LanguageResolver {

  @Override
  public Optional<String> resolveLanguage() {
    Locale locale = LocaleContextHolder.getLocale();
    if (locale == null) {
      return Optional.empty();
    }
    String lang = locale.getLanguage();
    if (lang == null || lang.isBlank()) {
      return Optional.empty();
    }
    return Optional.of(lang);
  }
}
