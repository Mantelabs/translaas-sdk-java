# Release v0.5.0-beta — .NET parity (t(), offline cache, HTTP/1.1)

## Overview

Beta release at **`0.5.0-beta`** on **`0.4.0-beta`**. Aligns **`TranslaasService.t()`** overloads and offline cache plural/parameter resolution with the .NET SDK, adds optional HTTP/1.1 pinning for JDK **`HttpClient`**, and fixes Spring locale resolution to ISO-639 language codes. Coordinated with Python **`0.5.0b1`** and JS **`0.5.0-beta`**.

## Artifacts published

- **`io.translaas:translaas-sdk:0.5.0-beta`** — convenience aggregator (recommended)
- **`io.translaas:translaas-sdk-client:0.5.0-beta`**
- **`io.translaas:translaas-sdk-models:0.5.0-beta`**
- **`io.translaas:translaas-sdk-caching:0.5.0-beta`**
- **`io.translaas:translaas-sdk-caching-file:0.5.0-beta`**
- **`io.translaas:translaas-sdk-spring-boot-starter:0.5.0-beta`**
- **`io.translaas:translaas-sdk-thymeleaf-spring-boot-starter:0.5.0-beta`**
- **`io.translaas:translaas-sdk-parent:0.5.0-beta`** — parent POM

Search: [Maven Central — io.translaas](https://central.sonatype.com/search?q=g:io.translaas)

## Install

**Maven:**

```xml
<dependency>
  <groupId>io.translaas</groupId>
  <artifactId>translaas-sdk</artifactId>
  <version>0.5.0-beta</version>
</dependency>
```

**Gradle (Kotlin DSL):**

```kotlin
dependencies {
    implementation("io.translaas:translaas-sdk:0.5.0-beta")
}
```

## Highlights

- **`TranslaasService.t()`** — auto-language overloads for plural count and interpolation parameters (parity with .NET **`ITranslaasService.T`**)
- **Offline cache** — **`PluralResolver`** uses one/other only; **`ParameterReplacer`** supports **`{name}`** placeholders only
- **`preferHttp11`** — optional HTTP/1.1 for JDK **`HttpClient`**; Spring Boot **`translaas.prefer-http-11`**
- **Spring locale** — **`TranslaasLanguageResolver`** returns ISO-639 codes (**`en`**) instead of BCP 47 tags (**`en-US`**)

## Migration

1. **Offline cache-only** callers: plural selection is now strictly one/other; language-specific offline rules are removed.
2. **Offline placeholders**: only **`{name}`** is supported; **`{{name}}`** and **`%name%`** are no longer substituted.
3. **Spring apps**: verify locale resolution if you relied on full BCP 47 tags from the resolver.
4. **Proxies / HTTP/2 issues**: set **`translaas.prefer-http-11: true`** when needed.

## Changelog

Full details: **[CHANGELOG.md](https://github.com/acuencadev/translaas-sdk-java/blob/v0.5.0-beta/CHANGELOG.md)** — section **`[0.5.0-beta]`**.

**Compare:** https://github.com/acuencadev/translaas-sdk-java/compare/v0.4.0-beta...v0.5.0-beta
