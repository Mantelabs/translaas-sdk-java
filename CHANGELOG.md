# Changelog

Notable changes to this project are listed here. Versions follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

When you publish a GitHub release, use [`.github/RELEASE_NOTES_TEMPLATE.md`](.github/RELEASE_NOTES_TEMPLATE.md) as the starting point, then move or summarize relevant **Unreleased** items into the release notes for that tag.

## [Unreleased]

### Added

- JaCoCo minimum line coverage gate for `translaas-sdk-client` during `./mvnw verify`.
- Optional Maven profile `integration` with module `translaas-sdk-client-integration-tests` for live API smoke tests (`TRANSLAAS_BASE_URL`, `TRANSLAAS_API_KEY`).
- GitHub Actions workflow to run integration tests manually with repository secrets.

## [0.1.0-SNAPSHOT]

Pre-release development builds; see git history for detail.
