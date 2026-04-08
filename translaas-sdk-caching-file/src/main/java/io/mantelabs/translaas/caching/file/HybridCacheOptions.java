package io.mantelabs.translaas.caching.file;

import io.mantelabs.translaas.caching.MemoryTranslaasCacheOptions;
import java.util.Objects;

/** Configuration for {@link HybridCacheProvider}. */
public final class HybridCacheOptions {

  private final MemoryTranslaasCacheOptions memory;
  private final boolean promoteL2HitsToL1;

  private HybridCacheOptions(MemoryTranslaasCacheOptions memory, boolean promoteL2HitsToL1) {
    this.memory = Objects.requireNonNull(memory, "memory");
    this.promoteL2HitsToL1 = promoteL2HitsToL1;
  }

  public MemoryTranslaasCacheOptions getMemory() {
    return memory;
  }

  public boolean isPromoteL2HitsToL1() {
    return promoteL2HitsToL1;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private MemoryTranslaasCacheOptions memory = MemoryTranslaasCacheOptions.defaults();
    private boolean promoteL2HitsToL1 = true;

    private Builder() {}

    public Builder memory(MemoryTranslaasCacheOptions memory) {
      this.memory = memory != null ? memory : MemoryTranslaasCacheOptions.defaults();
      return this;
    }

    public Builder promoteL2HitsToL1(boolean promote) {
      this.promoteL2HitsToL1 = promote;
      return this;
    }

    public HybridCacheOptions build() {
      return new HybridCacheOptions(memory, promoteL2HitsToL1);
    }
  }
}
