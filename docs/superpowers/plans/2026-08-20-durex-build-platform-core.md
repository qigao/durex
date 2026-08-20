# Durex Build Platform Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` task-by-task. Every task follows RED -> GREEN and ends in a focused commit.

**Goal:** Implement the Durex-owned Gradle dependency/module platform, prove it with isolated fixtures, and migrate the real Spring `music` build away from Gradle Version Catalogs and repeated `apply from` scripts without cutting over the legacy root build.

**Architecture:** `build-bootstrap` owns Durex TOML parsing, settings bootstrap, build-local dependency registry, plugin resolution, and project discovery. The isolated `build-logic` build loads the same TOML source into its own registry and provides module-type/feature plugins. Modules declare a type, optional features, explicit business `project(...)` dependencies, and use `durex.library("alias")` only for uncommon external libraries.

**Baseline:** Gradle 9.1, Java/GraalVM 25, `org.tomlj:tomlj:1.1.1`, Spring Boot 4.1.0, GraalVM Build Tools 1.1.1, jOOQ 3.21.5.

**Spec:** `docs/superpowers/specs/2026-08-20-durex-build-platform-design.md`

## Global constraints

- `gradle/dependencies/*.toml` is the single version/plugin source for new Durex build-platform code.
- New/migrated paths must not use `VersionCatalogsExtension`, `sLibs`, `dbLibs`, `gLibs`, `uLibs`, or `tLibs`.
- Gradle BOM/platform and Gradle dependency resolution remain authoritative; Durex does not implement a resolver.
- `build-bootstrap` may hard-code only `org.tomlj:tomlj:1.1.1` as bootstrap implementation detail.
- Included builds are isolated: consumer/root and `build-logic` each create one build-local registry from the same TOML source.
- Settings extensions use Gradle provider properties (`DirectoryProperty`, `RegularFileProperty`, `Property<Boolean>`) so both Groovy and Kotlin settings DSLs are first-class.
- Manifest loading/discovery is deferred until `settingsEvaluated` so `repositoryRoot` and manifest overrides are configured first.
- Known external project-plugin versions are resolved by `durex.settings`; a manifest plugin with `module` uses `useModule`, otherwise `useVersion`.
- Module discovery supports `auto`, `manual`, `strict-auto`; precedence is `exclude > manual include/override > auto > fallback naming`.
- Manual module entries support `build-file = "build.spring.gradle"`.
- Module paths resolve against `repositoryRoot`, not the `modules.toml` directory.
- `durex.library(alias)` is the generic escape hatch for manifest-managed non-feature libraries.
- `durex.jooq-schema` stays separate from runtime `durex.feature.jooq`.
- New Durex module types use Java 25; do not rewrite Java-21-compatible business sources just to use Java 25 syntax.
- Do not modify root `settings.gradle`, root wrapper, or legacy Quarkus module descriptors in this plan.

---

## Task 1 — Dependency manifest, registry service, and minimal `durex.settings`

**Create:**

```text
build-bootstrap/settings.gradle.kts
build-bootstrap/build.gradle.kts
build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/DependencyManifestLoader.groovy
build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/DependencyRegistry.groovy
build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/DependencyRegistryService.groovy
build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/VersionSpec.groovy
build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/PlatformSpec.groovy
build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/LibrarySpec.groovy
build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/PluginSpec.groovy
build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexSettingsExtension.groovy
build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexSettingsPlugin.groovy
gradle/dependencies/durex.toml
gradle/dependencies/spring.toml
gradle/dependencies/database.toml
gradle/dependencies/test.toml
gradle/dependencies/utils.toml
build-bootstrap/tests/manifest-valid/settings.gradle
build-bootstrap/tests/manifest-valid/build.gradle
build-bootstrap/tests/manifest-validation/settings.gradle
build-bootstrap/tests/manifest-validation/*.toml
build-bootstrap/tests/plugin-resolution/settings.gradle
build-bootstrap/tests/plugin-resolution/build.gradle
```

**Public contract:**

```text
DependencyManifestLoader.load(File) -> immutable DependencyRegistry
DependencyRegistry.javaVersion()
DependencyRegistry.version(id)
DependencyRegistry.platform(id)
DependencyRegistry.library(id)
DependencyRegistry.plugin(alias)
DependencyRegistry.pluginByGradleId(id)
```

`DurexSettingsExtension` is a Gradle managed type:

```text
DirectoryProperty repositoryRoot
RegularFileProperty dependencyManifest
RegularFileProperty modulesManifest
Property<Boolean> moduleDiscovery
```

`repositoryRoot` defaults to `settings.rootDir`; `dependencyManifest` convention is `repositoryRoot/gradle/dependencies/durex.toml`; `modulesManifest` convention is `repositoryRoot/gradle/modules.toml`; `moduleDiscovery` defaults true. Conventions must track a later `repositoryRoot.set(...)` rather than eagerly snapshotting the initial root.

- [ ] **RED:** fixture applies an unknown `durex.settings`:

```groovy
pluginManagement {
    includeBuild('../..')
    repositories { gradlePluginPortal(); mavenCentral() }
}
plugins { id 'durex.settings' }

durexSettings {
    repositoryRoot.set(file('../../..'))
    moduleDiscovery.set(false)
}
```

Run:

```bash
gradle -p build-bootstrap/tests/manifest-valid verifyDurexManifest --stacktrace
```

Expected: unknown `durex.settings`.

- [ ] **GREEN bootstrap build:** apply `groovy-gradle-plugin`, repositories `gradlePluginPortal()` + `mavenCentral()`, and only:

```kotlin
dependencies {
    implementation("org.tomlj:tomlj:1.1.1")
}
```

Register only `durex.settings` in this task; do not register Task-3 plugin classes before they exist.

- [ ] **Create Durex manifests** with exact migration baselines:

```text
durex.toml: include spring/database/test/utils; java.version=25
spring.toml: spring-boot=4.1.0, graal-native=1.1.1,
             Spring Boot BOM, core/web/validation/actuator/test/web-test,
             JPA/JDBC/jOOQ/Redis, Jackson annotations/databind,
             Jakarta validation, H2, Spring Boot + GraalVM plugins
database.toml: jooq=3.21.5, core/meta/codegen/meta-extensions,
               org.jooq.jooq-codegen-gradle plugin module
utils.toml: lombok=1.18.46, jsonschema2pojo=1.3.3,
            jsonschema2pojo-core=1.1.2, swagger-annotations=2.2.0,
            CDI=2.0.SP1, javax.inject=1, transaction=1.3,
            javax.validation=2.0.1.Final, interceptor=1.2.2,
            org.jsonschema2pojo plugin marker
test.toml: junit=5.8.2, junit-jupiter, launcher=1.8.2
```

Spring-managed libraries use `platform = "spring"` and carry no explicit version. A normal library has exactly one owner: `version`, `version.ref`, or `platform`.

- [ ] **Implement strict parser:** reject include cycles, missing includes, duplicate aliases, duplicate Gradle plugin ids, unknown refs/platforms, unsupported sections/keys, malformed `group:name`, missing version ownership, and `platform + explicit version`. Error prefix is always `Durex dependency manifest error` and names source file/object/problem.

- [ ] **Implement `DependencyRegistryService`:** Gradle `BuildService` with `RegularFileProperty manifestFile`; lazily parse once and delegate registry methods. Service registration name is exactly `durexDependencyRegistry`.

- [ ] **Implement minimal `DurexSettingsPlugin`:** create extension immediately; in `settings.gradle.settingsEvaluated` register the service and configure `pluginManagement.resolutionStrategy.eachPlugin`. If a managed plugin request supplies a conflicting explicit version, fail with a Durex version-conflict message. If `moduleDiscovery=false`, stop after registry/plugin bootstrap.

- [ ] **Validation matrix:** one reusable fixture selects `-Pmanifest=<file>` and exercises `cycle`, `duplicate`, `unknown-ref`, `version-conflict`, `unsupported-key`. Each command must fail for the intended Durex message, never an NPE/opaque parser error.

- [ ] **Plugin-resolution proof:** fixture build contains `plugins { id 'org.jsonschema2pojo' }` with no version. `durex.settings` resolves it from TOML. Run `verifyPluginResolution` and require PASS.

- [ ] **Add `durexDependencies` diagnostic:** root task prints canonical manifest path, Java version, Spring platform coordinate, and managed Spring Boot/GraalVM/jOOQ plugin versions in stable sorted output.

- [ ] **Verify and commit:** 

```bash
gradle -p build-bootstrap/tests/manifest-valid verifyDurexManifest durexDependencies --stacktrace
gradle -p build-bootstrap/tests/plugin-resolution verifyPluginResolution --stacktrace
```

Commit: `build: add Durex dependency manifest bootstrap`.

---

## Task 2 — Auto/manual module discovery and `ProjectRegistry`

**Create/modify:**

```text
build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexSettingsPlugin.groovy
build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/ProjectDiscovery.groovy
build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/ProjectRegistry.groovy
build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/ProjectSpec.groovy
build-bootstrap/tests/modules-auto/**
build-bootstrap/tests/modules-manual/**
build-bootstrap/tests/modules-strict/**
build-bootstrap/tests/modules-conflict/**
```

`ProjectSpec` contains Gradle path, canonical directory, source `AUTO|MANUAL`, and build file. `ProjectRegistry` has unique lookup by Gradle path and canonical directory.

- [ ] **RED auto fixture:** `core/music`, `core/shared/utils`, `core/shared/jakarta/common`, `core/schema/music/repo`, and excluded `core/ignored` all have `build.gradle`. `modules.toml`:

```toml
[discovery]
mode = "auto"
roots = ["core"]
exclude = ["core/ignored"]

[[module]]
name = "shared-common"
path = "core/shared/jakarta/common"
```

Expected final names: `:music`, `:shared-utils`, `:shared-common`, `:music-repo`; no `:ignored`.

- [ ] **RED manual fixture:**

```toml
[discovery]
mode = "manual"

[[module]]
name = "music"
path = "core/music"
build-file = "build.spring.gradle"

[[module]]
name = "admin-tool"
path = "tools/admin"
```

Run both `projects`; before implementation expected project set is wrong/missing.

- [ ] **Implement naming:**

```text
core/<name>                 -> :<name>
core/shared/<name>          -> :shared-<name>
core/schema/<domain>/<kind> -> :<domain>-<kind>
auto fallback               -> path-under-root segments joined with '-'
```

`strict-auto` rejects any discovered path that would require fallback naming. `manual` never scans. Manual override of an auto-discovered physical directory replaces the inferred name instead of duplicating the project.

- [ ] **Register projects:** `settings.include(path)`, assign `projectDir`, and set `buildFileName` when `build-file` is present. Reject duplicate logical paths and one physical directory mapped to two logical paths.

- [ ] **Add `durexProjects`:** sorted stable lines `gradlePath | relativePath | source | buildFile`.

- [ ] **Verify:**

```bash
gradle -p build-bootstrap/tests/modules-auto projects durexProjects --stacktrace
gradle -p build-bootstrap/tests/modules-manual projects durexProjects --stacktrace
gradle -p build-bootstrap/tests/modules-strict projects --stacktrace
gradle -p build-bootstrap/tests/modules-conflict projects --stacktrace
```

First two PASS. Strict fixture fails on an unmappable deep path. Conflict fixture fails on duplicate logical name.

Commit: `build: add Durex module discovery`.

---

## Task 3 — Bootstrap isolated `build-logic` from the same manifest

**Create/modify:**

```text
build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexBuildLogicSettingsExtension.groovy
build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexBuildLogicSettingsPlugin.groovy
build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexBuildLogicPlugin.groovy
build-bootstrap/build.gradle.kts
build-logic/settings.gradle.kts
build-logic/build.gradle.kts
```

`DurexBuildLogicSettingsExtension` exposes `DirectoryProperty repositoryRoot` and `RegularFileProperty dependencyManifest`.

- [ ] **RED target settings:**

```kotlin
pluginManagement {
    includeBuild("../build-bootstrap")
    repositories { gradlePluginPortal(); mavenCentral() }
}
plugins { id("durex.build-logic-settings") }

durexBuildLogicSettings {
    repositoryRoot.set(file(".."))
}
rootProject.name = "durex-build-logic"
```

`build-logic/build.gradle.kts` target:

```kotlin
plugins {
    `groovy-gradle-plugin`
    id("durex.build-logic")
}
repositories { gradlePluginPortal(); mavenCentral() }
```

Run `gradle -p build-logic tasks --stacktrace`; expected unknown bootstrap plugins.

- [ ] **Implement and only now register** `durex.build-logic-settings` and `durex.build-logic` in `build-bootstrap/build.gradle.kts`.

- [ ] `durex.build-logic-settings` uses the same deferred settings lifecycle, registers a build-local `durexDependencyRegistry`, and performs no module discovery.

- [ ] `durex.build-logic` reads that local service and adds manifest-backed `implementation` dependencies for Spring Boot plugin, GraalVM Native plugin, jOOQ codegen plugin, jOOQ core, and jOOQ meta. No framework version is hard-coded in `build-logic/build.gradle.kts`.

- [ ] Remove `dbLibs` and the `versionCatalogs` block from `build-logic`.

- [ ] Verify existing schema plugin still compiles/generates:

```bash
gradle -p build-logic tasks --stacktrace
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/QMusic.java
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/records/RMusic.java
```

Commit: `build: bootstrap Durex build logic from manifest`.

---

## Task 4 — Module types and generic dependency access

**Create/modify:**

```text
build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy
build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy
build-logic/src/main/groovy/com/github/durex/gradle/PersistenceExtension.groovy
build-logic/src/main/groovy/com/github/durex/gradle/DurexModuleState.groovy
build-logic/src/main/groovy/com/github/durex/gradle/ModuleKind.groovy
build-logic/src/main/groovy/com/github/durex/gradle/DurexDependencyAccess.groovy
build-logic/src/main/groovy/durex.java-base.gradle
build-logic/src/main/groovy/durex.spring-base.gradle
build-logic/src/main/groovy/durex.java-library.gradle
build-logic/src/main/groovy/durex.spring-library.gradle
build-logic/src/main/groovy/durex.spring-service.gradle
build-logic/build.gradle.kts
build-logic/tests/java-library-smoke/**
build-logic/tests/spring-service-smoke/**
build-logic/tests/module-conflict/**
```

- [ ] **RED:** fixtures apply `durex.java-library`, `durex.spring-service`, and conflicting `durex.spring-service + durex.java-library`; run `test`, `verifyDurexService`, and `help`. Expected missing plugins.

- [ ] Register binary plugin `durex.module` (`DurexModulePlugin`). It owns one `DurexModuleState`, `DurexExtension`, and nested `PersistenceExtension` per project.

- [ ] Build-logic runtime locates consumer `durexDependencyRegistry` dynamically through Gradle shared-service registrations; do not cast to bootstrap classes across included-build classloaders.

- [ ] `DurexDependencyAccess.add(project, configuration, alias)` resolves alias, inserts required Gradle `platform(...)` once per `(configuration, platform)`, records active platform, then adds the library.

- [ ] `durex.library(alias)` returns `group:name:version` for explicit-version libraries and `group:name` for platform-managed libraries. A platform-managed alias is legal only after that platform is active in the project. Unknown alias is a Durex error.

- [ ] Exact type composition:

```text
durex.java-base:
  java + durex.module + Java25/toolchain/compiler; claims no module kind

durex.java-library:
  java-library + java-base; claims JAVA_LIBRARY; adds JUnit Jupiter/launcher

durex.spring-base:
  java-base; activates Spring platform; claims no module kind

durex.spring-library:
  java-library Gradle plugin + spring-base; claims SPRING_LIBRARY;
  adds spring-core + spring-test

durex.spring-service:
  org.springframework.boot + spring-base; claims SPRING_SERVICE;
  adds core + web + validation + observability + spring-test + web-test
```

Do **not** implement Spring service by applying Durex `java-library`; that would claim the wrong type.

- [ ] Java base uses toolchain 25, `options.release=25`, UTF-8. Plain Java library adds JUnit 5.8.2 and `useJUnitPlatform()`. Spring types rely on Boot test starter instead of injecting the old plain-Java JUnit version into the Spring graph.

- [ ] `DurexModuleState.claim` is idempotent for same kind and fails for a different kind, naming both kinds.

- [ ] Verify:

```bash
gradle -p build-logic/tests/java-library-smoke test --stacktrace
gradle -p build-logic/tests/spring-service-smoke verifyDurexService dependencies --configuration compileClasspath --stacktrace
gradle -p build-logic/tests/module-conflict help --stacktrace
```

First two PASS; conflict FAILS intentionally.

Commit: `feat: add Durex module type plugins`.

---

## Task 5 — Feature DSL and `durexCapabilities`

**Create/modify:**

```text
build-logic/src/main/groovy/durex.feature.jpa.gradle
build-logic/src/main/groovy/durex.feature.jdbc.gradle
build-logic/src/main/groovy/durex.feature.jooq.gradle
build-logic/src/main/groovy/durex.feature.redis.gradle
build-logic/src/main/groovy/durex.feature.native.gradle
build-logic/src/main/groovy/durex.feature.lombok.gradle
build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy
build-logic/src/main/groovy/com/github/durex/gradle/PersistenceExtension.groovy
build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy
build-logic/tests/features-smoke/**
build-logic/tests/feature-invalid/**
```

- [ ] **RED coexistence:** `durex.spring-service` + `persistence { jpa(); jooq() }` + `redis()`. **RED invalid:** `durex.java-library` + `nativeImage()`. Run `dependencies`/`help`; DSL methods initially missing.

- [ ] Map features:

```text
jpa    -> implementation spring-jpa
jdbc   -> implementation spring-jdbc
jooq   -> implementation spring-jooq
redis  -> implementation spring-redis
lombok -> compileOnly + annotationProcessor lombok
native -> apply org.graalvm.buildtools.native
```

JPA/JDBC/jOOQ/Redis require Spring library or service; Native requires Spring service; Lombok is valid on all module types. Repeated activation is idempotent.

- [ ] `durexCapabilities` stable sorted output includes type, Java, active platforms, active features, native flag. Coexistence fixture must print:

```text
Type: SPRING_SERVICE
Java: 25
Platforms: spring
Features: jpa,jooq,redis
Native: disabled
```

- [ ] Verify coexistence PASS; invalid fixture FAILS with `durex.feature.native requires durex.spring-service`.

Commit: `feat: add Durex dependency feature DSL`.

---

## Task 6 — Migrate Spring reference builds; prove Kotlin DSL compatibility

**Modify:**

```text
reference/spring-capabilities/settings.gradle.kts
reference/spring-capabilities/build.gradle.kts
reference/spring-native/settings.gradle.kts
reference/spring-native/build.gradle.kts
reference/spring-native/README.md
```

These files stay Kotlin DSL. If Durex type-safe accessors fail, fix the plugin/extension API; do not convert these fixtures to Groovy to bypass the problem.

- [ ] **Settings target for both builds:**

```kotlin
pluginManagement {
    includeBuild("../../build-bootstrap")
    includeBuild("../../build-logic")
    repositories { gradlePluginPortal(); mavenCentral() }
}
plugins { id("durex.settings") }

durexSettings {
    repositoryRoot.set(file("../.."))
    moduleDiscovery.set(false)
}

dependencyResolutionManagement {
    repositories { mavenCentral() }
}
```

Convert settings first; run native `tasks` and observe RED while old `sLibs` remains in build file.

- [ ] **Capabilities build target:**

```kotlin
plugins { id("durex.spring-service") }

durex {
    persistence {
        jpa()
        jdbc()
        jooq()
    }
    redis()
}
```

No catalog aliases or `apply(from=...)` remain.

- [ ] **Native build target:** `plugins { id("durex.spring-service") }` plus `durex { nativeImage() }`; preserve artifact name/application/`/hello` behavior.

- [ ] Verify:

```bash
gradle -p reference/spring-capabilities dependencies --configuration compileClasspath --stacktrace
gradle -p reference/spring-native test processAot bootJar --stacktrace
gradle -p reference/spring-native nativeTest --stacktrace
gradle -p reference/spring-native nativeCompile --stacktrace
```

- [ ] Update README ownership from `gradle/versions` + `gradle/library` to `gradle/dependencies`, Durex module type/features.

Commit: `refactor: use Durex build platform in Spring references`.

---

## Task 7 — Migrate jOOQ schema/supporting music modules off catalogs

**Modify:**

```text
build-logic/src/main/groovy/durex.jooq-schema.gradle
build-logic/tests/jooq-schema-smoke/settings.gradle.kts
core/shared/utils/build.spring.gradle
core/shared/jakarta/common/build.spring.gradle
core/shared/json-schema-annotation/build.spring.gradle
core/schema/music/json/build.spring.gradle
core/schema/music/entity/build.spring.gradle
core/schema/music/repo/build.spring.gradle
```

- [ ] Update jOOQ smoke settings to include both builds:

```kotlin
pluginManagement {
    includeBuild("../../../build-bootstrap")
    includeBuild("../..")
    repositories { gradlePluginPortal(); mavenCentral() }
}
plugins { id("durex.settings") }

durexSettings {
    repositoryRoot.set(file("../../.."))
    moduleDiscovery.set(false)
}

dependencyResolutionManagement {
    repositories { mavenCentral() }
}
```

This is required because `durex.jooq-schema` now consumes the consumer build's registry at runtime.

- [ ] `durex.jooq-schema` preserves official jOOQ plugin, DDLDatabase, Q/R naming, generated source directory, and `compileJava -> jooqCodegen`; it also adds manifest `jooq-core` for generated Q/R types.

- [ ] Establish RED by removing `dbLibs/gLibs` from `music-entity` before replacing them; compile must fail specifically on missing jOOQ/Lombok wiring.

- [ ] Convert module types:

```text
shared-utils            -> durex.java-library
shared-common           -> durex.java-library + lombok()
json-schema-annotation  -> durex.java-library + lombok()
music-json              -> durex.spring-library + org.jsonschema2pojo
music-entity            -> durex.java-library + durex.jooq-schema + lombok()
music-repo              -> durex.spring-library + persistence.jooq() + lombok()
```

Retain current source-set include/exclude rules.

- [ ] Replace uncommon external dependencies with `durex.library(...)`: `json-schema-pojo`, `swagger-annotations`, legacy javax bridge APIs. `music-json` uses platform-managed `jackson-annotations`, `jackson-databind`, `jakarta-validation` after Spring platform activation.

- [ ] Verify:

```bash
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/QMusic.java
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/records/RMusic.java
gradle -p migration/spring-music :music-json:compileJava :music-entity:compileJava :music-repo:compileJava --stacktrace
```

Commit: `refactor: migrate music support modules to Durex plugins`.

---

## Task 8 — Run real `music` migration through Durex manual discovery

**Create/modify:**

```text
migration/spring-music/modules.toml
migration/spring-music/settings.gradle
migration/spring-music/build.gradle
core/music/build.spring.gradle
```

- [ ] Create manual seven-project manifest; every entry uses existing physical path and `build-file = "build.spring.gradle"`:

```text
shared-utils -> core/shared/utils
shared-common -> core/shared/jakarta/common
json-schema-annotation -> core/shared/json-schema-annotation
music-json -> core/schema/music/json
music-entity -> core/schema/music/entity
music-repo -> core/schema/music/repo
music -> core/music
```

- [ ] Migration settings target:

```groovy
pluginManagement {
    includeBuild('../../build-bootstrap')
    includeBuild('../../build-logic')
    repositories { gradlePluginPortal(); mavenCentral() }
}
plugins { id 'durex.settings' }

durexSettings {
    repositoryRoot.set(file('../..'))
    modulesManifest.set(file('modules.toml'))
}

dependencyResolutionManagement {
    repositories { mavenCentral() }
}
```

Delete all Version Catalog registration and `springProject(...)` calls. Run:

```bash
gradle -p migration/spring-music projects durexProjects --stacktrace
```

Expected exactly the same seven logical projects, source `manual`, build file `build.spring.gradle`.

- [ ] `core/music/build.spring.gradle` target:

```groovy
plugins { id 'durex.spring-service' }

durex {
    persistence { jooq() }
    lombok()
}
```

Keep existing migration source-set rules and business project dependencies. Replace bridge/H2 coordinates with `durex.library('javax-cdi')`, `durex.library('javax-inject')`, `durex.library('javax-transaction')`, and `durex.library('h2')`.

- [ ] Remove migration-root `java-library`, Java 21 source/target/release/encoding conventions now owned by module types; retain group/version policy only.

- [ ] Verify real runtime:

```bash
gradle -p migration/spring-music :music:compileJava --stacktrace
gradle -p migration/spring-music :music:test --stacktrace
```

Expected all CRUD MockMvc tests and H2 -> Boot DataSource -> jOOQ `DSLContext` -> repository -> service -> HTTP integration test green.

- [ ] Assert migrated descriptors/settings have no catalog access:

```bash
grep -R -n -E 'VersionCatalogsExtension|[a-z]Libs\.' \
  migration/spring-music \
  core/music/build.spring.gradle \
  core/schema/music/*/build.spring.gradle \
  core/shared/*/build.spring.gradle \
  core/shared/*/*/build.spring.gradle || true
```

Expected no hits.

Commit: `refactor: run music migration on Durex build platform`.

---

## Task 9 — CI evidence

**Create/modify:**

```text
.github/workflows/durex-build-platform.yml
.github/workflows/spring-native-reference.yml
.github/workflows/spring-music.yml
```

- [ ] Add `Durex Build Platform` job on Java 25 + Gradle 9.1 for positive fixtures: manifest, plugin resolution, auto/manual discovery, Java library, Spring service, features, jOOQ schema.

- [ ] For intended-failure fixtures (manifest cycle/duplicate/unknown-ref/version-conflict/unsupported, strict-auto, project conflict, module-type conflict, invalid-native), shell must invert the command result **and grep the exact expected Durex error**. Do not use unchecked `continue-on-error`.

- [ ] Both Spring workflows add path filters:

```text
build-bootstrap/**
build-logic/**
gradle/dependencies/**
```

Remove old `gradle/library/spring-*` / version-catalog triggers only when the corresponding reference/migration build no longer consumes them.

- [ ] Preserve Spring Music evidence:

```bash
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace
gradle -p migration/spring-music projects --stacktrace
gradle -p migration/spring-music :music:compileJava --stacktrace
gradle -p migration/spring-music :music:test --stacktrace
```

- [ ] Preserve Spring Native JVM tests, AOT HTTP smoke, `nativeTest`, `nativeCompile`, and native `/hello` smoke.

- [ ] Required branch evidence:

```text
Durex Build Platform     green
Spring Music Migration   green
Spring Native Reference  JVM/AOT/native green
```

Do not call repository-wide CI green while unrelated legacy Quarkus/Shared Utils workflows remain red.

- [ ] Final scope verification:

```bash
gradle -p migration/spring-music :music:test --stacktrace
gradle -p reference/spring-native test processAot --stacktrace
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace
```

Inspect `git diff master...HEAD`; root `settings.gradle`, root wrapper, and legacy Quarkus descriptors must be untouched.

Commit: `ci: verify Durex build platform`.

---

## Follow-up boundary

This plan intentionally stops after design Phase 3: build-platform core + isolated references + real `music` migration. A separate plan owns Phase 4/5:

```text
remaining module conversion
root Gradle 9.1 promotion
root durex.settings activation
remove root Version Catalog registration
remove deprecated gradle/library/spring-*.gradle
remove old gradle/versions/*.toml after every consumer is gone
replace/remove legacy gradle/extensions/modules.gradle
```

Do not perform those cutover changes opportunistically while executing this plan.
