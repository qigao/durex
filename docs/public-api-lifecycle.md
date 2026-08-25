# Durex 0.1 public API lifecycle

Durex 0.1 is a pre-1.0 line, but it is still intended to be usable as a stable external runtime platform. Pre-1.0 does **not** mean that every patch release may arbitrarily break consumers.

## What is a supported API

The public Maven artifact set is fixed during the 0.1 stabilization phase:

- `io.github.qigao.durex:shared-common`
- `io.github.qigao.durex:shared-spring-http`
- `io.github.qigao.durex:messaging-api`
- `io.github.qigao.durex:messaging-spring-redis`

`gradle/public-api/0.1-surface.txt` is the authoritative type inventory. Its scope is significant:

- `api` means the type is a supported user-facing contract.
- `runtime` means the type must be public for Spring/framework wiring or the built-in implementation, but public Java visibility alone is **not** an API promise.

For `api` entries, `gradle/public-api/0.1-signatures.txt` records the JVM signatures protected by CI. The signatures are generated from staged Maven jars, not project source.

A type or member is not promoted to supported API merely because it is declared `public`. New contract surface must be deliberate.

## Compatibility rules for 0.1.x

Within the `0.1.x` line:

- existing protected API signatures must not be removed, renamed, narrowed in visibility, or changed incompatibly;
- changing a method/constructor/field JVM descriptor is a breaking change;
- changing an API type from class/interface/record/enum in a way that invalidates the baseline is a breaking change;
- additive API is allowed, but it must be deliberate and included in the release review before it becomes part of the next release baseline;
- `runtime` implementation types may evolve without a JVM compatibility promise, provided the supported user API and documented runtime behavior remain valid;
- fixes may change incorrect behavior, but a user-visible semantic change requires an issue and release-note rationale.

A normal `0.1.x` patch must therefore pass both the Maven-only consumer fixture and the JVM signature compatibility gate without deleting baseline signatures.

## Adding API

When adding a new supported type:

1. add the type to `gradle/public-api/0.1-surface.txt` with `scope=api`;
2. add a Maven-only usage to `reference/publication-consumer/PublicApiBaseline.java` when practical;
3. regenerate/review the signature baseline before the release that promises the new API;
4. document the addition in release notes.

When adding a member to an existing `api` type, the compatibility checker intentionally permits the additive change during development. Before release, the generated current signature set and committed release baseline must be reviewed so the new member is either deliberately protected or explicitly kept outside the promised contract by redesigning the API surface.

Do not update a baseline merely to silence CI.

## Deprecation and removal

Supported API should be deprecated before removal whenever a migration path is possible.

1. Open an issue that explains the problem and replacement.
2. Add the replacement first when feasible.
3. Mark the old Java API with `@Deprecated`, including `since`/`forRemoval` where appropriate.
4. Keep the deprecated signature for the remainder of the `0.1.x` line.
5. Document the migration in release notes.
6. Remove it only in a later minor line such as `0.2.0`, with an explicit compatibility-baseline change.

Emergency removals for security or correctness require a dedicated issue and an explicit release-note warning; they are exceptions, not a normal patch-release mechanism.

## Breaking changes and 0.2+

A breaking public API change targets a new minor line (`0.2.0` while pre-1.0), not a `0.1.x` patch. The PR must include:

- the motivating issue;
- migration guidance;
- the intentional manifest/signature-baseline diff;
- release-note rationale;
- updated Maven-only consumer coverage.

The same discipline should continue after 1.0, with semantic-versioning rules becoming stricter rather than looser.

## Build-time boundary

SimpleDSL is a Durex repository/build-time composition tool. External runtime consumers of the four Maven artifacts do not need SimpleDSL, Durex project dependencies, composite builds, or source injection.

See [Public artifacts](public-artifacts.md) for Maven coordinates and consumer examples.
