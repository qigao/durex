# Releasing Durex 0.1

Durex releases are built from one exact Git commit. The public artifact set remains fixed to:

- `io.github.qigao.durex:shared-common`
- `io.github.qigao.durex:shared-spring-http`
- `io.github.qigao.durex:messaging-api`
- `io.github.qigao.durex:messaging-spring-redis`

Durex is distributed under the Apache License, Version 2.0. The canonical license text is the repository-root `LICENSE` file, and Maven POM license metadata is fixed by `gradle/durex-publication.gradle`; release jobs do not inject or override the distribution license.

## Release verification

`Durex Release Verification` is the credentials-free release dry-run. It runs on pull requests that modify the release contract and can also be started manually with a stable version such as `0.1.0`.

The verification workflow has `contents: read` only and does not reference signing or Central credentials. It:

1. derives and validates a non-SNAPSHOT `X.Y.Z` version;
2. requires the canonical Apache-2.0 repository `LICENSE`;
3. publishes the four artifacts unsigned to an isolated temporary Maven repository;
4. verifies the exact artifact set and Apache-2.0 POM metadata;
5. requires the generated release signatures to exactly match the committed 0.1 API baseline (not merely be backward-compatible with it);
6. compiles the external Maven-only consumer against the release-version artifacts;
7. assembles an unsigned Central-layout bundle with MD5/SHA-1 checksums;
8. records the tested SHA, coordinates, and license without making any network publication.

`scripts/verify-release-security-boundary.sh` is an executable CI contract that keeps this workflow read-only/secret-free and keeps the privileged publication workflow tag-only.

## Real release prerequisites

Before a real Central release:

- Central Publisher Portal namespace `io.github.qigao.durex` must be verified for the publishing account;
- the privileged tag-only `Durex Release` job must receive `DUREX_SIGNING_KEY`, `DUREX_SIGNING_PASSWORD` (when the key is protected), optional `DUREX_SIGNING_KEY_ID`, `CENTRAL_TOKEN_USERNAME`, and `CENTRAL_TOKEN_PASSWORD`;
- the release commit must pass normal Publication Surface/API compatibility CI and `Durex Release Verification`.

Release credentials are referenced only by the tag-only publication workflow. Pull-request/manual verification has no credential references. The OpenPGP key is supplied to Gradle's Signing Plugin in memory and is not stored in the repository.

The third-party actions used by both release workflows are pinned to immutable commit SHAs rather than mutable major-version tags.

## Exact tag contract

A real publication runs only from tag `vX.Y.Z`. `Durex Release` has no pull-request or manual-dispatch trigger. The workflow derives Maven version `X.Y.Z`, rejects SNAPSHOT/other tag forms, resolves the tag commit, and requires it to equal the workflow's `GITHUB_SHA`.

For example, after the desired commit is reviewed and all gates are green:

```bash
git tag v0.1.0 <exact-commit-sha>
git push origin v0.1.0
```

Pushing the tag invokes the real publication path. Missing signing/Central credentials fail before staging/upload; the repository license cannot be replaced by workflow input or environment variables.

## Central Publisher Portal flow

The release repository is converted into a Maven Repository Layout zip containing only the four public components and their release version directories. For each main/source/javadoc/POM file the bundle includes MD5/SHA-1 checksums; the real release additionally requires the OpenPGP `.asc` signature produced by Gradle.

The workflow uploads the single signed bundle to the official Central Publisher API using `POST /api/v1/publisher/upload` with `publishingType=USER_MANAGED`. It polls deployment status until `VALIDATED`, explicitly promotes that deployment, then waits for `PUBLISHED`. It does not use the retired OSSRH service.

After Central reports `PUBLISHED`, the workflow creates or updates the matching GitHub release with the exact tested SHA, four Maven coordinates, Apache-2.0 license, and Central deployment id.

Central components are immutable after publication, so version/tag reuse is not a recovery mechanism. Fixes require a new version.
