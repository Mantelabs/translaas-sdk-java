<!--
  Manual release notes template (Java SDK).

  GitHub does NOT inject this file into the “New release” form automatically.
  When publishing a release:
  1. Optionally click “Generate release notes” to pull merged PRs (uses `.github/release.yml`).
  2. Replace or augment that text by copying sections below and filling placeholders.

  Suggested release title:
  Release VERSION — short headline (e.g. SDK v1 API parity)
-->

## 🎉 Overview

Summarize this release in a few sentences: scope (e.g. beta / GA), alignment with **Translaas SDK v1** HTTP surface (`/sdk/v1/...`), and any headline themes (client options, caching, offline ZIP, API key validation, etc.).

## 📦 Artifacts published

- **`io.mantelabs:translaas-sdk:VERSION`** — [Maven Central](https://central.sonatype.com/search?q=g:io.mantelabs+translaas-sdk)

<!-- Optional: additional modules or BOM -->

- **`io.mantelabs:translaas-sdk-bom:VERSION`** (if applicable)

## 📥 Install

**Maven:**

```xml
<dependency>
  <groupId>io.mantelabs</groupId>
  <artifactId>translaas-sdk</artifactId>
  <version>VERSION</version>
</dependency>
```

**Gradle (Kotlin DSL):**

```kotlin
dependencies {
    implementation("io.mantelabs:translaas-sdk:VERSION")
}
```

## ✨ New features

### 1. Short section title

- Bullet points for public API / behavior.
- Reference packages: `io.mantelabs.translaas.client`, `io.mantelabs.translaas.config`, etc.

```java
import io.mantelabs.translaas.client.TranslaasClient;
import io.mantelabs.translaas.client.TranslaasOptions;

TranslaasOptions options = TranslaasOptions.builder()
    .apiKey(System.getenv("TRANSLAAS_API_KEY"))
    .baseUrl("https://api.mantelabs.io")
    .build();

TranslaasClient client = new TranslaasClient(options);
// ...
```

### 2. Another feature group (optional)

…

## 🧪 Testing

Describe test status for this line (e.g. JUnit 5, `./mvnw verify`, approximate test count, integration vs unit).

## 📚 Documentation

- **[README.md](https://github.com/Mantelabs/translaas-sdk-java/blob/main/README.md)**: install, `baseUrl`, API overview
- **[`.docs/`](https://github.com/Mantelabs/translaas-sdk-java/tree/main/.docs)** or **`docs/`** (if present): deep dives
- **[CONTRIBUTING.md](https://github.com/Mantelabs/translaas-sdk-java/blob/main/CONTRIBUTING.md)** (if present): release / Maven Central process

## 🔄 Migration guide

Call out breaking changes, path or `baseUrl` expectations (`/sdk/v1`), and coordination with other language SDKs if relevant.

## 🐛 Bug fixes

Summarize fixes; link **`CHANGELOG.md`** section for detail.

## 📝 Changelog

- **[CHANGELOG.md](https://github.com/Mantelabs/translaas-sdk-java/blob/vVERSION/CHANGELOG.md)** — section for this release (use the same `vVERSION` git tag as the release)

## 🙏 Contributors

Thank contributors by name or “Translaas SDK Contributors”.

---

**Repository**: https://github.com/Mantelabs/translaas-sdk-java  

**Full compare**: https://github.com/Mantelabs/translaas-sdk-java/compare/PREVIOUS_TAG...vVERSION  

(Replace `PREVIOUS_TAG` and `VERSION` with the previous release tag and this release tag, e.g. `v0.2.0...v0.3.0`.)
