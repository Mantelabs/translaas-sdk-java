package io.translaas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.translaas.client.TranslaasClient;
import io.translaas.models.exception.TranslaasConfigurationException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranslaasServiceTest {

  @Mock private TranslaasClient client;

  private io.translaas.client.TranslaasOptions clientOptions;

  @BeforeEach
  void setUp() {
    clientOptions =
        io.translaas.client.TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl("https://api.example.com")
            .defaultLanguage(LanguageCodes.ENGLISH)
            .build();
  }

  @Test
  void t_withExplicitLang_delegatesToClient() {
    when(client.getEntry(
            eq("common"),
            eq("welcome"),
            eq("en"),
            isNull(),
            isNull(),
            isNull(),
            isNull()))
        .thenReturn(CompletableFuture.completedFuture("Hello"));

    TranslaasService service = new TranslaasService(client, clientOptions, List.of());
    assertThat(service.t("common", "welcome", "en").join()).isEqualTo("Hello");

    verify(client)
        .getEntry(
            eq("common"),
            eq("welcome"),
            eq("en"),
            isNull(),
            isNull(),
            isNull(),
            isNull());
  }

  @Test
  void t_withoutLang_usesDefaultFromOptions() {
    when(client.getEntry(
            eq("common"),
            eq("welcome"),
            eq("en"),
            isNull(),
            isNull(),
            isNull(),
            isNull()))
        .thenReturn(CompletableFuture.completedFuture("x"));

    TranslaasService service = new TranslaasService(client, clientOptions, List.of());
    assertThat(service.t("common", "welcome").join()).isEqualTo("x");
  }

  @Test
  void resolveLanguage_matchesTWithoutExplicitLang() {
    io.translaas.client.TranslaasOptions opts =
        io.translaas.client.TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl("https://api.example.com")
            .defaultLanguage(LanguageCodes.ENGLISH)
            .build();

    LanguageResolver german = () -> Optional.of("de");
    TranslaasService service = new TranslaasService(client, opts, List.of(german));
    assertThat(service.resolveLanguage()).isEqualTo("de");
  }

  @Test
  void t_withoutLang_usesLanguageResolverBeforeDefault() {
    io.translaas.client.TranslaasOptions opts =
        io.translaas.client.TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl("https://api.example.com")
            .defaultLanguage(LanguageCodes.ENGLISH)
            .build();

    when(client.getEntry(
            eq("g"),
            eq("e"),
            eq("de"),
            isNull(),
            isNull(),
            isNull(),
            isNull()))
        .thenReturn(CompletableFuture.completedFuture("de-text"));

    LanguageResolver german = () -> Optional.of("de");
    TranslaasService service = new TranslaasService(client, opts, List.of(german));
    assertThat(service.t("g", "e").join()).isEqualTo("de-text");
  }

  @Test
  void t_withoutLang_throwsWhenNoDefaultAndNoResolver() {
    io.translaas.client.TranslaasOptions opts =
        io.translaas.client.TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl("https://api.example.com")
            .build();

    TranslaasService service = new TranslaasService(client, opts, List.of());
    assertThatThrownBy(() -> service.t("g", "e"))
        .isInstanceOf(TranslaasConfigurationException.class);
  }

  @Test
  void t_withAutoLangPlural_delegatesWithResolvedLanguage() {
    when(client.getEntry(
            eq("messages"),
            eq("item"),
            eq("en"),
            ArgumentMatchers.eq(BigDecimal.valueOf(5)),
            isNull(),
            isNull(),
            isNull()))
        .thenReturn(CompletableFuture.completedFuture("items"));

    TranslaasService service = new TranslaasService(client, clientOptions, List.of());
    assertThat(service.t("messages", "item", 5).join()).isEqualTo("items");
  }

  @Test
  void t_withAutoLangParameters_delegatesWithResolvedLanguage() {
    Map<String, String> params = Map.of("userName", "John");
    when(client.getEntry(
            eq("messages"),
            eq("greeting"),
            eq("en"),
            isNull(),
            eq(params),
            isNull(),
            isNull()))
        .thenReturn(CompletableFuture.completedFuture("Hello John"));

    TranslaasService service = new TranslaasService(client, clientOptions, List.of());
    assertThat(service.t("messages", "greeting", params).join()).isEqualTo("Hello John");
  }

  @Test
  void t_withAutoLangPluralAndParameters_delegatesWithResolvedLanguage() {
    Map<String, String> params = Map.of("userName", "John", "pending", "3");
    when(client.getEntry(
            eq("messages"),
            eq("greeting"),
            eq("en"),
            ArgumentMatchers.eq(BigDecimal.valueOf(5)),
            eq(params),
            isNull(),
            isNull()))
        .thenReturn(CompletableFuture.completedFuture("Hello John, you have 5 items and 3 pending"));

    TranslaasService service = new TranslaasService(client, clientOptions, List.of());
    assertThat(service.t("messages", "greeting", 5, params).join())
        .isEqualTo("Hello John, you have 5 items and 3 pending");
  }

  @Test
  void t_plural_delegatesWithDecimalN() {
    when(client.getEntry(
            eq("messages"),
            eq("item"),
            eq("en"),
            ArgumentMatchers.eq(BigDecimal.valueOf(5)),
            isNull(),
            isNull(),
            isNull()))
        .thenReturn(CompletableFuture.completedFuture("items"));

    TranslaasService service = new TranslaasService(client, clientOptions, List.of());
    assertThat(service.t("messages", "item", "en", 5).join()).isEqualTo("items");
  }

  @Test
  void t_withInterpolationMap_delegates() {
    Map<String, String> params = Map.of("name", "Ada");
    when(client.getEntry(
            eq("g"),
            eq("e"),
            eq("en"),
            isNull(),
            eq(params),
            isNull(),
            isNull()))
        .thenReturn(CompletableFuture.completedFuture("Hello Ada"));

    TranslaasService service = new TranslaasService(client, clientOptions, List.of());
    assertThat(service.t("g", "e", "en", params).join()).isEqualTo("Hello Ada");
  }
}
