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

Legacy `shared.support` helpers are not part of the public contract and are not packaged in the 0.1 external surface.

## Compatibility verification

`gradle/public-api/0.1-signatures.txt` is the normalized JVM signature baseline for entries classified as `api`. `scripts/public-api-signatures.sh` generates signatures from the **staged Maven jars** using JDK `javap -protected -s`; it does not inspect project source.

The check is directional: every baseline line must still exist in the current staged artifact. This catches removed classes/members, incompatible JVM descriptor changes, and visibility/class-declaration changes while allowing additive API growth. Types classified as `runtime` are intentionally excluded from the user API compatibility promise.

An intentional incompatible baseline edit must be reviewed as an explicit public contract change; updating the baseline is not a generic way to make CI green.

## Publication verification

PR CI publishes the four artifacts into a clean temporary Maven repository. `reference/publication-consumer` is then built as an ordinary external Gradle project that knows only Maven coordinates; it does not load SimpleDSL, Durex project dependencies, composite builds, or source injection.

`PublicApiBaseline.java` remains a compile-time usability fixture. The signature baseline adds deterministic JVM-level compatibility protection on top of that consumer compile proof.

The staging repository is a CI proof mechanism. Network release credentials and deployment to a public Maven repository are a separate release operation.
