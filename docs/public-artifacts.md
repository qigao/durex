# Durex public artifacts

Durex separates its external runtime/API surface from repository-local applications, schemas, and reference builds.

## Initial Maven surface

All public artifacts use group `io.github.qigao.durex` and one synchronized version. The first development line is `0.1.0-SNAPSHOT`; pre-1.0 releases follow the compatibility policy in [Public API lifecycle](public-api-lifecycle.md).

| Artifact | Contract |
| --- | --- |
| `shared-common` | shared response/error model used by Durex runtime adapters |
| `shared-spring-http` | Spring HTTP error mapping; exposes `shared-common` transitively |
| `messaging-api` | framework-neutral messaging annotations |
| `messaging-spring-redis` | Spring Redis Pub/Sub/Stream adapter, codec, and listener failure policy; exposes `messaging-api` transitively |

Music, schema/codegen, migration, and reference modules are not public artifacts. `shared-utils` remains internal until its SQL-builder responsibility has an explicit external contract.

A BOM is intentionally not published yet. Four artifacts share one version, so a BOM would add an artifact without solving a current alignment problem. Revisit that decision if the public surface expands or versions diverge.

## Maven-only consumption

External consumers use ordinary Maven coordinates. They do **not** apply SimpleDSL and do not depend on the Durex source tree.

The examples below use the intended `0.1.0` release coordinate. Repository PR CI currently proves the same graph from a staged `0.1.0-SNAPSHOT` Maven repository before public deployment.

### HTTP error model

Gradle Groovy DSL:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "io.github.qigao.durex:shared-spring-http:0.1.0"
}
```

`shared-common` is exposed transitively, so application code can use the canonical error/response types without declaring it separately:

```java
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.shared.exceptions.model.ErrorCode;
import com.github.durex.shared.model.RespData;

RespData<String> ok = RespData.of("ready", null);
throw new ApiException("missing", ErrorCode.ENTITY_NOT_FOUND);
```

The Spring HTTP adapter supplies Durex's `ApiException` mapping when its auto-configuration is active.

### Redis messaging

Gradle Groovy DSL:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "io.github.qigao.durex:messaging-spring-redis:0.1.0"
}
```

`messaging-api` is exposed transitively. A consumer can use Durex annotations and the Redis adapter without a project/composite dependency:

```java
import com.github.durex.messaging.annotation.Outgoing;
import com.github.durex.messaging.annotation.RedisStreamListener;
import com.github.durex.messaging.annotation.RedisStreamOutgoing;

@Outgoing("events.normalized")
public Event normalize(Event event) {
    return event;
}

@RedisStreamOutgoing("events.stream")
public Event publish(Event event) {
    return event;
}

@RedisStreamListener(
    stream = "events.stream",
    group = "workers",
    consumer = "worker-1")
public void consume(Event event) {
    // application handling
}
```

Applications may provide their own `RedisMessageCodec` or `RedisStreamListenerFailureHandler` when the default codec/failure disposition is not sufficient.

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

An intentional incompatible baseline edit must be reviewed as an explicit public contract change; updating the baseline is not a generic way to make CI green. The release/deprecation process is defined in [Public API lifecycle](public-api-lifecycle.md).

## Central Publisher Portal readiness

The four public modules are built as Maven Central bundle inputs: each publication contains the main jar, sources jar, javadoc jar, Maven POM, and Gradle module metadata. Gradle's Signing Plugin is wired to the `mavenJava` publication and accepts an ASCII-armored OpenPGP key through `signingKey` / `signingPassword` Gradle properties or `DUREX_SIGNING_KEY` / `DUREX_SIGNING_PASSWORD` environment variables. An optional `signingKeyId` / `DUREX_SIGNING_KEY_ID` supports OpenPGP subkeys.

PR CI does not need a signing secret. When a signing key is present, publishing the Maven publication creates and publishes the `.asc` signatures for the publication artifacts/metadata.

License selection is deliberately separate from build mechanics. The repository currently has no canonical distribution license, so issue #177 blocks an actual Central release. Snapshot staging may inject a clearly marked CI-only license value to verify POM serialization; a non-SNAPSHOT publication cannot generate its POM unless `durexLicenseName` and `durexLicenseUrl` (or `DUREX_LICENSE_NAME` / `DUREX_LICENSE_URL`) are present.

Durex does not configure the retired OSSRH service. The release workflow will target the Central Publisher Portal bundle/API path after the exact release SHA has passed staging and compatibility checks.

## Publication verification

PR CI publishes the four artifacts into a clean temporary Maven repository. `reference/publication-consumer` is then built as an ordinary external Gradle project that knows only Maven coordinates; it does not load SimpleDSL, Durex project dependencies, composite builds, or source injection.

`PublicApiBaseline.java` remains a compile-time usability fixture. The signature baseline adds deterministic JVM-level compatibility protection on top of that consumer compile proof.

The staging repository is a CI proof mechanism. Network release credentials and deployment to a public Maven repository are a separate release operation.
