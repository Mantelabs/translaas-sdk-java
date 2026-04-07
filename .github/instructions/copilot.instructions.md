# Translaas SDK (Java) - Copilot Instructions

## Repository Overview

This is an open-source **Java** SDK for the **Translaas Translation Delivery API**. The SDK provides a strongly typed, modular way to consume translation APIs on the JVM. The backend API is proprietary; this client is open source for community contributions.

**Repository type**: Java SDK library  
**Languages**: Java  
**JDK**: **11+** (baseline; match `maven-compiler-plugin` / CI matrix)  
**Coordinates**: `io.mantelabs` (reverse-DNS for [mantelabs.io](https://mantelabs.io))  
**Base package**: `io.mantelabs.translaas`  
**Build system**: **Maven** (prefer `./mvnw` when the wrapper is present)  
**Tests**: **JUnit 5** (JUnit Jupiter), **Mockito** / **AssertJ** as configured in the parent POM  
**API parity reference**: `.docs/sdk-features-checklist.md`

## High-Level Architecture

The SDK is organized as **Maven modules** (exact names may vary—follow the repo):

- **translaas-sdk-models** — DTOs, enums; JDK-only / minimal dependencies
- **translaas-sdk-client** — HTTP client, headers (`X-Api-Key`), mapping to models
- **translaas-sdk-caching** — Cache abstractions and in-memory implementations
- **translaas-sdk-caching-file** — File / offline caching (depends on client + models)
- **translaas-sdk** (optional) — Aggregator or convenience API
- **translaas-sdk-spring-boot-starter** (optional) — Spring Boot integration if maintained

Tests live under each module: `src/test/java`, mirroring `src/main/java` packages.

## Build and Validation Instructions

### Prerequisites

- **JDK 11** or later (use the same major version(s) as CI when possible)
- **Maven** 3.9+ (or only the **Maven Wrapper** `./mvnw` / `mvnw.cmd`)

### Build (no install)

From the repository root:

```bash
./mvnw -q -DskipTests package
```

On Windows:

```bat
mvnw.cmd -q -DskipTests package
```

### Test

**Run all tests:**

```bash
./mvnw -q test
```

**Run tests for a single module:**

```bash
./mvnw -q -pl translaas-sdk-client test
```

**Run with coverage** (when JaCoCo is configured on `verify`):

```bash
./mvnw -q verify
```

**Run a single test class** (example):

```bash
./mvnw -q -pl translaas-sdk-client -Dtest=TranslaasClientTest test
```

**Important**: Tests must pass on supported JDKs. Run `./mvnw test` or `./mvnw verify` before committing.

### Static Analysis and Formatting

Commands depend on what the parent POM configures. Typical additions:

- **Checkstyle** or **Spotless** — `mvnw spotless:check` or `mvnw checkstyle:check` if present
- **Error Prone** / **NullAway** — if enabled in the build

Run whatever **validate** or **verify** phase the project documents in `pom.xml` or CI.

### Validation Steps (before committing)

1. **Compile and test**: `./mvnw -q verify` (or at least `./mvnw -q test`)
2. **Static analysis**: run configured Checkstyle/Spotless/Error Prone goals if the project uses them
3. **Coverage**: ensure JaCoCo thresholds (if any) pass when running `verify`

**CI pipeline**

When `.github/workflows/ci.yml` runs: **PRs and pushes to `main`** use **Ubuntu only** (JDK 11, 17, 21). **Windows** and **macOS** are **separate jobs** (`build-windows-manual`, `build-macos-manual`) and run only when you **manually** run the workflow (`Actions` → `CI` → `Run workflow`). Align local JDK with CI when debugging failures.

## Project Layout and File Management

### Critical synchronization rules

**When adding or removing modules or dependencies:**

- Update the **parent** `pom.xml` `<modules>` list
- Update **dependencyManagement** / **BOM** if versions are centralized
- If using **JPMS**, update each `module-info.java` `requires` / `exports`
- Add or adjust tests under `src/test/java` for new production code

**When adding a new Java type:**

- Place it under `src/main/java/io/mantelabs/translaas/...` (or the module’s agreed package root)
- Add **JUnit 5** tests under the parallel test tree
- Expose public API only from documented packages; keep helpers in `internal` or package-private types

**When removing code:**

- Remove or rewrite tests; delete dead modules from the parent POM

### Directory structure (illustrative)

```
translaas-sdk-java/
├── .github/
│   ├── workflows/
│   │   └── ci.yml                    # CI (when present)
│   └── instructions/
│       └── copilot.instructions.md   # This file
├── .cursor/
│   └── rules/
│       └── translaas-sdk-rules.mdc   # Detailed Java SDK rules
├── .docs/
│   └── sdk-features-checklist.md
├── pom.xml                           # Parent POM
├── translaas-sdk-models/
│   ├── pom.xml
│   └── src/main/java/io/mantelabs/translaas/...
├── translaas-sdk-client/
│   ├── pom.xml
│   ├── src/main/java/...
│   └── src/test/java/...
├── ...                               # Other modules
├── README.md
├── CONTRIBUTING.md                   # If present
└── LICENSE
```

### Key configuration files

- **`pom.xml`** (root and modules): dependencies, plugins, Java `release`, test setup
- **`.github/workflows/ci.yml`**: CI jobs (when present)
- **`.cursor/rules/translaas-sdk-rules.mdc`**: Extended conventions (TDD, HTTP semantics, modules)

## Development Guidelines

### Test-Driven Development (TDD) — mandatory

1. **Red**: Failing test that specifies behavior  
2. **Green**: Minimal implementation to pass  
3. **Refactor**: Clean up while tests stay green  

**Rules:**

- Prefer writing or updating tests **before** or **with** implementation for new behavior
- Every new public API should have tests
- Name tests clearly, e.g. `getEntry_returnsText_whenHttpReturns200`
- Cover success paths, HTTP errors, timeouts, and invalid configuration

### Code style

- Follow **Google Java Style** (or project Checkstyle/Spotless), consistent with existing code
- Use **`java.time.Duration`** / **`Instant`** for time configuration where appropriate
- Prefer **`java.net.http.HttpClient`** or the project’s chosen HTTP stack consistently
- Use **`Optional`** for absent values in public APIs when it clarifies contracts
- **Javadoc** required for public classes and members in published API packages

### JDK support

- Baseline **JDK 11** unless the build raises it—do not use newer language features without guarding `release` / `--release`
- Test on the same JDK line(s) as CI when possible

### Dependencies

- Add dependencies only with clear justification; prefer JDK APIs
- JSON: **Jackson** or **Gson**—use one stack consistently
- Keep **models** free of unnecessary third-party compile dependencies

### Error handling

- Use **`TranslaasException`**, **`TranslaasApiException`**, **`TranslaasConfigurationException`** (or the hierarchy defined in the repo)
- Do not log API keys; preserve interrupt status on **`InterruptedException`**
- Map HTTP failures to typed exceptions with status codes when available

### HTTP semantics

Align with **OpenAPI** and `.docs/sdk-features-checklist.md`: correct paths under `/sdk/v1/translations/...`, **`X-Api-Key`**, GET vs POST, and content types (plain text, JSON, ZIP).

## Common Commands Reference

**Full verify (tests + typical plugins):**

```bash
./mvnw -q verify
```

**Tests only:**

```bash
./mvnw -q test
```

**Single module:**

```bash
./mvnw -q -pl translaas-sdk-client test
```

**Skip tests (compile only):**

```bash
./mvnw -q -DskipTests package
```

**Clean:**

```bash
./mvnw -q clean
```

## Pre-Commit Checklist

- [ ] TDD: tests added/updated for new behavior  
- [ ] `./mvnw verify` (or at least `test`) passes locally  
- [ ] Javadoc updated for new or changed public API  
- [ ] No secrets or API keys in code or tests  
- [ ] New dependencies justified and declared in the correct `pom.xml`  
- [ ] Module / parent POM updated if modules or dependencyManagement changed  

## Trust Instructions

**Trust these instructions** as the default workflow for this Java repository. Search the codebase when:

- Module names, plugin IDs, or CI commands differ from this document  
- You need the exact package layout or `pom.xml` structure  
- Instructions appear outdated relative to the current build  

Use `.cursor/rules/translaas-sdk-rules.mdc` and the live `pom.xml` files as the detailed source of truth when in doubt.
