package io.mantelabs.translaas.caching.file.offline;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.node.TextNode;
import io.mantelabs.translaas.models.ProjectGroupPayload;
import io.mantelabs.translaas.models.ProjectLocalesResponse;
import io.mantelabs.translaas.models.ProjectTranslationsResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SpecFileCacheProviderTest {

  @TempDir Path tempDir;

  @Test
  void saveAndLoadProject_roundTrips() {
    SpecFileCacheProvider cache = new SpecFileCacheProvider(tempDir);
    ProjectTranslationsResponse project =
        new ProjectTranslationsResponse(
            "demo",
            "en",
            1,
            Instant.parse("2026-01-01T00:00:00Z"),
            Map.of(
                "common",
                new ProjectGroupPayload(
                    Map.of("hello", new TextNode("Hello")), null, null)),
            null,
            null,
            null);
    cache.saveProject("demo", "en", project);
    assertThat(cache.isCached("demo", "en")).isTrue();
    assertThat(cache.getProject("demo", "en")).isPresent();
    assertThat(cache.getGroup("demo", "common", "en")).isPresent();
    assertThat(
            cache.getGroup("demo", "common", "en").get().getEntries().get("hello").asText())
        .isEqualTo("Hello");
    assertThat(Files.exists(tempDir.resolve("demo/en/project.json"))).isTrue();
    assertThat(Files.exists(tempDir.resolve("manifest.json"))).isTrue();
  }

  @Test
  void saveLocales_persistsFile() {
    SpecFileCacheProvider cache = new SpecFileCacheProvider(tempDir);
    cache.saveProjectLocales(
        "demo", new ProjectLocalesResponse("demo", List.of("en", "de"), Instant.now()));
    assertThat(cache.getProjectLocales("demo")).isPresent();
    assertThat(Files.exists(tempDir.resolve("demo/locales.json"))).isTrue();
  }
}
