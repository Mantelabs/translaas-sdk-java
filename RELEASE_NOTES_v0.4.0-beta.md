# Release v0.4.0-beta — SDK v1 API parity (beta)

## Overview

First **Maven Central** beta for the Java SDK at **`0.4.0-beta`**. Aligns with the **Translaas SDK v1** HTTP surface (`/sdk/v1/translations`, API key validation, report-missing, offline ZIP bundles) and ships the full caching and offline stack.

## Artifacts published

- **`io.mantelabs:translaas-sdk:0.4.0-beta`** — convenience aggregator (recommended)
- **`io.mantelabs:translaas-sdk-client:0.4.0-beta`**
- **`io.mantelabs:translaas-sdk-models:0.4.0-beta`**
- **`io.mantelabs:translaas-sdk-caching:0.4.0-beta`**
- **`io.mantelabs:translaas-sdk-caching-file:0.4.0-beta`**
- **`io.mantelabs:translaas-sdk-spring-boot-starter:0.4.0-beta`**
- **`io.mantelabs:translaas-sdk-thymeleaf-spring-boot-starter:0.4.0-beta`**
- **`io.mantelabs:translaas-sdk-parent:0.4.0-beta`** — parent POM

Search: [Maven Central — io.mantelabs](https://central.sonatype.com/search?q=g:io.mantelabs)

## Install

**Maven:**

```xml
<dependency>
  <groupId>io.mantelabs</groupId>
  <artifactId>translaas-sdk</artifactId>
  <version>0.4.0-beta</version>
</dependency>
```

**Gradle (Kotlin DSL):**

```kotlin
dependencies {
    implementation("io.mantelabs:translaas-sdk:0.4.0-beta")
}
```

## Highlights

- **SDK v1 routes** — configurable `sdkTranslationsPathPrefix` (default `/sdk/v1/translations`)
- **Caching** — `CacheKeyBuilder` colon keys aligned with .NET; L1 memory cache modes
- **Offline** — `CachingTranslaasClient`, spec §7.6 on-disk layout, plural/parameter resolution
- **Spring Boot** — `translaas.offline.*` properties and auto-configured sync service
- **Breaking** — empty models on 204/304 bundle reads (not `null`); new L1 cache key format; new offline disk layout

## Changelog

Full details: **[CHANGELOG.md](https://github.com/acuencadev/translaas-sdk-java/blob/v0.4.0-beta/CHANGELOG.md)** — section **`[0.4.0-beta]`**.

## Migration

1. Set **`baseUrl`** to your API host; use default or explicit **`sdkTranslationsPathPrefix`**.
2. Replace **`null`** checks on bundle methods with empty-model semantics.
3. Invalidate L1 caches after upgrade (key format changed).
4. Re-sync offline bundles if you used the legacy hash-blob layout.
