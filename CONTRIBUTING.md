# Contributing

## Prerequisites

- **JDK 11** or newer (CI also runs Temurin 17 and 21 on Ubuntu).
- Git.

## Build

From the repository root:

```bash
./mvnw -q verify
```

On Windows (PowerShell or cmd):

```cmd
mvnw.cmd -q verify
```

Use the **Maven Wrapper** (`mvnw` / `mvnw.cmd`) so the build matches CI and you do not need a global Maven install.

## Layout

- **Parent POM** — `com.mantelabs:translaas-sdk-parent` at the repo root.
- **Modules** — `translaas-sdk-models`, `translaas-sdk-client`, `translaas-sdk-caching`, `translaas-sdk-caching-file`, and the convenience aggregate `translaas-sdk`.
- **Base package** — `io.mantelabs.translaas` (with subpackages per module where it helps). The HTTP client and its options builder live under **`io.mantelabs.translaas.client`**; the **`translaas-sdk`** aggregator may add facade types under **`io.mantelabs.translaas`** (see root **README.md** package table). Prefer that split over duplicating `TranslaasClient` in the root package.

## Tests and coverage

- Unit tests use **JUnit 5**, **AssertJ**, and **Mockito** where needed. HTTP-level tests in `translaas-sdk-client` use **WireMock** (`@WireMockTest`) for request/response shaping without a real backend.
- **`./mvnw verify`** runs unit tests, enforces a **JaCoCo line-coverage minimum** on `translaas-sdk-client`, and generates JaCoCo HTML/XML under each module’s `target/site/jacoco/` (except modules that skip JaCoCo).
- **Optional live integration tests** (real Translaas API): set environment variables **`TRANSLAAS_BASE_URL`** (API origin, e.g. `https://your-tenant.translaas.io`) and **`TRANSLAAS_API_KEY`** (project-scoped key). Never put secrets in source control. Then run:
  ```bash
  export TRANSLAAS_BASE_URL="https://..."
  export TRANSLAAS_API_KEY="..."
  ./mvnw -Pintegration verify -pl translaas-sdk-client-integration-tests -am
  ```
  On PowerShell, set `$env:TRANSLAAS_BASE_URL` and `$env:TRANSLAAS_API_KEY` instead of `export`.
  If those variables are unset, the live test class is skipped and the build still succeeds. In **GitHub Actions**, use repository secrets with the same names and run the **Integration tests** workflow manually (`.github/workflows/integration-tests.yml`).
- User-facing release notes for maintainers: see **`CHANGELOG.md`** and [`.github/RELEASE_NOTES_TEMPLATE.md`](.github/RELEASE_NOTES_TEMPLATE.md).
- Maven Central setup and publish steps: **[`docs/MAVEN_CENTRAL_SETUP.md`](docs/MAVEN_CENTRAL_SETUP.md)**.

## Pull requests

- Prefer focused changes with clear commit messages.
- Ensure `./mvnw verify` passes before opening a PR.
- Link related GitHub issues when relevant.
