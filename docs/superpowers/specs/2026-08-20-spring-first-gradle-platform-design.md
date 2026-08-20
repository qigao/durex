# Spring-first Gradle Platform Design

## Context

Durex already has a useful Gradle dependency-management architecture: domain-specific version catalogs under `gradle/versions`, reusable capability scripts under `gradle/library`, shared build behavior under `gradle/extensions`, and automatic module discovery through `gradle/extensions/modules.gradle`.

The Spring migration must improve this architecture rather than replace it with repetitive per-module build files or a monolithic root build.

## Decision

Durex becomes Spring-first while preserving Gradle as a first-class platform layer.

The build model is:

```text
gradle/versions/*.toml
        -> versions, BOMs, plugin versions

gradle/library/*.gradle
        -> reusable dependency capabilities

gradle/extensions/*.gradle
        -> reusable build/test/repository behavior

module/build.gradle(.kts)
        -> declares only the capabilities the module needs
```

Automatic module discovery remains part of the design.

## Dependency rules

1. Versions belong in version catalogs, grouped by concern instead of one giant catalog.
2. Spring Boot provides the dependency platform for Spring-managed libraries.
3. Capability scripts are granular. A module opts into web, JPA, Redis, observability, testing, and native support independently.
4. New modules must not use the existing catch-all `spring.gradle` dependency set.
5. The legacy Spring Boot 2.7 aliases remain temporarily so the old build is not broken before migration.
6. Quarkus aliases and modules remain temporarily but receive no new platform work; they are removed only after Spring replacements exist.
7. Pure Java libraries remain framework-neutral where that is natural.

## Phase 1

Phase 1 proves that the modern Spring Boot 4.1 / GraalVM reference build consumes Durex's central Gradle management instead of maintaining its own dependency versions.

Changes:

- extend `spring.versions.toml` with Spring Boot 4.1 and GraalVM Native Build Tools versions while retaining legacy aliases;
- add granular `spring-core.gradle`, `spring-web.gradle`, and `spring-test.gradle` capability scripts;
- refactor `reference/spring-native` to import the central Spring catalog and apply those capability scripts;
- keep the reference as an isolated Gradle 9.1 build until legacy modules are migrated;
- verify JVM tests, Spring AOT runtime, `nativeTest`, `nativeCompile`, and native HTTP smoke in GitHub Actions.

## Next phases

After Phase 1 is green, add and exercise `spring-data-jpa.gradle`, `spring-redis.gradle`, and `spring-observability.gradle`. Migrate real modules capability-by-capability. Only after legacy Quarkus/Spring 2 modules are removed should the repository root wrapper move to Gradle 9.1 and the temporary legacy aliases be deleted.
