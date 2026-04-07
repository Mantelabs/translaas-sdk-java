package io.mantelabs.translaas.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * Request body for {@code POST /sdk/v1/translations/report-missing} (OpenAPI component
 * {@code ReportMissingKeysRequest}).
 */
public final class ReportMissingKeysRequest {

  private final List<ReportMissingKeyItemRequest> keys;

  /**
   * @param keys list of missing key descriptors, or {@code null}
   */
  @JsonCreator
  public ReportMissingKeysRequest(@JsonProperty("keys") List<ReportMissingKeyItemRequest> keys) {
    this.keys = keys;
  }

  /** @return keys to report, or {@code null} */
  public List<ReportMissingKeyItemRequest> getKeys() {
    return keys;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReportMissingKeysRequest that = (ReportMissingKeysRequest) o;
    return Objects.equals(keys, that.keys);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keys);
  }
}
