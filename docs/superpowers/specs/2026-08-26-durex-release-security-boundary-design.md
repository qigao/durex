# Durex Release Security Boundary Design

## Goal

Make the release trust boundary structural before `v0.1.0`: pull-request/manual verification must be read-only and secret-free, while Maven Central publication is possible only from an exact release tag in a separate privileged workflow.

## Current problem

`.github/workflows/durex-release.yml` currently handles pull requests, manual dry-runs, and tag publication in one job. That job declares `contents: write` and release credentials at job scope. Conditional shell logic prevents upload during a dry-run, but the workflow shape itself does not express the intended privilege separation.

## Design

### Verification workflow

Create `.github/workflows/durex-release-verify.yml` named `Durex Release Verification`.

It runs on the existing release-sensitive pull-request paths and by manual dispatch. It declares only `contents: read`, has no Central/OpenPGP secrets in workflow or job environment, and performs the current dry-run contract:

1. validate a stable `X.Y.Z` version;
2. require the canonical Apache-2.0 license;
3. publish the four public artifacts unsigned to an isolated Maven repository;
4. require generated release signatures to equal the committed 0.1 baseline;
5. compile the Maven-only consumer;
6. assemble and inspect an unsigned Central-layout bundle;
7. record the tested SHA without network publication.

### Publication workflow

Keep `.github/workflows/durex-release.yml` named `Durex Release`, but make it tag-only (`vX.Y.Z`). It retains `contents: write` because it creates the immutable GitHub Release after Central publication. Signing and Central credentials exist only in this tag-only job.

The workflow must require:

- `GITHUB_REF_TYPE == tag`;
- `GITHUB_REF_NAME == v$version`;
- the tag commit equals `GITHUB_SHA`;
- signing key and Central credentials are non-empty before staging/upload;
- the staged release signatures exactly equal the committed 0.1 baseline;
- Central reaches `VALIDATED`, then `PUBLISHED`, before the GitHub Release is created.

Manual publication is intentionally removed. A real network publication is triggered only by pushing the exact release tag.

### Supply-chain pinning

All third-party actions used by these release workflows are pinned to immutable commit SHAs:

- `actions/checkout`: `11d5960a326750d5838078e36cf38b85af677262`
- `actions/setup-java`: `b6effb05e454b25005698d916606bdc6ffcbf961`
- `gradle/actions/setup-gradle`: `ed408507eac070d1f99cc633dbcf757c94c7933a`

Human-readable comments may note the corresponding major release, but mutable `@v*` references are not accepted in the release workflows.

### Executable security contract

Add `scripts/verify-release-security-boundary.sh` and run it from a small pull-request workflow. It rejects:

- a missing verification workflow;
- `pull_request` or `workflow_dispatch` triggers in the privileged publication workflow;
- `contents: write` in the verification workflow;
- signing/Central secret names in the verification workflow;
- mutable third-party action refs in either release workflow.

This contract is intentionally independent of release behavior tests so a future workflow edit cannot silently collapse the privilege boundary.

## Non-goals

- No change to Maven coordinates, public API, Central Publisher API flow, signing format, or version scheme.
- No branch-protection/ruleset change; that is tracked separately by #189.
- No new release service or custom Gradle plugin.

## Verification

The RED state is the new security contract running against the current single privileged workflow. GREEN requires the split workflows plus the unchanged release dry-run/staging/API/Maven-consumer checks to pass.

Closes #188 when merged.