# Translaas SDK Client Integration Tests

Optional live API tests for the Java SDK. These tests run against a real Translaas API instance (local Docker or hosted).

## Prerequisites

- A running Translaas API instance
- Valid project-scoped API key

## Configuration

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `TRANSLAAS_API_KEY` | **Yes** to run | — | Raw `X-Api-Key` value |
| `TRANSLAAS_BASE_URL` | No | `https://api.translaas.local` | API origin only (no `/api` or `/sdk` suffix) |
| `TRANSLAAS_DEFAULT_PROJECT` | No | `translaas-sdk-samples` | Project id for scoped reads |

**Note:** Do **not** append `/api` to the base URL — the client adds `/sdk/v1/translations/...` and `/api/v1/...` automatically.

## Running

### Windows (PowerShell)

```powershell
$env:TRANSLAAS_API_KEY = "your-api-key-here"
$env:TRANSLAAS_BASE_URL = "https://api.translaas.local"  # Optional
$env:TRANSLAAS_DEFAULT_PROJECT = "translaas-sdk-samples"  # Optional
./mvnw -Pintegration verify -pl translaas-sdk-client-integration-tests -am
```

### Linux/macOS (Bash)

```bash
export TRANSLAAS_API_KEY="your-api-key-here"
export TRANSLAAS_BASE_URL="https://api.translaas.local"  # Optional
export TRANSLAAS_DEFAULT_PROJECT="translaas-sdk-samples"  # Optional
./mvnw -Pintegration verify -pl translaas-sdk-client-integration-tests -am
```

## Test behavior

- **If `TRANSLAAS_API_KEY` is not set**: Live test classes are skipped; `./mvnw verify` (without `-Pintegration`) still passes with no secrets.
- **If `TRANSLAAS_API_KEY` is set**: Tests run against the configured API. Happy-path tests **soft-skip** when fixture data is missing (HTTP 404 on Mantelabs platform, or empty/204 responses on legacy APIs).

## Local Docker (`platform/translaas`)

After `docker compose --profile core up -d`, use the same API origin as platform `.env.example`:

```powershell
$env:TRANSLAAS_API_KEY = "<your-sdk-api-key>"
./mvnw -Pintegration verify -pl translaas-sdk-client-integration-tests -am
```

## Fixture data

Canonical strings live in [translaas-sdk-examples `translaas_sdk_samples_strings.csv`](https://github.com/Mantelabs/translaas-sdk-examples/blob/main/dotnet/docs/translaas_sdk_samples_strings.csv):

| Field | Value |
|-------|-------|
| Project | `translaas-sdk-samples` |
| Group (simple entry) | `common` |
| Entry (simple) | `welcome.message` |
| Group (plural) | `messages` |
| Entry (plural) | `item` |
| Language | `en` (optional: `fr`, `es`, `de`) |

Example SDK URL:

`GET /sdk/v1/translations/text?project=translaas-sdk-samples&group=common&lang=en&entry=welcome.message`

## Coverage (Go/.NET/Rust parity)

| Area | Test class |
|------|------------|
| Validate API key | `ValidateApiKeyLiveTest` |
| Get entry (existing, plural, not-found, invalid key) | `GetEntryLiveTest` |
| Get group translations | `GetGroupTranslationsLiveTest` |
| Get project translations | `GetProjectTranslationsLiveTest` |
| Get project locales | `GetProjectLocalesLiveTest` |
| Service `t()` with explicit language | `TranslaasServiceLiveTest` |
| Error scenarios (invalid URL, timeout, auth) | `ErrorScenariosLiveTest` |

## API behavior

| Endpoint | Missing resource | Legacy fixture API | Mantelabs platform | Integration test |
|----------|------------------|--------------------|--------------------|------------------|
| `getEntry` | not found | 204 → entry key fallback | 404 `TranslaasApiException` | Accepts 204 fallback or 404 |
| `getGroupTranslations` / `getProjectTranslations` / `getProjectLocales` | not found | 204 → empty container | 404 `TranslaasApiException` | Accepts empty container or 404 |
| Invalid API key | auth failure | 401/403 | 401/403 | `TranslaasApiException` |

## CI/CD

These tests are **optional**. Default CI runs unit tests only. Use the **Integration tests** workflow (`.github/workflows/integration-tests.yml`) manually with repository secrets `TRANSLAAS_BASE_URL` and `TRANSLAAS_API_KEY`.
