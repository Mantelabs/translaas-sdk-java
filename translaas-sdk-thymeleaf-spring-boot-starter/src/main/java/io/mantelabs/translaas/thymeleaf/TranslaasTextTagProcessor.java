package io.mantelabs.translaas.thymeleaf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mantelabs.translaas.TranslaasService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Renders {@code <translaas:text .../>} by calling {@link TranslaasService} on the request thread
 * ({@link CompletableFuture#join()}).
 *
 * <p>Attributes:
 *
 * <ul>
 *   <li>{@code group} (required), {@code entry} (required) — literals or standard Thymeleaf expressions.
 *   <li>{@code lang} (optional) — omitted uses {@link TranslaasService#resolveLanguage()}.
 *   <li>{@code number} (optional) — plural {@code n}; literal or expression resolving to a number.
 *   <li>{@code params} (optional) — JSON object of string values, e.g. {@code {"name":"Ada"}}, or an expression
 *       resolving to {@code Map}.
 * </ul>
 */
public final class TranslaasTextTagProcessor extends AbstractElementTagProcessor {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

  private final TranslaasService translaasService;

  public TranslaasTextTagProcessor(String dialectPrefix, TranslaasService translaasService) {
    super(
        TemplateMode.HTML,
        dialectPrefix,
        "text",
        /* prefixElementName */ true,
        /* attributeName */ null,
        /* prefixAttributeName */ false,
        /* precedence */ 1000);
    this.translaasService = Objects.requireNonNull(translaasService, "translaasService");
  }

  @Override
  protected void doProcess(
      ITemplateContext context,
      IProcessableElementTag tag,
      IElementTagStructureHandler structureHandler) {
    String group = requireNonBlank(evalString(context, tag, "group"), "group");
    String entry = requireNonBlank(evalString(context, tag, "entry"), "entry");
    String langRaw = evalString(context, tag, "lang");
    String lang = langRaw == null || langRaw.isBlank() ? null : langRaw.trim();

    BigDecimal pluralN = evalNumber(context, tag, "number");
    Map<String, String> params = evalParams(context, tag, "params");

    CompletableFuture<String> future = translate(group, entry, lang, pluralN, params);
    String text = future.join();

    structureHandler.removeTags();
    structureHandler.replaceWith(context.getModelFactory().createText(text), /* processable */ false);
  }

  private CompletableFuture<String> translate(
      String group,
      String entry,
      String langOrNull,
      BigDecimal pluralN,
      Map<String, String> params) {
    boolean explicitLang = langOrNull != null;
    boolean hasPlural = pluralN != null;
    boolean hasParams = params != null && !params.isEmpty();

    if (!explicitLang && !hasPlural && !hasParams) {
      return translaasService.t(group, entry);
    }

    String lang = explicitLang ? langOrNull : translaasService.resolveLanguage();

    if (hasPlural && hasParams) {
      return translaasService.t(group, entry, lang, pluralN, params);
    }
    if (hasPlural) {
      return translaasService.t(group, entry, lang, pluralN);
    }
    if (hasParams) {
      return translaasService.t(group, entry, lang, params);
    }
    return translaasService.t(group, entry, lang);
  }

  private static String requireNonBlank(String value, String attr) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Attribute '" + attr + "' is required on <translaas:text>.");
    }
    return value.trim();
  }

  private static String evalString(ITemplateContext context, IProcessableElementTag tag, String attr) {
    String raw = rawAttribute(tag, attr);
    if (raw == null) {
      return null;
    }
    Object v = evalMaybeExpression(context, raw);
    return v == null ? null : Objects.toString(v, null);
  }

  private static BigDecimal evalNumber(ITemplateContext context, IProcessableElementTag tag, String attr) {
    String raw = rawAttribute(tag, attr);
    if (raw == null || raw.isBlank()) {
      return null;
    }
    Object v = evalMaybeExpression(context, raw);
    if (v == null) {
      return null;
    }
    if (v instanceof BigDecimal) {
      return (BigDecimal) v;
    }
    if (v instanceof Number) {
      return BigDecimal.valueOf(((Number) v).doubleValue());
    }
    return new BigDecimal(Objects.toString(v, "").trim());
  }

  private static Map<String, String> evalParams(ITemplateContext context, IProcessableElementTag tag, String attr) {
    String raw = rawAttribute(tag, attr);
    if (raw == null || raw.isBlank()) {
      return null;
    }
    if (looksLikeThymeleafExpression(raw)) {
      Object v = parseAndExecute(context, raw);
      if (v == null) {
        return null;
      }
      if (v instanceof Map) {
        return stringifyMap((Map<?, ?>) v);
      }
      throw new IllegalArgumentException("Attribute '" + attr + "' must evaluate to a Map when using an expression.");
    }
    return parseJsonParams(raw, attr);
  }

  private static Map<String, String> parseJsonParams(String raw, String attr) {
    try {
      Map<String, Object> map = JSON.readValue(raw.trim(), MAP_TYPE);
      if (map == null || map.isEmpty()) {
        return null;
      }
      Map<String, String> out = new LinkedHashMap<>();
      for (Map.Entry<String, Object> e : map.entrySet()) {
        out.put(e.getKey(), e.getValue() == null ? "" : Objects.toString(e.getValue()));
      }
      return out;
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JSON for attribute '" + attr + "': " + e.getMessage(), e);
    }
  }

  private static Map<String, String> stringifyMap(Map<?, ?> map) {
    if (map.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, String> out = new LinkedHashMap<>();
    for (Map.Entry<?, ?> e : map.entrySet()) {
      String key = Objects.toString(e.getKey(), "");
      out.put(key, e.getValue() == null ? "" : Objects.toString(e.getValue()));
    }
    return out;
  }

  private static Object evalMaybeExpression(ITemplateContext context, String raw) {
    if (looksLikeThymeleafExpression(raw)) {
      return parseAndExecute(context, raw);
    }
    return raw;
  }

  private static boolean looksLikeThymeleafExpression(String raw) {
    String t = raw.trim();
    return t.contains("${")
        || t.contains("*{")
        || t.contains("#{")
        || t.startsWith("|")
        || t.endsWith("|")
        || t.contains("|${");
  }

  private static Object parseAndExecute(ITemplateContext context, String expression) {
    var parser = StandardExpressions.getExpressionParser(context.getConfiguration());
    var expr = parser.parseExpression(context, expression.trim());
    return expr.execute(context);
  }

  private static String rawAttribute(IProcessableElementTag tag, String attr) {
    if (tag.hasAttribute(attr)) {
      return tag.getAttributeValue(attr);
    }
    String dataAttr = "data-" + attr;
    if (tag.hasAttribute(dataAttr)) {
      return tag.getAttributeValue(dataAttr);
    }
    return null;
  }
}
