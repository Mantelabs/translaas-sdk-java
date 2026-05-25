package io.translaas.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Response body for {@code GET /api/v1/api-keys/validate} (OpenAPI component
 * {@code ValidateApiKeyResponse}).
 *
 * <p>Tenant and project identifiers are serialized as ULID strings in JSON.
 */
public final class ValidateApiKeyResponse {

  private final boolean valid;
  private final String tenantId;
  private final String projectId;
  private final String integrationName;
  private final Instant authenticatedAt;

  /**
   * @param valid whether the key is valid for the request
   * @param tenantId tenant ULID string, or {@code null}
   * @param projectId project ULID string, or {@code null}
   * @param integrationName integration display name, or {@code null}
   * @param authenticatedAt when the key was authenticated, or {@code null}
   */
  @JsonCreator
  public ValidateApiKeyResponse(
      @JsonProperty("isValid") boolean valid,
      @JsonProperty("tenantId") String tenantId,
      @JsonProperty("projectId") String projectId,
      @JsonProperty("integrationName") String integrationName,
      @JsonProperty("authenticatedAt") Instant authenticatedAt) {
    this.valid = valid;
    this.tenantId = tenantId;
    this.projectId = projectId;
    this.integrationName = integrationName;
    this.authenticatedAt = authenticatedAt;
  }

  /** @return whether the API key is valid */
  @JsonProperty("isValid")
  public boolean isValid() {
    return valid;
  }

  /** @return tenant ULID as string, or {@code null} */
  public String getTenantId() {
    return tenantId;
  }

  /** @return project ULID as string, or {@code null} */
  public String getProjectId() {
    return projectId;
  }

  /** @return integration name, or {@code null} */
  public String getIntegrationName() {
    return integrationName;
  }

  /** @return authentication timestamp, or {@code null} */
  public Instant getAuthenticatedAt() {
    return authenticatedAt;
  }
}
