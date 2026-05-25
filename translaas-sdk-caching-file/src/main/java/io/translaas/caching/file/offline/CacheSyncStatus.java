package io.translaas.caching.file.offline;

/** Synchronization status stored in the offline cache manifest. */
public enum CacheSyncStatus {
  SYNCED("synced"),
  PENDING("pending"),
  FAILED("failed");

  private final String wireName;

  CacheSyncStatus(String wireName) {
    this.wireName = wireName;
  }

  public String wireName() {
    return wireName;
  }

  public static CacheSyncStatus fromWire(String raw) {
    if (raw == null) {
      return SYNCED;
    }
    for (CacheSyncStatus s : values()) {
      if (s.wireName.equalsIgnoreCase(raw)) {
        return s;
      }
    }
    return SYNCED;
  }
}
