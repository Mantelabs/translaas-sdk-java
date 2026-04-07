package io.mantelabs.translaas.models.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Shared Jackson {@link ObjectMapper} for Translaas API DTOs (ISO-8601 instants, tolerant parsing).
 */
public final class TranslaasJson {

  private static final ObjectMapper MAPPER = createMapper();

  private TranslaasJson() {}

  /**
   * @return a thread-safe mapper configured for Translaas JSON payloads
   */
  public static ObjectMapper mapper() {
    return MAPPER;
  }

  private static ObjectMapper createMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    return mapper;
  }
}
