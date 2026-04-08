package io.mantelabs.translaas.caching.file;

import java.nio.file.Path;
import java.util.Objects;

/** Configuration for {@link FileCacheProvider}. */
public final class FileCacheOptions {

  private final Path root;

  private FileCacheOptions(Path root) {
    this.root = Objects.requireNonNull(root, "root");
  }

  public Path getRoot() {
    return root;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private Path root;

    private Builder() {}

    public Builder root(Path root) {
      this.root = root;
      return this;
    }

    public FileCacheOptions build() {
      return new FileCacheOptions(root);
    }
  }
}
