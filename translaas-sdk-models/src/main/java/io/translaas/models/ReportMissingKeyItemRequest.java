package io.translaas.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * One missing-key entry in {@link ReportMissingKeysRequest} (OpenAPI component
 * {@code ReportMissingKeyItemRequest}).
 */
public final class ReportMissingKeyItemRequest {

  private final String groupKey;
  private final String entryKey;
  private final String languageIsoCode;

  /**
   * @param groupKey translation group key, or {@code null}
   * @param entryKey entry key within the group, or {@code null}
   * @param languageIsoCode locale code, or {@code null}
   */
  @JsonCreator
  public ReportMissingKeyItemRequest(
      @JsonProperty("groupKey") String groupKey,
      @JsonProperty("entryKey") String entryKey,
      @JsonProperty("languageIsoCode") String languageIsoCode) {
    this.groupKey = groupKey;
    this.entryKey = entryKey;
    this.languageIsoCode = languageIsoCode;
  }

  /** @return group key, or {@code null} */
  public String getGroupKey() {
    return groupKey;
  }

  /** @return entry key, or {@code null} */
  public String getEntryKey() {
    return entryKey;
  }

  /** @return language ISO code, or {@code null} */
  public String getLanguageIsoCode() {
    return languageIsoCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReportMissingKeyItemRequest that = (ReportMissingKeyItemRequest) o;
    return Objects.equals(groupKey, that.groupKey)
        && Objects.equals(entryKey, that.entryKey)
        && Objects.equals(languageIsoCode, that.languageIsoCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupKey, entryKey, languageIsoCode);
  }
}
