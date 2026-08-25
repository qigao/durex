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

## Supported type surface

`gradle/public-api/0.1-surface.txt` is the authoritative type inventory for the 0.1 line. Each entry is classified as either:

- `api`: user-facing contract that compatibility CI must preserve, or
- `runtime`: a public bytecode type required by Spring/framework wiring or the built-in implementation, but not a direct user API compatibility promise.

The distinction is deliberate: Java/Spring mechanics sometimes require implementation classes to remain `public`, but public visibility alone must not silently expand the supported Durex API.

The initial supported API includes the complete HTTP error model (`ApiException`, `ErrorCode`, `ErrorResponse`, `RespData`, `DurexHttpExceptionHandler`), the three messaging annotations, and the Redis codec/failure-policy contracts including `RedisStreamListenerFailure`.

Legacy `shared.support` helpers are not part of the public contract and must not be packaged in the first 0.1 external surface unless a real consumer contract is established.

## Publication verification

PR CI publishes the four artifacts into a clean temporary Maven repository. `reference/publication-consumer` is then built as an ordinary external Gradle project that knows only Maven coordinates; it does not load SimpleDSL, Durex project dependencies, composite builds, or source injection.

`PublicApiBaseline.java` is the compile-time fixture for the supported `api` entries. The publication workflow also verifies that every manifest entry exists in the staged jars and rejects known dead support types. #169 will add deterministic JVM signature compatibility on top of this explicit manifest.

The staging repository is a CI proof mechanism. Network release credentials and deployment to a public Maven repository are a separate release operation.
