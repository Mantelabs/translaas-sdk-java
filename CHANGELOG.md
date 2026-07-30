# Changelog

## [Unreleased]

## [0.5.0-beta] - 2026-06-09

Coordinated **beta** release aligning offline cache behavior and **`TranslaasService.t()`** overloads with the .NET reference implementation and JS/Python **0.5.0** SDK lines.

### Changed

- **`TranslaasService.t()`** — added auto-language overloads for plural count and parameters (parity with .NET `ITranslaasService.T`).
- **`PluralResolver`** — offline cache plural selection uses one/other only (`1 → one`, else `other`; language ignored), matching .NET `CachingTranslaasClient.DeterminePluralCategory`.
- **`ParameterReplacer`** — offline substitution uses `{name}` placeholders only with case-insensitive keys and auto-`N` merge, matching .NET `SubstituteParameters`.
- **`TranslaasLanguageResolver`** — returns ISO-639 language codes via **`Locale#getLanguage()`** (for example `en` from `en_US`) instead of BCP 47 **`toLanguageTag()`**.
- README **Examples** section links to **[translaas-sdk-examples](https://github.com/acuencadev/translaas-sdk-examples)**.

### Added

- **`preferHttp11`** on client and facade **`TranslaasOptions`** — pins JDK **`HttpClient`** to HTTP/1.1 when enabled; Spring Boot maps **`translaas.prefer-http-11`**.
- Tests for offline `{N}` / `{userName}` substitution and README .NET SDK parity section.

## [0.4.0-beta] - 2026-05-22

Coordinated **beta** release aligning the Java SDK with the **Translaas SDK v1** HTTP surface, the .NET reference implementation, and JS/Python **0.3.0-beta** / **0.4.0-beta** SDK lines.

### Added

#### Translaas SDK v1 HTTP API

- Configurable **`sdkTranslationsPathPrefix`** on `TranslaasOptions` (default **`/sdk/v1/translations`**) for `{prefix}/text`, `{prefix}/group`, `{prefix}/project`, `{prefix}/locales`, `{prefix}/report-missing`, and `{prefix}/offline-cache`.
- **`TranslaasTranslationClient`** interface exposing group, project, locales, offline ZIP, report-missing, and API-key validation operations.
- **`SdkTranslationPaths`** helpers for building SDK route paths from options.
- **`TranslationResponseParsing`** for group bare maps and project **`format=flat-json`** composite keys.
- **`TranslationResponses`** factories that return empty models for **204** / **304** bundle responses.
- **`OfflineCacheDownloadResult.isNotModified()`** and **`notModified()`** factory for **304** offline ZIP downloads.
- **`TranslaasApiErrorMessages`** to prefer JSON **`{ "code", "message" }`** bodies in thrown API errors when present.

#### Caching

- **`CacheKeyBuilder`** producing colon-separated L1 keys aligned with .NET (project, group, locale, channel, version, flags, etc.).
- **`TranslationCacheKeys`** and updated **`TranslationResponseCache`** wired to the new key format.
- **`CacheMode`**-aware L1 behavior on read paths (entry, group, project).

#### Offline cache

- **`OfflineCacheOptions`**, **`OfflineHybridCacheOptions`**, and **`OfflineFallbackMode`** for cache-first, API-first, and cache-only flows.
- **`CachingTranslaasClient`** orchestrating L1 memory cache, spec on-disk layout, and live API fallback.
- **`SpecFileCacheProvider`** implementing spec §7.6 on-disk tree layout (`manifest.json`, per-project folders).
- **`OfflineZipBundle.parseOfflineZip`**, **`OfflineCacheSyncService`**, and **`TranslaasClients`** factory helpers.
- **`PluralResolver`**, **`ParameterReplacer`**, and **`TranslationEntries`** for offline entry resolution (nested strings, plural maps via `JsonNode`).
- **`TranslaasOfflineCacheMissException`** when cache-only mode cannot satisfy a lookup.

#### Service layer and Spring Boot

- **`TranslaasService`** forwards group, project, locales, offline ZIP, report-missing, and validate-API-key calls.
- **`defaultProjectId`** on `TranslaasOptions` / `TranslaasProperties` for text lookups without an explicit project per call.
- Spring Boot **`translaas.offline.*`** properties, auto-configured **`OfflineCacheSyncService`**, and offline-aware **`TranslaasAutoConfiguration`**.

#### Documentation

- **`docs/PARITY_CHECKLIST.md`** — sign-off matrix against the HTTP spec and sibling SDKs.

### Changed

#### HTTP semantics (breaking for some callers)

- **`GET /text`**: HTTP **204** returns the requested entry key as the translation text; HTTP **304** returns cached text when L1 is enabled, otherwise the entry key.
- **`GET /group`**, **`GET /project`**, **`GET /locales`**: HTTP **204** and **304** return **empty model instances** instead of **`null`** (callers that checked for `null` on not-modified bundle reads must switch to empty-model checks or **`OfflineCacheDownloadResult.isNotModified()`**).
- **`GET /text`**: when plural count **`n`** is set, query param **`N`** is injected automatically for servers that expect the uppercase form.
- **`POST report-missing`**: an empty key list skips the HTTP request entirely (no-op).
- L1 cache keys changed from **`path + queryString`** to **`CacheKeyBuilder`** colon format — **invalidate in-memory caches** after upgrading.

#### Offline disk layout (breaking)

- On-disk offline bundles use the spec §7.6 **`manifest.json`** tree via **`SpecFileCacheProvider`**, not the legacy hash-blob layout from earlier **`FileCacheProvider`** experiments.

### Fixed

- Shorthand **`/text`** entry query keys (e.g. **`k`**) no longer leak into L1 cache keys as interpolation parameters.
- Uppercase **`N`** may be supplied explicitly as an interpolation parameter without conflicting with lowercase plural **`n`**.
- Group and project translation tests and parsers handle bare maps, **`entries`** envelopes, optional **`channel`**, **`v`**, and **`includeContext`** consistently with OpenAPI examples.
- README corrected (removed inaccurate retry claim); Spring starter tests cover offline property binding.

### Migration

1. Point **`baseUrl`** at your API host; rely on default **`sdkTranslationsPathPrefix`** or set it explicitly during migration from legacy **`/api/translations/...`** paths.
2. Replace **`null`** checks on group/project/locale bundle methods with empty-model semantics or cache **`304`** handling.
3. Clear L1 caches or restart processes after upgrade because cache key format changed.
4. Re-sync offline bundles if you used the pre-spec hash-blob disk layout.
