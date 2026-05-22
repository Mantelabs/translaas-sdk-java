#!/usr/bin/env bash
# Tag, GitHub Release, and optional Maven Central publish for translaas-sdk-java.
#
# Usage:
#   ./scripts/publish-release.sh 0.4.0-beta [--skip-workflow]

set -euo pipefail

VERSION="${1:?Usage: $0 VERSION [--skip-workflow]}"
SKIP_WORKFLOW=false
if [[ "${2:-}" == "--skip-workflow" ]]; then
  SKIP_WORKFLOW=true
fi

TAG="v${VERSION}"
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
NOTES_FILE="${REPO_ROOT}/RELEASE_NOTES_v${VERSION}.md"
POM_FILE="${REPO_ROOT}/pom.xml"

cd "${REPO_ROOT}"

if [[ ! -f "${NOTES_FILE}" ]]; then
  echo "Release notes not found: ${NOTES_FILE}" >&2
  exit 1
fi

BRANCH="$(git rev-parse --abbrev-ref HEAD)"
if [[ "${BRANCH}" != "main" ]]; then
  echo "Checkout main before releasing (current: ${BRANCH})." >&2
  exit 1
fi

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Working tree is not clean." >&2
  exit 1
fi

git fetch origin main
LOCAL_HEAD="$(git rev-parse HEAD)"
REMOTE_HEAD="$(git rev-parse origin/main)"
if [[ "${LOCAL_HEAD}" != "${REMOTE_HEAD}" ]]; then
  echo "Local main does not match origin/main. Pull first." >&2
  exit 1
fi

POM_VERSION="$(grep -m1 '<version>' "${POM_FILE}" | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d '[:space:]')"
if [[ "${POM_VERSION}" != "${VERSION}" ]]; then
  echo "Root pom.xml version '${POM_VERSION}' does not match '${VERSION}'." >&2
  exit 1
fi
if [[ "${POM_VERSION}" == *SNAPSHOT* ]]; then
  echo "Release version must not contain SNAPSHOT." >&2
  exit 1
fi

echo "Creating tag ${TAG} ..."
git tag "${TAG}"
git push origin "${TAG}"

echo "Creating GitHub Release ${TAG} ..."
gh release create "${TAG}" \
  --repo acuencadev/translaas-sdk-java \
  --title "Release ${TAG} — SDK v1 API parity (beta)" \
  --notes-file "${NOTES_FILE}"

if [[ "${SKIP_WORKFLOW}" == "false" ]]; then
  echo "Triggering release.yml (publish_to_maven_central=true) ..."
  gh workflow run release.yml \
    --repo acuencadev/translaas-sdk-java \
    -f publish_to_maven_central=true \
    -f "version=${VERSION}"
fi

echo "Done. Monitor Actions on acuencadev/translaas-sdk-java and Maven Central."
