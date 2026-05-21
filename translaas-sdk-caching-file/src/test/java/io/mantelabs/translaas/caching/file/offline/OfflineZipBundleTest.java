package io.mantelabs.translaas.caching.file.offline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mantelabs.translaas.models.json.TranslaasJson;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;

class OfflineZipBundleTest {

  private static final ObjectMapper MAPPER = TranslaasJson.mapper();

  @Test
  void parseOfflineZip_readsManifestLocalesAndProjects() throws Exception {
    byte[] zip = buildZip();
    OfflineBundle bundle = OfflineZipBundle.parseOfflineZip(zip);
    assertThat(bundle.getManifest()).containsEntry("version", "1.0");
    assertThat(bundle.getLocalesByProject().get("demo-project").getLocales())
        .containsExactly("en", "de");
    assertThat(
            bundle
                .getProjectsByProjectLang()
                .get("demo-project")
                .get("en")
                .getGroups()
                .get("common")
                .getEntries()
                .get("hello")
                .asText())
        .isEqualTo("Hello");
  }

  @Test
  void parseOfflineZip_emptyRaises() {
    assertThatThrownBy(() -> OfflineZipBundle.parseOfflineZip(new byte[0]))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void resolveProjectKey_findsSanitizedFolder() throws Exception {
    OfflineBundle bundle = OfflineZipBundle.parseOfflineZip(buildZip());
    assertThat(OfflineZipBundle.resolveProjectKey(bundle, "demo-project")).isEqualTo("demo-project");
  }

  private static byte[] buildZip() throws Exception {
    Map<String, Object> manifest = new LinkedHashMap<>();
    manifest.put("version", "1.0");
    manifest.put("projects", Map.of("demo-project", Map.of("languages", List.of("en", "de"))));
    Map<String, Object> localesWrapper =
        Map.of("cachedAt", "2026-01-01T00:00:00Z", "data", Map.of("locales", List.of("en", "de")));
    Map<String, Object> enProject =
        Map.of(
            "cachedAt",
            "2026-01-01T00:00:00Z",
            "data",
            Map.of("common", Map.of("hello", "Hello")));
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try (ZipOutputStream zip = new ZipOutputStream(buffer)) {
      zip.putNextEntry(new ZipEntry("manifest.json"));
      zip.write(MAPPER.writeValueAsBytes(manifest));
      zip.closeEntry();
      zip.putNextEntry(new ZipEntry("demo-project/locales.json"));
      zip.write(MAPPER.writeValueAsBytes(localesWrapper));
      zip.closeEntry();
      zip.putNextEntry(new ZipEntry("demo-project/en/project.json"));
      zip.write(MAPPER.writeValueAsBytes(enProject));
      zip.closeEntry();
    }
    return buffer.toByteArray();
  }
}
