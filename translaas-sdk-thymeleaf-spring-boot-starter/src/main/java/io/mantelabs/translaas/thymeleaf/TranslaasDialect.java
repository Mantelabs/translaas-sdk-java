package io.mantelabs.translaas.thymeleaf;

import io.mantelabs.translaas.TranslaasService;
import java.util.LinkedHashSet;
import java.util.Set;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

/** Thymeleaf 3.x dialect exposing Translaas markup (namespace {@value TranslaasDialect#NAMESPACE_URI}). */
public final class TranslaasDialect extends AbstractProcessorDialect {

  /** XML namespace for {@code translaas:*} elements and (future) attributes. */
  public static final String NAMESPACE_URI = "https://translaas.mantelabs.io";

  public static final String PREFIX = "translaas";

  private final TranslaasService translaasService;

  public TranslaasDialect(TranslaasService translaasService) {
    super("Translaas", PREFIX, /* processorPrecedence */ 1000);
    this.translaasService = translaasService;
  }

  @Override
  public Set<IProcessor> getProcessors(String dialectPrefix) {
    Set<IProcessor> processors = new LinkedHashSet<>();
    processors.add(new TranslaasTextTagProcessor(dialectPrefix, translaasService));
    return processors;
  }
}
