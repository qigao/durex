# Releasing Durex 0.1

Durex releases are built from one exact Git commit. The public artifact set remains fixed to:

- `io.github.qigao.durex:shared-common`
- `io.github.qigao.durex:shared-spring-http`
- `io.github.qigao.durex:messaging-api`
- `io.github.qigao.durex:messaging-spring-redis`

## Dry-run

The `Durex Release` workflow runs as a credentials-free dry-run on pull requests that modify the release contract. It can also be started manually with a version such as `0.1.0` and `publish_to_central=false`.

A dry-run:

1. derives and validates a non-SNAPSHOT `X.Y.Z` version;
2. publishes the four artifacts to an isolated temporary Maven repository;
3. verifies the exact artifact set;
4. requires the generated release signatures to exactly match the committed 0.1 API baseline (not merely be backward-compatible with it);
5. compiles the external Maven-only consumer against the release-version artifacts;
6. assembles an unsigned Central-layout bundle with MD5/SHA-1 checksums;
7. records the tested SHA and coordinates without making any network publication.

The dry-run uses a visibly invalid CI-only license placeholder because Durex currently has no selected distribution license. That placeholder is never allowed in a real publication.

## Real release prerequisites

A real Central release is intentionally impossible until all prerequisites exist:

- issue #177 is resolved with a canonical repository `LICENSE` and matching POM license metadata;
- Central Publisher Portal namespace `io.github.qigao.durex` is verified for the publishing account;
- repository secrets contain `DUREX_SIGNING_KEY`, `DUREX_SIGNING_PASSWORD` (when the key is protected), optional `DUREX_SIGNING_KEY_ID`, `CENTRAL_TOKEN_USERNAME`, and `CENTRAL_TOKEN_PASSWORD`;
- the release commit passes normal Publication Surface/API compatibility CI.

The OpenPGP key is supplied to Gradle's Signing Plugin in memory. It is not stored in the repository.

## Exact tag contract

A real publication must run from tag `vX.Y.Z`. The workflow derives Maven version `X.Y.Z`, rejects SNAPSHOT/other tag forms, resolves the tag commit, and requires it to equal the workflow's `GITHUB_SHA`.

For example, after the desired commit is reviewed and all gates are green:

```bash
git tag v0.1.0 <exact-commit-sha>
git push origin v0.1.0
```

Pushing the tag invokes the real publication path. Missing license/signing/Central credentials fail before any upload.

## Central Publisher Portal flow

The release repository is converted into a Maven Repository Layout zip containing only the four public components and their release version directories. For each main/source/javadoc/POM file the bundle includes MD5/SHA-1 checksums; a real release additionally requires the OpenPGP `.asc` signature produced by Gradle.

The workflow uploads the single bundle to the official Central Publisher API using `POST /api/v1/publisher/upload` with `publishingType=USER_MANAGED`. It polls deployment status until `VALIDATED`, explicitly promotes that deployment, then waits for `PUBLISHED`. It does not use the retired OSSRH service.

After Central reports `PUBLISHED`, the workflow creates or updates the matching GitHub release with the exact tested SHA, four Maven coordinates, and Central deployment id.

Central components are immutable after publication, so version/tag reuse is not a recovery mechanism. Fixes require a new version.
