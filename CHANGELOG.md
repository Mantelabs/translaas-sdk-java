# Changelog

## [Unreleased]

### Added

- `CacheKeyBuilder` aligned with .NET colon-separated cache keys.
- `sdkTranslationsPathPrefix` on `TranslaasOptions` (default `/sdk/v1/translations`).
- `TranslationResponseParsing` for group bare maps and project `flat-json` composite keys.
- `OfflineCacheDownloadResult.isNotModified()` and `notModified()` factory.
- `TranslationResponses` empty-model factories for 204/304 parity.

### Changed

- HTTP **204** returns entry key (text) or empty models (group/project/locales).
- HTTP **304** without L1 cache returns empty models instead of `null` (breaking for JSON bundle callers).
- `/text` auto-injects query param `N` when plural `n` is set.
- Empty `report-missing` skips the HTTP call.
- API error messages prefer JSON `{ "code", "message" }` when present.

### Fixed

- Shorthand `/text` entry query keys no longer pollute L1 cache keys as interpolation parameters.
- Uppercase `N` may be passed as an explicit interpolation parameter without conflicting with plural `n`.

### Tracking

- Part of [#47](https://github.com/acuencadev/translaas-sdk-java/issues/47) — Phase A (contract correctness) complete; Phases B–C remain.
