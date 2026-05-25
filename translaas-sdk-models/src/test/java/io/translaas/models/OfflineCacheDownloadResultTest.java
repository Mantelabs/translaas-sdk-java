package io.translaas.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OfflineCacheDownloadResultTest {

  @Test
  void notModified_factory() {
    OfflineCacheDownloadResult r = OfflineCacheDownloadResult.notModified();
    assertThat(r.isNotModified()).isTrue();
    assertThat(r.getZipBytes()).isEmpty();
  }
}
