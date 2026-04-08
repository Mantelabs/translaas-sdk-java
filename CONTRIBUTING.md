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

- **Parent POM** — `io.mantelabs:translaas-sdk-parent` at the repo root.
- **Modules** — `translaas-sdk-models`, `translaas-sdk-client`, `translaas-sdk-caching`, `translaas-sdk-caching-file`, and the convenience aggregate `translaas-sdk`.
- **Base package** — `io.mantelabs.translaas` (with subpackages per module where it helps). The HTTP client and its options builder live under **`io.mantelabs.translaas.client`**; the **`translaas-sdk`** aggregator may add facade types under **`io.mantelabs.translaas`** (see root **README.md** package table). Prefer that split over duplicating `TranslaasClient` in the root package.

## Tests and coverage

- Unit tests use **JUnit 5**, **AssertJ**, and **Mockito** where needed.
- **`./mvnw verify`** runs tests and generates **JaCoCo** HTML/XML under each module’s `target/site/jacoco/` when applicable.

## Pull requests

- Prefer focused changes with clear commit messages.
- Ensure `./mvnw verify` passes before opening a PR.
- Link related GitHub issues when relevant.
