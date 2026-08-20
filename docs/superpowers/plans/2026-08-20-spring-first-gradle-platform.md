# Spring-first Gradle Platform Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Spring Boot 4.1 / GraalVM reference application consume Durex's existing central Gradle dependency-management architecture.

**Architecture:** Preserve domain version catalogs, reusable `gradle/library` capability scripts, shared extensions, and automatic module discovery. Add modern Spring aliases alongside legacy aliases, then refactor the isolated Spring Native reference build to import the central catalog and compose granular Spring capabilities.

**Tech Stack:** Gradle 9.1, Spring Boot 4.1, Java/GraalVM 25, GraalVM Native Build Tools 1.1.1, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-08-20-spring-first-gradle-platform-design.md`

## Global Constraints

- Preserve the existing `gradle/versions`, `gradle/library`, and `gradle/extensions` model.
- Preserve automatic module discovery.
- Do not upgrade the legacy root Gradle wrapper in Phase 1.
- Do not break the existing Spring Boot 2.7 aliases in Phase 1.
- Do not add new Quarkus platform work.
- Keep Spring capabilities granular rather than recreating the existing catch-all `spring.gradle`.

---

### Task 1: Extend the Spring version catalog

**Files:**
- Modify: `gradle/versions/spring.versions.toml`

**Interfaces:**
- Produces: `spring-bom4`, `springboot4`, and `graalvmNative` catalog aliases.

- [ ] Add Spring Boot 4.1.0 and GraalVM Native Build Tools 1.1.1 versions while preserving the Spring Boot 2.7.1 and dependency-management plugin aliases.
- [ ] Add the Spring Boot 4 BOM library alias.
- [ ] Add Spring Boot 4 and GraalVM Native plugin aliases.

### Task 2: Add granular Spring capability scripts

**Files:**
- Create: `gradle/library/spring-core.gradle`
- Create: `gradle/library/spring-web.gradle`
- Create: `gradle/library/spring-test.gradle`

**Interfaces:**
- Consumes: the `sLibs` version catalog.
- Produces: independently applicable Spring core, MVC, and test capabilities.

- [ ] Add a Spring core capability that imports the central Spring Boot 4 BOM and `spring-boot-starter`.
- [ ] Add a Spring web capability that imports the same BOM and `spring-boot-starter-webmvc`.
- [ ] Add a Spring test capability that imports the BOM, `spring-boot-starter-test`, and JUnit Platform test behavior.

### Task 3: Refactor the Spring Native reference build

**Files:**
- Modify: `reference/spring-native/settings.gradle.kts`
- Modify: `reference/spring-native/build.gradle.kts`
- Modify: `reference/spring-native/README.md`

**Interfaces:**
- Consumes: central `spring.versions.toml` and Spring capability scripts.
- Produces: the same `/hello` application and native build behavior as before with no local Spring/GraalVM version duplication.

- [ ] Import the central `sLibs` catalog from `../../gradle/versions/spring.versions.toml`.
- [ ] Replace hard-coded Spring/GraalVM plugin versions with catalog aliases.
- [ ] Replace the local dependency block with `spring-core.gradle`, `spring-web.gradle`, and `spring-test.gradle` applications.
- [ ] Keep Java source/toolchain compatibility at Java 21+ while CI runs Java/GraalVM 25.
- [ ] Update documentation to state Gradle 9.1 for Java 25 builds and explain central capability composition.

### Task 4: Verify the migration

**Files:**
- No production files.

**Interfaces:**
- Verifies: dependency catalog import, external capability script application, Spring AOT, and GraalVM native behavior.

- [ ] Push the refactor commit to `refactor/spring-native-reference`.
- [ ] Confirm PR #128 triggers `Spring Native Reference` GitHub Actions.
- [ ] Verify JVM tests pass.
- [ ] Verify Spring AOT JVM HTTP smoke passes.
- [ ] Verify `nativeTest` passes.
- [ ] Verify `nativeCompile` passes.
- [ ] Verify native executable HTTP smoke passes.
