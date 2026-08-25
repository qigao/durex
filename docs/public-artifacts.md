# Durex public artifacts

Durex separates its external runtime/API surface from repository-local applications, schemas, and reference builds.

## Initial Maven surface

All public artifacts use group `io.github.qigao.durex` and one synchronized version. The first development line is `0.1.0-SNAPSHOT`; pre-1.0 releases follow semantic versioning.

| Artifact | Contract |
| --- | --- |
| `shared-common` | shared response/error model used by Durex runtime adapters |
| `shared-spring-http` | Spring HTTP error mapping; exposes `shared-common` transitively |
| `messaging-api` | framework-neutral messaging annotations |
| `messaging-spring-redis` | Spring Redis Pub/Sub/Stream adapter, codec, and listener failure policy; exposes `messaging-api` transitively |

Music, schema/codegen, migration, and reference modules are not public artifacts. `shared-utils` remains internal until its SQL-builder responsibility has an explicit external contract.

A BOM is intentionally not published yet. Four artifacts share one version, so a BOM would add an artifact without solving a current alignment problem. Revisit that decision if the public surface expands or versions diverge.

## Publication verification

PR CI publishes the four artifacts into a clean temporary Maven repository. `reference/publication-consumer` is then built as an ordinary external Gradle project that knows only Maven coordinates; it does not load SimpleDSL, Durex project dependencies, composite builds, or source injection.

`PublicApiBaseline.java` is the checked compatibility fixture for the supported entry points. Removing or incompatibly changing one of those promised APIs makes the external consumer fail to compile. This fixture should evolve deliberately with release notes when the public contract changes.

The staging repository is a CI proof mechanism. Network release credentials and deployment to a public Maven repository are a separate release operation.
