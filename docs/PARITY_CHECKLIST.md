# Java SDK v1 parity checklist

Sign-off for **0.4.0-beta** parity with the Translaas SDK HTTP API, .NET reference, and JS/Python **0.3.0-beta** / **0.4.0-beta** SDKs.

Reference: [translaas-sdk-java-parity-change-plan.md](../../../.docs/translaas-sdk-java-parity-change-plan.md)

## HTTP and auth

- [x] Configurable `{prefix}` defaulting to `/sdk/v1/translations`
- [x] `X-Api-Key` on SDK routes; validate on `/api/v1/api-keys/validate`
- [x] `channel`, `v`, `includeContext` query rules
- [x] `If-None-Match` / weak ETag on `TranslaasRequestContext`

## Reads

- [x] `/text` plural `n` + auto `N`
- [x] `/text` 204 / 304 semantics
- [x] `/group`, `/project`, `/locales` 204 / 304 empty models
- [x] `format=flat-json` parsing helpers
- [x] Nested string / plural map via `JsonNode` + `TranslationEntries`

## Writes / downloads

- [x] `report-missing` 202; empty keys skip HTTP
- [x] Offline ZIP download with `notModified`
- [x] `OfflineZipBundle.parseOfflineZip`

## Caching

- [x] `CacheKeyBuilder` colon keys
- [x] `CacheMode` L1 behavior
- [x] `CachingTranslaasClient` cache-first / api-first / cache-only
- [x] Spec on-disk tree (`SpecFileCacheProvider`)

## Service and Spring

- [x] `defaultProject` for text
- [x] `TranslaasService` forwards group/project/locales/offline/report/validate
- [x] Spring `translaas.offline.*` + auto-config

## Docs

- [x] README accurate (no false retry claim)
- [x] CHANGELOG migration notes for cache keys, 304 JSON, offline layout

## Sign-off

| Role | Version | Date |
|------|---------|------|
| Implementation | 0.4.0-beta | 2026-05-22 |
