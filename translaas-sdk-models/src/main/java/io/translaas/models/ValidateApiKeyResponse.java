package io.translaas.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

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
  private final List<String> projectIds;
  private final String defaultProjectId;
  private final String integrationName;
  private final Instant authenticatedAt;

  /**
   * @param valid whether the key is valid for the request
   * @param tenantId tenant ULID string, or {@code null}
   * @param projectId default project ULID string, or {@code null}
   * @param projectIds all project ULIDs the key may access
   * @param defaultProjectId implicit default project for multi-project keys
   * @param integrationName integration display name, or {@code null}
   * @param authenticatedAt when the key was authenticated, or {@code null}
   */
  @JsonCreator
  public ValidateApiKeyResponse(
      @JsonProperty("isValid") boolean valid,
      @JsonProperty("tenantId") String tenantId,
      @JsonProperty("projectId") String projectId,
      @JsonProperty("projectIds") List<String> projectIds,
      @JsonProperty("defaultProjectId") String defaultProjectId,
      @JsonProperty("integrationName") String integrationName,
      @JsonProperty("authenticatedAt") Instant authenticatedAt) {
    this.valid = valid;
    this.tenantId = tenantId;
    this.projectId = projectId;
    this.projectIds = projectIds == null ? List.of() : List.copyOf(projectIds);
    this.defaultProjectId = defaultProjectId;
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

  /** @return default project ULID as string, or {@code null} */
  public String getProjectId() {
    return projectId;
  }

  /** @return all project ULIDs the key may access */
  public List<String> getProjectIds() {
    return projectIds;
  }

  /** @return implicit default project id when returned by the API */
  public String getDefaultProjectId() {
    return defaultProjectId;
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
