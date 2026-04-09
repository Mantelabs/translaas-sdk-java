# Translaas SDK for Java

CI
Maven Central
License
Java
GitHub stars

A strongly typed, modular Java SDK for consuming the **Translaas Translation Delivery API**. Use it to fetch translations in JVM applications with compile-time safety and familiar Java APIs.

Published artifacts use the `**io.mantelabs`** group ID (reverse-DNS for [mantelabs.io](https://mantelabs.io)). Example `baseUrl` values below assume the hosted API at `**https://api.mantelabs.io**`; use whatever origin matches your deployment.

## Features

- **Strongly typed API** — Models and configuration types instead of untyped maps
- **Convenience API** — Simple translation lookups via `TranslaasService` (for example a `t` / fluent helper, aligned with other language SDKs)
- **Automatic language resolution** — Optional locale when language providers are configured
- **Flexible caching** — Pluggable or built-in cache modes (memory, entry, group, project) where the SDK exposes them
- **Offline / hybrid caching** — File-based bundles when supported by the client module
- **Resilience** — Configurable timeouts and retry policies on the HTTP layer
- **Modular artifacts** — Optional split modules (core client, models, integrations) if published separately
- **Async-friendly** — Asynchronous calls where the API exposes them (`CompletableFuture`, or virtual-thread friendly blocking APIs on modern JDKs)
- **Standard build tooling** — Published to Maven Central; works with Maven, Gradle, and other JVM build tools

## Requirements

- **JDK 11** or newer (LTS releases recommended for production)

## Installation

### Maven

```xml
<dependency>
  <groupId>io.mantelabs</groupId>
  <artifactId>translaas-sdk</artifactId>
  <version>x.y.z</version><!-- replace with the current version from Maven Central -->
</dependency>
```

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.mantelabs:translaas-sdk:x.y.z") // replace with the current version from Maven Central
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.mantelabs:translaas-sdk:+'
}
```

Pin an explicit version in production builds instead of dynamic `+` resolution.

### Bill of Materials (BOM)

If a BOM is published for aligned versions:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.mantelabs</groupId>
      <artifactId>translaas-sdk-bom</artifactId>
      <version>x.y.z</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

## Quick start

### 1. Add the dependency

Use the Maven or Gradle snippet above with the current version from [Maven Central](https://central.sonatype.com/).

### 2. Package layout (parity with other SDKs)

The **`translaas-sdk`** artifact exposes two layers, similar to .NET’s `Translaas.Client` vs convenience/DI types:

| Layer | Java package | Typical types |
| ----- | ------------ | ------------- |
| HTTP client | `io.mantelabs.translaas.client` | `TranslaasClient`, `TranslaasOptions` (client builder) |
| Convenience API | `io.mantelabs.translaas` | `TranslaasService`, `TranslaasOptions` (facade builder), `CacheMode`, `LanguageCodes` |

Use **`io.mantelabs.translaas.client.TranslaasOptions`** when constructing **`TranslaasClient`**. Use the facade **`io.mantelabs.translaas.TranslaasOptions`** with **`TranslaasService`** (it delegates to the client options internally).

### 3. Create a client

**Option A — `TranslaasService` (convenience lookups)**

```java
import io.mantelabs.translaas.CacheMode;
import io.mantelabs.translaas.LanguageCodes;
import io.mantelabs.translaas.TranslaasOptions;
import io.mantelabs.translaas.TranslaasService;

TranslaasOptions options = TranslaasOptions.builder()
    .apiKey(System.getenv("TRANSLAAS_API_KEY"))
    .baseUrl("https://api.mantelabs.io")
    .cacheMode(CacheMode.GROUP)
    .build();

TranslaasService translaas = new TranslaasService(options);

// Example: async style (exact method names follow the shipped API)
var welcome = translaas.t("common", "welcome", LanguageCodes.ENGLISH).join();

// With automatic language resolution when providers are configured
var welcomeAuto = translaas.t("common", "welcome").join();

// Pluralization (signature mirrors server / SDK contract)
var items = translaas.t("messages", "item", LanguageCodes.ENGLISH, 5).join();
```

**Option B — `TranslaasClient` (full HTTP API)**

```java
import io.mantelabs.translaas.LanguageCodes;
import io.mantelabs.translaas.client.TranslaasClient;
import io.mantelabs.translaas.client.TranslaasOptions;

TranslaasOptions options = TranslaasOptions.builder()
    .apiKey(System.getenv("TRANSLAAS_API_KEY"))
    .baseUrl("https://api.mantelabs.io")
    .build();

TranslaasClient client = new TranslaasClient(options);

var translation = client.getEntry("common", "welcome", LanguageCodes.ENGLISH).join();
```

Use blocking adapters if the SDK provides them (for example `getEntryBlocking`) in servlet-style code; prefer async APIs on structured concurrency or event-loop style runtimes when available.

## Configuration

Samples below use the **`io.mantelabs.translaas`** facade for **`TranslaasService`**. For **`TranslaasClient`** only, build **`io.mantelabs.translaas.client.TranslaasOptions`** instead.

### Basic configuration

```java
import io.mantelabs.translaas.TranslaasOptions;

TranslaasOptions options = TranslaasOptions.builder()
    .apiKey(System.getenv("TRANSLAAS_API_KEY"))
    .baseUrl("https://api.mantelabs.io")
    .build();

TranslaasService translaas = new TranslaasService(options);
```

### Advanced configuration

```java
import io.mantelabs.translaas.CacheMode;
import io.mantelabs.translaas.LanguageCodes;
import io.mantelabs.translaas.TranslaasOptions;
import java.time.Duration;

TranslaasOptions options = TranslaasOptions.builder()
    .apiKey(System.getenv("TRANSLAAS_API_KEY"))
    .baseUrl("https://api.mantelabs.io")
    .defaultLanguage(LanguageCodes.ENGLISH)
    .cacheMode(CacheMode.GROUP)
    .cacheAbsoluteExpiration(Duration.ofHours(1))
    .cacheSlidingExpiration(Duration.ofMinutes(15))
    .timeout(Duration.ofSeconds(30))
    .build();
```

**Configuration options**


| Option                    | Required | Description                                               |
| ------------------------- | -------- | --------------------------------------------------------- |
| `apiKey`                  | **Yes**  | Translaas API key                                         |
| `baseUrl`                 | **Yes**  | API origin only (do **not** append `/api`)                |
| `defaultLanguage`         | No       | Default locale / language code                            |
| `cacheMode`               | No       | `NONE`, `ENTRY`, `GROUP`, `PROJECT` (names as in the SDK) |
| `cacheAbsoluteExpiration` | No       | Absolute cache TTL                                        |
| `cacheSlidingExpiration`  | No       | Sliding cache TTL                                         |
| `timeout`                 | No       | HTTP client timeout                                       |


Exact types (`Duration`, enums, builders) follow the published Javadoc.

### File and hybrid cache (`translaas-sdk-caching-file`)

Use a disk-backed `FileCacheProvider` for offline-friendly persistence, or `HybridCacheProvider` for L1 memory plus L2 file with promotion on L2 hits:

```java
import io.mantelabs.translaas.caching.MemoryTranslaasCacheOptions;
import io.mantelabs.translaas.caching.TranslaasCacheEntry;
import io.mantelabs.translaas.caching.file.FileCacheProvider;
import io.mantelabs.translaas.caching.file.HybridCacheProvider;
import io.mantelabs.translaas.caching.file.HybridCacheOptions;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

Path cacheRoot = Path.of(System.getProperty("java.io.tmpdir"), "translaas-cache");
TranslaasCacheEntry sample =
    new TranslaasCacheEntry("text".getBytes(StandardCharsets.UTF_8), null, null);

FileCacheProvider fileOnly = new FileCacheProvider(cacheRoot);
fileOnly.put("opaque-cache-key", sample);

HybridCacheProvider hybrid =
    new HybridCacheProvider(
        cacheRoot,
        HybridCacheOptions.builder()
            .memory(MemoryTranslaasCacheOptions.lru(512))
            .promoteL2HitsToL1(true)
            .build());
hybrid.put("opaque-cache-key", sample);
```

For cache-only or offline bootstrap, you can mark options so application code may skip API-key validation (the client does not validate on construction):

```java
import io.mantelabs.translaas.client.TranslaasOptions;

TranslaasOptions offlineAware =
    TranslaasOptions.builder()
        .apiKey(System.getenv("TRANSLAAS_API_KEY"))
        .baseUrl("https://api.mantelabs.io")
        .skipApiValidation(true)
        .build();
```

### Environment variables and system properties

Typical mappings (names may match other Translaas SDKs):

```bash
# Shell / container
export TRANSLAAS_API_KEY=your-api-key
export TRANSLAAS_BASE_URL=https://api.mantelabs.io
export TRANSLAAS_CACHE_MODE=GROUP
export TRANSLAAS_DEFAULT_LANGUAGE=en
```

```java
import io.mantelabs.translaas.CacheMode;
import io.mantelabs.translaas.TranslaasOptions;

TranslaasOptions options = TranslaasOptions.builder()
    .apiKey(System.getenv("TRANSLAAS_API_KEY"))
    .baseUrl(System.getenv().getOrDefault("TRANSLAAS_BASE_URL", "https://api.mantelabs.io"))
    .cacheMode(CacheMode.valueOf(System.getenv().getOrDefault("TRANSLAAS_CACHE_MODE", "NONE")))
    .defaultLanguage(System.getenv("TRANSLAAS_DEFAULT_LANGUAGE"))
    .build();
```

On Java you can also layer **Micronaut** or **Quarkus** config, mapping keys into `TranslaasOptions` in `@Configuration` beans, or use the optional Spring Boot starter described below.

Keep secrets out of source control; use env vars, vaults, or your platform’s secret store.

## Framework integration

### Spring Boot (optional starter)

Add **`translaas-sdk-spring-boot-starter`** alongside **`spring-boot-starter`** (or **`spring-boot-starter-web`**) in your application. The starter registers **`TranslaasClient`**, **`TranslaasService`**, and **`TranslaasProperties`** bound from `translaas.*` (see table below). Auto-configuration is registered for **Spring Boot 2.x** (`META-INF/spring.factories`) and **Spring Boot 3.x** (`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`). The starter is built against **Spring Boot 2.7** and **Java 11**; it is expected to work on **Spring Boot 3** applications on **JDK 17+** as well.

**Maven**

```xml
<dependency>
  <groupId>io.mantelabs</groupId>
  <artifactId>translaas-sdk-spring-boot-starter</artifactId>
  <version>x.y.z</version><!-- align with other io.mantelabs artifacts -->
</dependency>
```

**Minimal `application.yml`**

```yaml
translaas:
  api-key: ${TRANSLAAS_API_KEY}
  base-url: https://api.mantelabs.io
```

Required keys match programmatic **`TranslaasOptions`**: `api-key` and `base-url`. Other settings use the same names as in the **Configuration options** table under [Configuration](#configuration) (kebab-case in YAML), for example `cache-mode`, `default-language`, `timeout`, `channel`, `skip-api-validation`.

**Optional behaviors**

| Property | Effect |
| -------- | ------ |
| `translaas.enabled` | When `false`, skips auto-configuration (default `true`). |
| `translaas.caching.memory.enabled` | Registers a **`MemoryTranslaasCacheProvider`** bean (if you did not define your own **`TranslaasCacheProvider`**), with optional `translaas.caching.memory.lru-max-entries`. |
| `translaas.locale.use-spring-locale-context` | Registers a **`LanguageResolver`** that uses Spring’s **`LocaleContextHolder`** (request locale in Spring MVC). |

Disable or replace beans by defining your own **`TranslaasClient`**, **`TranslaasService`**, or **`io.mantelabs.translaas.TranslaasOptions`** bean where needed.

### Spring Boot + Thymeleaf (optional starter)

Add **`translaas-sdk-thymeleaf-spring-boot-starter`** alongside **`translaas-sdk-spring-boot-starter`** and **`spring-boot-starter-thymeleaf`**. The artifact pulls those transitively, registers **`TranslaasDialect`** on the auto-configured **`SpringTemplateEngine`**, and reuses existing **`TranslaasService`** / **`LanguageResolver`** / **`translaas.*`** configuration.

**Maven**

```xml
<dependency>
  <groupId>io.mantelabs</groupId>
  <artifactId>translaas-sdk-thymeleaf-spring-boot-starter</artifactId>
  <version>x.y.z</version>
</dependency>
```

**Template usage** — declare the namespace on the root element, then use **`translaas:text`** (Thymeleaf runs **`TranslaasService`** during render and **`CompletableFuture.join()`** on the request thread):

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:translaas="https://translaas.mantelabs.io">
<body>
  <p><translaas:text group="common" entry="welcome"/></p>
  <p><translaas:text group="common" entry="hello" lang="fr" number="3" params='{"name":"Ada"}'/></p>
</body>
</html>
```

| Attribute | Required | Notes |
| --------- | -------- | ----- |
| `group` | yes | Literal or standard expression. |
| `entry` | yes | Literal or standard expression. |
| `lang` | no | Omit to use the same resolution as **`t(group, entry)`** (defaults + **`LanguageResolver`**). |
| `number` | no | Plural **`n`** (literal or expression); passed through to **`TranslaasService`**. |
| `params` | no | JSON object of string values, or an expression that evaluates to **`Map`**; interpolation parameters for the API. |

Markup aligns with the .NET Razor **translaas** tag helper: **`group`**, **`entry`**, optional **`lang`**, **`number`**, and interpolation **`params`**.

### Other JVM frameworks

Without the starter, you can expose **`TranslaasClient`** / **`TranslaasService`** as **`@Bean`** methods from **`@Configuration`** (for example in Spring Boot), or use **CDI** producers on **Jakarta EE**, **Quarkus**, or **Micronaut**. **Android** is only appropriate if the SDK’s Android policy and dependencies match your app; prefer a dedicated Android artifact if one is published.

## Usage examples

### Single translation entry

**With `TranslaasService`**

```java
var translation = translaas.t("ui", "button.save", LanguageCodes.ENGLISH).join();
var withPlural = translaas.t("messages", "item.count", LanguageCodes.ENGLISH, 5).join();
```

**With `TranslaasClient`**

```java
var translation = client.getEntry("ui", "button.save", LanguageCodes.ENGLISH).join();
var withPlural = client.getEntry("messages", "item.count", LanguageCodes.ENGLISH, 5).join();
```

## JVM compatibility


| Environment               | Notes                                                               |
| ------------------------- | ------------------------------------------------------------------- |
| JDK 11+                   | Baseline for this library                                           |
| JDK 17+ / 21+             | Recommended LTS for new services                                    |
| Virtual threads (JDK 21+) | Use with blocking HTTP APIs or SDK blocking wrappers where provided |


## Error handling

```java
import io.mantelabs.translaas.LanguageCodes;
import io.mantelabs.translaas.client.TranslaasClient;
import io.mantelabs.translaas.client.TranslaasOptions;
import io.mantelabs.translaas.models.exception.TranslaasApiException;

// Example: client constructed from io.mantelabs.translaas.client.TranslaasOptions
TranslaasClient client =
    new TranslaasClient(
        TranslaasOptions.builder()
            .apiKey(System.getenv("TRANSLAAS_API_KEY"))
            .baseUrl("https://api.mantelabs.io")
            .build());

try {
    var translation = client.getEntry("group", "entry", LanguageCodes.ENGLISH).join();
} catch (TranslaasApiException e) {
    System.err.println("Translaas error: " + e.getMessage());
    if (e.getStatusCode() != null) {
        System.err.println("HTTP status: " + e.getStatusCode());
    }
} catch (Exception e) {
    System.err.println("Unexpected error: " + e.getMessage());
}
```

Prefer typed exceptions from the SDK; map HTTP status codes and I/O failures as documented in Javadoc.

## Development

### Build from source

```bash
git clone https://github.com/Mantelabs/translaas-sdk-java.git
cd translaas-sdk-java
./mvnw -q verify
```

The repository is **Maven-first**: use the committed wrapper (`mvnw` / `mvnw.cmd`) so CI and local builds stay aligned. Module layout and commands are summarized in [CONTRIBUTING.md](CONTRIBUTING.md).

### Run tests

```bash
./mvnw test
```

### Coverage (JaCoCo or configured reporter)

```bash
./mvnw verify
# Open target/site/jacoco/index.html when JaCoCo is enabled
```

## API endpoints

The SDK talks to the Translaas HTTP API. `baseUrl` must be the origin only (for example `https://api.mantelabs.io`).


| Endpoint                              | Method | Purpose                                     |
| ------------------------------------- | ------ | ------------------------------------------- |
| `/sdk/v1/translations/text`           | GET    | Single translation entry                    |
| `/sdk/v1/translations/group`          | GET    | All translations for a group                |
| `/sdk/v1/translations/project`        | GET    | All translations for a project              |
| `/sdk/v1/translations/locales`        | GET    | Locales for a project                       |
| `/sdk/v1/translations/report-missing` | POST   | Report missing keys (project-scoped key)    |
| `/sdk/v1/translations/offline-cache`  | GET    | Offline translation ZIP bundle              |
| `/api/v1/api-keys/validate`           | GET    | Validate API key (connectivity / bootstrap) |


Translation routes use GET with query parameters except `report-missing` (JSON body). Optional query flags include `channel`, `v`, and `includeContext` where the API supports them—surface them via options or per-call parameters on the client.

## Authentication

Send the API key using the `X-Api-Key` header (as defined in the OpenAPI spec). Configure it when building options (client or facade), for example:

```java
import io.mantelabs.translaas.TranslaasOptions;

TranslaasOptions options = TranslaasOptions.builder()
    .apiKey(System.getenv("TRANSLAAS_API_KEY"))
    .baseUrl("https://api.mantelabs.io")
    .build();
```

## Examples

Sample projects may live under `examples/` (not always tracked). If present:

```bash
cd examples/basic
./mvnw exec:java -Dexec.mainClass="com.example.Main"
```

Adjust module layout and commands to match the repository once examples are added.

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

## Support

- **Website**: [https://mantelabs.io](https://mantelabs.io)
- **Issues**: [https://github.com/Mantelabs/translaas-sdk-java/issues](https://github.com/Mantelabs/translaas-sdk-java/issues)
- **Documentation**: published on [mantelabs.io](https://mantelabs.io) when available

## Contributing

Contributions are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) for workflow, style, and review expectations.

---

Made for the JVM ecosystem.