# Music Spring Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate the existing `core/music` imperative application to the Spring-first platform without copying the business module or forcing an immediate root-build cutover.

**Architecture:** Keep the existing module directories and legacy `build.gradle` files during migration. Add `build.spring.gradle` descriptors selected by a dedicated Spring migration settings file, so the same source/schema structure can be validated with Spring Boot 4.1, Gradle 9.1, and GraalVM 25 before the new build becomes the repository default. Preserve jOOQ as an explicit persistence capability while adding Spring Data JPA as the default ORM capability for new repositories.

**Tech Stack:** Java 21 source level, Java/GraalVM 25 runtime, Gradle 9.1, Spring Boot 4.1, Spring MVC, Spring Data JPA, Spring JDBC, Spring Boot jOOQ, Redis, Actuator, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-08-20-spring-first-gradle-platform-design.md`

## Global Constraints

- Keep `gradle/versions/*.toml` as version ownership.
- Keep `gradle/library/*.gradle` as composable dependency capabilities.
- Keep automatic module discovery for the eventual root build.
- Do not copy `core/music` into a parallel Spring business tree.
- Do not migrate `core/music-reactive` in this phase.
- Keep jOOQ/codegen available; Spring Data JPA is additive, not a forced replacement.
- Do not merge the draft PR until JVM, AOT, nativeTest, nativeCompile, and HTTP smoke verification are green.

---

### Task 1: Persistence and runtime capabilities

**Files:**
- Create: `gradle/library/spring-data-jpa.gradle`
- Create: `gradle/library/spring-jdbc.gradle`
- Create: `gradle/library/spring-jooq.gradle`
- Create: `gradle/library/spring-redis.gradle`
- Create: `gradle/library/spring-observability.gradle`

**Interfaces:**
- Consumes: `sLibs` catalog and `spring-bom4`.
- Produces: small capability scripts that can be composed from module build descriptors.

- [ ] Add each capability with the Spring Boot BOM plus exactly one starter.
- [ ] Verify the existing Spring Native reference still resolves the central catalog and scripts.
- [ ] Commit as `build: add Spring platform capabilities`.

### Task 2: Alternate build descriptors for migration

**Files:**
- Create: `migration/spring-music/settings.gradle`
- Create: `core/music/build.spring.gradle`
- Create: `core/schema/music/json/build.spring.gradle`
- Create: `core/schema/music/entity/build.spring.gradle`
- Create: `core/schema/music/repo/build.spring.gradle`

**Interfaces:**
- Consumes: existing source directories and central Gradle catalogs/capabilities.
- Produces: a Gradle 9.1 multi-project build that points to the existing module directories while leaving legacy build files untouched.

- [ ] Define `sLibs`, `dbLibs`, `uLibs`, `gLibs`, and `tLibs` catalogs from the root `gradle/versions` directory.
- [ ] Map project descriptors to existing directories and set `buildFileName = 'build.spring.gradle'`.
- [ ] Make `music` consume `spring-core`, `spring-web`, `spring-test`, and `spring-observability`.
- [ ] Make `repo` consume `spring-jooq` and existing database/schema dependencies.
- [ ] Run `gradle projects` and `gradle :music:compileJava`; expected initial failure is framework-specific `javax.*`/JAX-RS wiring, not project resolution.

### Task 3: Spring MVC/service wiring without business duplication

**Files:**
- Modify: `core/music/src/main/java/com/github/durex/music/controller/MusicController.java`
- Modify: `core/music/src/main/java/com/github/durex/music/controller/PlaylistController.java`
- Modify: `core/music/src/main/java/com/github/durex/music/service/MusicService.java`
- Modify: `core/music/src/main/java/com/github/durex/music/service/PlaylistService.java`
- Create: `core/music/src/main/java/com/github/durex/music/MusicApplication.java`
- Create: `core/music/src/springTest/java/com/github/durex/music/controller/MusicControllerSpringTest.java`

**Interfaces:**
- Consumes: existing `Music`, `PlayList`, repository APIs, and response model.
- Produces: Spring-managed controllers/services with unchanged business method signatures and HTTP paths.

- [ ] Write a Spring MVC test asserting `GET /v1/music/{id}` delegates to `MusicService` and returns the existing response envelope.
- [ ] Run the Spring test source set and confirm it fails before Spring MVC annotations/application wiring exist.
- [ ] Replace field injection with constructor injection so framework annotations are not required for dependency assignment.
- [ ] Add Spring `@RestController`/mapping annotations and Spring service stereotypes; preserve endpoint paths and payloads.
- [ ] Run Spring MVC tests and existing service unit tests.

### Task 4: Spring-managed jOOQ repositories

**Files:**
- Modify: `core/schema/music/repo/src/main/java/com/github/durex/music/repository/MusicRepository.java`
- Modify: `core/schema/music/repo/src/main/java/com/github/durex/music/repository/PlayListRepository.java`
- Modify: `core/schema/music/repo/src/main/java/com/github/durex/music/repository/PlayListMusicRepository.java`

**Interfaces:**
- Consumes: generated jOOQ tables/mappers and Spring Boot-provided `DSLContext`.
- Produces: repository beans with the existing repository method API unchanged.

- [ ] Add repository-level tests around title/id queries using the migration build database fixture.
- [ ] Convert repository injection to constructor injection and Spring `@Repository`.
- [ ] Keep query code and jOOQ generated types unchanged.
- [ ] Verify transaction boundaries through Spring `@Transactional` in service tests.

### Task 5: Native verification and cutover evidence

**Files:**
- Create/Modify: `.github/workflows/spring-music.yml`
- Modify: `core/music/readme.adoc`

**Interfaces:**
- Consumes: Spring migration build from Tasks 2-4.
- Produces: repeatable JVM/AOT/native verification for the real music module.

- [ ] Run JVM unit/integration tests.
- [ ] Run `processAot` and an AOT-enabled HTTP smoke test.
- [ ] Run `nativeTest`.
- [ ] Run `nativeCompile` and a native `/v1/music` smoke test.
- [ ] Record the exact successful toolchain and migration commands in the module README.
- [ ] Only after all checks are green, propose promoting the Spring build descriptors and Gradle 9.1 wrapper to the root build.
