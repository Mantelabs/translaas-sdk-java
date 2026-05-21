# Changelog

## [Unreleased]

## [0.3.0-beta-SNAPSHOT] - 2026-05-21

### Added

- `CacheKeyBuilder` aligned with .NET colon-separated cache keys.
- `sdkTranslationsPathPrefix` on `TranslaasOptions` (default `/sdk/v1/translations`).
- `TranslationResponseParsing` for group bare maps and project `flat-json` composite keys.
- `OfflineCacheDownloadResult.isNotModified()` and `notModified()` factory.
- `TranslationResponses` empty-model factories for 204/304 parity.
- Offline stack: `OfflineCacheOptions`, `CachingTranslaasClient`, `SpecFileCacheProvider`, `OfflineZipBundle`, `OfflineCacheSyncService`.
- `PluralResolver`, `ParameterReplacer`, `TranslationEntries` for offline entry resolution.
- `TranslaasTranslationClient` interface; `TranslaasService` forwards bundle/offline APIs.
- Spring Boot `translaas.offline.*` properties and sync service bean.
- `docs/PARITY_CHECKLIST.md` and `examples/java/offline-node`.

### Changed

- HTTP **204** returns entry key (text) or empty models (group/project/locales).
- HTTP **304** without L1 cache returns empty models instead of `null` (breaking for JSON bundle callers).
- `/text` auto-injects query param `N` when plural `n` is set.
- Empty `report-missing` skips the HTTP call.
- API error messages prefer JSON `{ "code", "message" }` when present.
- Version line aligned with JS/Python **0.3.0-beta**.

### Fixed

- Shorthand `/text` entry query keys no longer pollute L1 cache keys as interpolation parameters.
- Uppercase `N` may be passed as an explicit interpolation parameter without conflicting with plural `n`.

### Migration

- L1 cache keys changed from `path + queryString` to `CacheKeyBuilder` format (invalidate in-memory caches).
- JSON bundle methods return empty models on 304 without cache instead of `null`.
- Offline disk layout uses spec §7.6 (`manifest.json` tree), not the legacy hash blob store.

Closes [#47](https://github.com/acuencadev/translaas-sdk-java/issues/47)