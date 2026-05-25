# Tag, GitHub Release, and optional Maven Central publish for translaas-sdk-java.
#
# Usage:
#   .\scripts\publish-release.ps1 -Version "0.4.0-beta" [-SkipWorkflow]

param(
    [Parameter(Mandatory = $true)]
    [string] $Version,

    [switch] $SkipWorkflow
)

$ErrorActionPreference = "Stop"

$tag = "v$Version"
$repoRoot = Split-Path -Parent $PSScriptRoot
$notesFile = Join-Path $repoRoot "RELEASE_NOTES_v$Version.md"
$pomFile = Join-Path $repoRoot "pom.xml"

Set-Location $repoRoot

if (-not (Test-Path $notesFile)) {
    throw "Release notes not found: $notesFile"
}

$branch = git rev-parse --abbrev-ref HEAD
if ($branch -ne "main") {
    throw "Checkout main before releasing (current: $branch)."
}

$status = git status --porcelain
if ($status) {
    throw "Working tree is not clean. Commit or stash changes first."
}

git fetch origin main
$localHead = git rev-parse HEAD
$remoteHead = git rev-parse origin/main
if ($localHead -ne $remoteHead) {
    throw "Local main ($localHead) does not match origin/main ($remoteHead). Pull first."
}

$pomVersion = ([xml](Get-Content $pomFile)).project.version
if ($pomVersion -ne $Version) {
    throw "Root pom.xml version '$pomVersion' does not match -Version '$Version'."
}
if ($pomVersion -match "SNAPSHOT") {
    throw "Release version must not contain SNAPSHOT (current: $pomVersion)."
}

Write-Host "Creating tag $tag ..."
git tag $tag
git push origin $tag

Write-Host "Creating GitHub Release $tag ..."
gh release create $tag `
    --repo acuencadev/translaas-sdk-java `
    --title "Release $tag — SDK v1 API parity (beta)" `
    --notes-file $notesFile

if (-not $SkipWorkflow) {
    Write-Host "Triggering release.yml (publish_to_maven_central=true) ..."
    gh workflow run release.yml `
        --repo acuencadev/translaas-sdk-java `
        -f publish_to_maven_central=true `
        -f version=$Version
}

Write-Host "Done. Monitor Actions on acuencadev/translaas-sdk-java and Maven Central."
