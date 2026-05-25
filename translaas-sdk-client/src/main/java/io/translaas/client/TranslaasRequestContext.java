package io.translaas.client;

import java.util.Optional;

/**
 * Per-request overrides and response metadata (parity with .NET {@code TranslaasRequestContext}).
 *
 * <p>Instances are mutable and intended for a single logical request; do not share across threads
 * unless synchronized externally.
 */
public final class TranslaasRequestContext {

  private String channel;
  private String version;
  private String project;
  private Boolean includeContext;
  private String ifNoneMatch;

  private String responseETag;
  private boolean notModified;

  public TranslaasRequestContext() {}

  public Optional<String> getChannel() {
    return Optional.ofNullable(channel);
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  /**
   * Snapshot / bundle version, sent as query parameter {@code v}.
   */
  public Optional<String> getVersion() {
    return Optional.ofNullable(version);
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Optional<String> getProject() {
    return Optional.ofNullable(project);
  }

  public void setProject(String project) {
    this.project = project;
  }

  public Optional<Boolean> getIncludeContext() {
    return Optional.ofNullable(includeContext);
  }

  public void setIncludeContext(Boolean includeContext) {
    this.includeContext = includeContext;
  }

  public Optional<String> getIfNoneMatch() {
    return Optional.ofNullable(ifNoneMatch);
  }

  public void setIfNoneMatch(String ifNoneMatch) {
    this.ifNoneMatch = ifNoneMatch;
  }

  /**
   * ETag from the last successful response (2xx), when present.
   */
  public Optional<String> getResponseETag() {
    return Optional.ofNullable(responseETag);
  }

  public void setResponseETag(String responseETag) {
    this.responseETag = responseETag;
  }

  public boolean isNotModified() {
    return notModified;
  }

  public void setNotModified(boolean notModified) {
    this.notModified = notModified;
  }

  /**
   * Clears response-derived fields so this context can be reused for another call.
   */
  public void clearResponseMetadata() {
    this.responseETag = null;
    this.notModified = false;
  }
}
