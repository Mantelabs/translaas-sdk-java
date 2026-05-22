# Maven Central setup and release guide

Quick reference for publishing **`io.mantelabs:*`** artifacts from [translaas-sdk-java](https://github.com/acuencadev/translaas-sdk-java).

## Prerequisites

### 1. Sonatype Central (OSSRH)

1. Create a [Sonatype Central](https://central.sonatype.com/) account and register namespace **`io.mantelabs`**.
   - Verify ownership of **`mantelabs.com`** (DNS TXT record on the domain) or link your GitHub org if Central accepts that path for the namespace.
   - The root POM **`organizationUrl`** is **`https://mantelabs.com`**; keep Central namespace metadata aligned with that domain.
2. Generate a **user token** for publishing (Central Portal → Account → Generate user token).
3. Store the token as GitHub Environment secrets on **`translaas-sdk-java`**:

| Secret | Purpose |
|--------|---------|
| `MAVEN_USERNAME` | Central user token username |
| `MAVEN_PASSWORD` | Central user token password |
| `MAVEN_GPG_PRIVATE_KEY` | ASCII-armored private key for artifact signing |
| `MAVEN_GPG_PASSPHRASE` | Passphrase for the GPG key |

Create GitHub Environment **`maven-central`** (Settings → Environments) and attach the secrets above. Restrict deployment to **`main`** and tags matching **`v*`** when you are ready for production-only publishes.

### 2. POM requirements (in repo)

The root **`pom.xml`** provides:

- **`distributionManagement`** with server id **`ossrh`**
- **`licenses`**, **`scm`**, **`developers`**, **`url`**
- **`release`** Maven profile: sources, Javadoc, GPG signing, Nexus staging (`autoReleaseAfterClose`)

Local **`./mvnw verify`** does **not** sign or deploy. CI uses **`./mvnw deploy -Prelease -DskipTests`**.

---

## Release checklist

1. Merge the release PR (version **`0.4.0-beta`**, non-SNAPSHOT, in all POMs).
2. Confirm CI is green on **`main`**.
3. Tag and publish the GitHub Release (see below).
4. Monitor the **Release** workflow → **Publish to Maven Central** job.
5. Verify artifacts on [Maven Central](https://central.sonatype.com/search?q=g:io.mantelabs).
6. Open a follow-up PR bumping **`main`** to the next dev snapshot (e.g. **`0.4.1-beta-SNAPSHOT`**).

---

## Option A — release script (recommended)

From the repository root, on **`main`**, after the release PR is merged:

**PowerShell (Windows):**

```powershell
.\scripts\publish-release.ps1 -Version 0.4.0-beta
```

**Bash (Linux/macOS):**

```bash
./scripts/publish-release.sh 0.4.0-beta
```

The script:

1. Verifies the working tree is clean and **`main`** matches **`origin/main`**
2. Confirms the root POM version matches the release (no **`-SNAPSHOT`**)
3. Creates and pushes git tag **`v0.4.0-beta`**
4. Creates a GitHub Release using **`RELEASE_NOTES_v0.4.0-beta.md`**
5. Optionally triggers **`release.yml`** with **`publish_to_maven_central=true`**

Publishing the GitHub Release also triggers deploy via the **`release: published`** event.

---

## Option B — manual steps

```bash
git checkout main
git pull
git tag v0.4.0-beta
git push origin v0.4.0-beta

gh release create v0.4.0-beta \
  --title "Release v0.4.0-beta — SDK v1 API parity (beta)" \
  --notes-file RELEASE_NOTES_v0.4.0-beta.md
```

Or dispatch the workflow without a GitHub Release:

```bash
gh workflow run release.yml \
  --repo acuencadev/translaas-sdk-java \
  -f publish_to_maven_central=true \
  -f version=0.4.0-beta
```

---

## Dry run (build only)

```bash
./mvnw -B -ntp verify
```

From **translaas-all**:

```powershell
.\scripts\release-sdk.ps1 -Target java -Version "0.4.0-beta"
```

(`publish=false` — build/verify only in the Java repo.)

---

## Troubleshooting

| Symptom | Likely cause |
|---------|----------------|
| Deploy fails: unauthorized | Missing or wrong **`MAVEN_USERNAME`** / **`MAVEN_PASSWORD`** |
| GPG sign failure | **`MAVEN_GPG_PRIVATE_KEY`** or passphrase incorrect |
| Version rejected | POM still contains **`-SNAPSHOT`** |
| Namespace not found | **`io.mantelabs`** not verified in Sonatype Central |
| Staging not released | Check Nexus staging plugin logs; **`autoReleaseAfterClose`** should close and release automatically |

See also [`.docs/sdk-release-runbook.md`](../../../.docs/sdk-release-runbook.md) in **translaas-all** for orchestrated multi-SDK releases.
