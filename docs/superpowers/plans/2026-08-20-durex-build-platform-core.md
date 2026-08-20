# Durex Build Platform Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the Durex-owned Gradle dependency/module platform, prove it with isolated Spring/native fixtures, and migrate the existing Spring `music` build away from Gradle Version Catalogs and repeated `apply from` capability scripts without cutting over the legacy root build yet.

**Architecture:** `build-bootstrap` owns TOML parsing, per-build registries, settings/plugin bootstrap, and automatic/manual module discovery. The independent `build-logic` included build consumes the same TOML source through its own build-local registry and provides module-type plus feature convention plugins. Modules use `durex.spring-service`, feature DSL calls such as `persistence.jooq()`, and `durex.library("alias")` for uncommon manifest-managed external libraries; normal inter-project dependencies remain standard `project(...)` dependencies.

**Tech Stack:** Gradle 9.1, Java/GraalVM 25, Groovy Gradle plugins, `org.tomlj:tomlj:1.1.1`, Spring Boot 4.1.0, GraalVM Build Tools 1.1.1, jOOQ 3.21.5, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-08-20-durex-build-platform-design.md`

## Global Constraints

- TOML under `gradle/dependencies/` is the single source of application/framework dependency and Gradle-plugin versions.
- Gradle Version Catalog APIs (`VersionCatalogsExtension`, `sLibs`, `dbLibs`, `gLibs`, etc.) must not be used by new Durex build-platform code or migrated fixtures/modules.
- Gradle BOM/platform and Gradle's resolver remain authoritative for alignment and conflict resolution; Durex does not implement a dependency resolver.
- `build-bootstrap` may hard-code only its bootstrap implementation dependency `org.tomlj:tomlj:1.1.1`; Spring/jOOQ/GraalVM/Lombok versions come from Durex TOML.
- Included builds are isolated. Root and `build-logic` each parse the same manifest once into separate build-local registries.
- Module discovery supports `auto`, `manual`, and `strict-auto`; manual exclude/override wins over automatic discovery.
- A manual `[[module]]` may specify `build-file = "build.spring.gradle"` so migration builds can point at alternate descriptors without changing the real module directory.
- `durex.library("alias")` is the generic escape hatch for manifest-managed external libraries that are not represented by a feature plugin; feature plugins remain preferred for framework capabilities.
- `durex.jooq-schema` remains separate from runtime `durex.feature.jooq`.
- Java baseline for new Durex module types is 25. Existing business sources may remain Java-21-compatible; do not rewrite source merely to use Java 25 syntax.
- Keep the legacy root Gradle 7.4.2 / Quarkus build untouched in this plan. Root cutover is a follow-up plan after remaining legacy modules are migrated or removed.
- Preserve business `project(...)` dependencies explicitly.
- Use strict RED -> GREEN TDD and make a focused commit after every task.

---

## File Structure

New bootstrap files:

```text
build-bootstrap/
  settings.gradle.kts
  build.gradle.kts
  src/main/groovy/com/github/durex/gradle/manifest/
    DependencyManifestLoader.groovy
    DependencyRegistry.groovy
    VersionSpec.groovy
    PlatformSpec.groovy
    LibrarySpec.groovy
    PluginSpec.groovy
  src/main/groovy/com/github/durex/gradle/settings/
    DurexSettingsExtension.groovy
    DurexSettingsPlugin.groovy
    DurexBuildLogicSettingsPlugin.groovy
    DurexBuildLogicPlugin.groovy
    ProjectDiscovery.groovy
    ProjectRegistry.groovy
    ProjectSpec.groovy
  tests/
    manifest-valid/
    manifest-invalid/
    modules-auto/
    modules-manual/
    modules-conflict/
```

New dependency manifests:

```text
gradle/dependencies/durex.toml
gradle/dependencies/spring.toml
gradle/dependencies/database.toml
gradle/dependencies/test.toml
gradle/dependencies/utils.toml
```

New/expanded build logic:

```text
build-logic/src/main/groovy/com/github/durex/gradle/
  DurexModulePlugin.groovy
  DurexExtension.groovy
  PersistenceExtension.groovy
  DurexModuleState.groovy
  ModuleKind.groovy
  DurexDependencyAccess.groovy
build-logic/src/main/groovy/
  durex.java-base.gradle
  durex.spring-base.gradle
  durex.java-library.gradle
  durex.spring-library.gradle
  durex.spring-service.gradle
  durex.feature.jpa.gradle
  durex.feature.jdbc.gradle
  durex.feature.jooq.gradle
  durex.feature.redis.gradle
  durex.feature.native.gradle
  durex.feature.lombok.gradle
```

Functional fixtures remain real Gradle builds under `build-bootstrap/tests/` and `build-logic/tests/`; they are invoked with Gradle directly so bootstrap testing does not require another test-framework version.

---

### Task 1: Durex dependency manifest and bootstrap parser

**Files:**
- Create: `build-bootstrap/settings.gradle.kts`
- Create: `build-bootstrap/build.gradle.kts`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/DependencyManifestLoader.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/DependencyRegistry.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/VersionSpec.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/PlatformSpec.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/LibrarySpec.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/PluginSpec.groovy`
- Create: `gradle/dependencies/durex.toml`
- Create: `gradle/dependencies/spring.toml`
- Create: `gradle/dependencies/database.toml`
- Create: `gradle/dependencies/test.toml`
- Create: `gradle/dependencies/utils.toml`
- Create: `build-bootstrap/tests/manifest-valid/settings.gradle`
- Create: `build-bootstrap/tests/manifest-valid/build.gradle`
- Create: `build-bootstrap/tests/manifest-invalid/settings.gradle`
- Create: `build-bootstrap/tests/manifest-invalid/build.gradle`

**Interfaces:**
- Produces: `DependencyRegistry DependencyManifestLoader.load(File rootManifest)`.
- `DependencyRegistry` exposes `int javaVersion()`, `VersionSpec version(String id)`, `PlatformSpec platform(String id)`, `LibrarySpec library(String id)`, `PluginSpec plugin(String id)`.
- `LibrarySpec` exposes resolved `module`, optional resolved `version`, and optional `platform` after validation.
- `PluginSpec` exposes `id`, optional `module`, and resolved `version`.

- [ ] **Step 1: Create a RED bootstrap fixture that requests `durex.settings` before the plugin exists**

`build-bootstrap/tests/manifest-valid/settings.gradle`:

```groovy
pluginManagement {
    includeBuild('../..')
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    id 'durex.settings'
}

durexSettings {
    repositoryRoot = file('../../..')
    moduleDiscovery = false
}

rootProject.name = 'manifest-valid'
```

`build-bootstrap/tests/manifest-valid/build.gradle`:

```groovy
tasks.register('verifyDurexManifest') {
    doLast {
        def registry = gradle.sharedServices.registrations
            .getByName('durexDependencyRegistry').service.get()
        assert registry.javaVersion() == 25
        assert registry.version('spring-boot').value == '4.1.0'
        assert registry.platform('spring').coordinate() ==
            'org.springframework.boot:spring-boot-dependencies:4.1.0'
        assert registry.library('spring-jooq').module ==
            'org.springframework.boot:spring-boot-starter-jooq'
    }
}
```

- [ ] **Step 2: Run RED verification**

Run:

```bash
gradle -p build-bootstrap/tests/manifest-valid verifyDurexManifest --stacktrace
```

Expected: FAIL because plugin `durex.settings` does not exist.

- [ ] **Step 3: Add the bootstrap build and exact manifest baseline**

`build-bootstrap/build.gradle.kts` must apply `groovy-gradle-plugin`, use `mavenCentral()`/`gradlePluginPortal()`, and contain only this third-party implementation dependency:

```kotlin
dependencies {
    implementation("org.tomlj:tomlj:1.1.1")
}
```

Register binary plugins:

```kotlin
gradlePlugin {
    plugins {
        create("durexSettings") {
            id = "durex.settings"
            implementationClass = "com.github.durex.gradle.settings.DurexSettingsPlugin"
        }
        create("durexBuildLogicSettings") {
            id = "durex.build-logic-settings"
            implementationClass = "com.github.durex.gradle.settings.DurexBuildLogicSettingsPlugin"
        }
        create("durexBuildLogic") {
            id = "durex.build-logic"
            implementationClass = "com.github.durex.gradle.settings.DurexBuildLogicPlugin"
        }
    }
}
```

Create manifest data with these exact current migration baselines:

```toml
# gradle/dependencies/durex.toml
include = ["spring.toml", "database.toml", "test.toml", "utils.toml"]
[java]
version = 25
```

```toml
# gradle/dependencies/spring.toml
[versions]
spring-boot = "4.1.0"
graal-native = "1.1.1"

[platforms.spring]
module = "org.springframework.boot:spring-boot-dependencies"
version.ref = "spring-boot"

[libraries.spring-core]
module = "org.springframework.boot:spring-boot-starter"
platform = "spring"
[libraries.spring-web]
module = "org.springframework.boot:spring-boot-starter-webmvc"
platform = "spring"
[libraries.spring-validation]
module = "org.springframework.boot:spring-boot-starter-validation"
platform = "spring"
[libraries.spring-observability]
module = "org.springframework.boot:spring-boot-starter-actuator"
platform = "spring"
[libraries.spring-test]
module = "org.springframework.boot:spring-boot-starter-test"
platform = "spring"
[libraries.spring-web-test]
module = "org.springframework.boot:spring-boot-starter-webmvc-test"
platform = "spring"
[libraries.spring-jpa]
module = "org.springframework.boot:spring-boot-starter-data-jpa"
platform = "spring"
[libraries.spring-jdbc]
module = "org.springframework.boot:spring-boot-starter-jdbc"
platform = "spring"
[libraries.spring-jooq]
module = "org.springframework.boot:spring-boot-starter-jooq"
platform = "spring"
[libraries.spring-redis]
module = "org.springframework.boot:spring-boot-starter-data-redis"
platform = "spring"
[libraries.jackson-annotations]
module = "com.fasterxml.jackson.core:jackson-annotations"
platform = "spring"
[libraries.jackson-databind]
module = "com.fasterxml.jackson.core:jackson-databind"
platform = "spring"
[libraries.jakarta-validation]
module = "jakarta.validation:jakarta.validation-api"
platform = "spring"
[libraries.h2]
module = "com.h2database:h2"
platform = "spring"

[plugins.spring-boot]
id = "org.springframework.boot"
module = "org.springframework.boot:spring-boot-gradle-plugin"
version.ref = "spring-boot"
[plugins.graalvm-native]
id = "org.graalvm.buildtools.native"
module = "org.graalvm.buildtools:native-gradle-plugin"
version.ref = "graal-native"
```

`database.toml` must define `jooq = "3.21.5"`, `jooq-core`, `jooq-meta`, `jooq-codegen`, `jooq-meta-extensions`, plus `plugins.jooq-codegen` with id `org.jooq.jooq-codegen-gradle`, module `org.jooq:jooq-codegen-gradle`, and `version.ref = "jooq"`.

`utils.toml` must define `lombok = "1.18.46"`, `jsonschema2pojo = "1.3.3"`, `json-schema-pojo = "1.1.2"`, `swagger-annotations = "2.2.0"`, and aliases for the existing migration bridge APIs: CDI `2.0.SP1`, javax.inject `1`, javax.transaction `1.3`, javax.validation `2.0.1.Final`, javax.interceptor `1.2.2`. `plugins.jsonschema2pojo` uses id `org.jsonschema2pojo` and `version.ref = "jsonschema2pojo"`; its `module` is intentionally omitted because root plugin resolution can use the plugin marker and version.

`test.toml` must define JUnit `5.8.2`, `junit-jupiter`, and `junit-platform-launcher` `1.8.2` for plain Java-library defaults.

- [ ] **Step 4: Implement strict parsing/validation**

`DependencyManifestLoader.load(File)` must recursively load explicit `include` entries, reject cycles/duplicates/unknown references/unsupported top-level sections, resolve all `version.ref` values, enforce exactly one version owner for libraries (`version`, `version.ref`, or `platform`), and produce file/id-specific `GradleException` messages.

- [ ] **Step 5: Add invalid-manifest fixture**

Use a fixture with one library containing both `platform = "spring"` and `version = "1.0"`; the build must fail with:

```text
Durex dependency manifest error
Library: broken
Problem: platform and explicit version ownership are mutually exclusive
```

- [ ] **Step 6: Run GREEN verification**

Run:

```bash
gradle -p build-bootstrap/tests/manifest-valid verifyDurexManifest --stacktrace
gradle -p build-bootstrap/tests/manifest-invalid help --stacktrace
```

Expected: first command PASS; second command FAIL for the intended Durex validation error only.

- [ ] **Step 7: Commit**

```bash
git add -- build-bootstrap gradle/dependencies
git commit -m "build: add Durex dependency manifest bootstrap"
```

---

### Task 2: Settings plugin, build-local registry, and module discovery

**Files:**
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexSettingsExtension.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexSettingsPlugin.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/ProjectDiscovery.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/ProjectRegistry.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/ProjectSpec.groovy`
- Create: `build-bootstrap/tests/modules-auto/**`
- Create: `build-bootstrap/tests/modules-manual/**`
- Create: `build-bootstrap/tests/modules-conflict/**`
- Create: `gradle/modules.toml`

**Interfaces:**
- `durexSettings.repositoryRoot` defaults to `settings.rootDir`.
- `durexSettings.dependencyManifest` defaults to `${repositoryRoot}/gradle/dependencies/durex.toml`.
- `durexSettings.modulesManifest` defaults to `${repositoryRoot}/gradle/modules.toml`.
- `durexSettings.moduleDiscovery` defaults to `true`; when false only dependency/plugin bootstrap runs.
- Manual module schema supports `name`, `path`, and optional `build-file`.
- Paths in `modules.toml` resolve against `repositoryRoot`, not the directory containing the TOML file.

- [ ] **Step 1: Write RED automatic/manual discovery fixtures**

Auto fixture tree:

```text
modules-auto/core/music/build.gradle
modules-auto/core/shared/utils/build.gradle
modules-auto/core/schema/music/repo/build.gradle
```

with:

```toml
[discovery]
mode = "auto"
roots = ["core"]
```

Manual fixture:

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

- [ ] **Step 2: Run RED discovery verification**

Run:

```bash
gradle -p build-bootstrap/tests/modules-auto projects --stacktrace
gradle -p build-bootstrap/tests/modules-manual projects --stacktrace
```

Expected: FAIL because discovery/registration is not implemented.

- [ ] **Step 3: Implement deterministic naming and precedence**

Naming rules in order:

```text
core/<name>                    -> :<name>
core/shared/<name>             -> :shared-<name>
core/schema/<domain>/<kind>    -> :<domain>-<kind>
other discovered path          -> ':' + relative path segments joined with '-'
```

Then apply precedence:

```text
manual exclude > manual include/override > auto discovery > default naming
```

If a manual declaration names an already discovered physical path, replace the inferred logical name rather than creating a second project.

- [ ] **Step 4: Implement `auto`, `manual`, and `strict-auto` plus `build-file`**

For every normalized `ProjectSpec`, call:

```groovy
settings.include(spec.gradlePath)
def descriptor = settings.project(spec.gradlePath)
descriptor.projectDir = spec.directory
if (spec.buildFile) {
    descriptor.buildFileName = spec.buildFile
}
```

Reject duplicate logical paths and one physical directory mapped to multiple logical projects.

- [ ] **Step 5: Add `durexProjects` diagnostic**

Register a root task that prints stable lines:

```text
:music | core/music | auto | build.gradle
:music-repo | core/schema/music/repo | auto | build.gradle
:admin-tool | tools/admin | manual | build.gradle
```

- [ ] **Step 6: Verify GREEN and conflict failure**

Run:

```bash
gradle -p build-bootstrap/tests/modules-auto projects durexProjects --stacktrace
gradle -p build-bootstrap/tests/modules-manual projects durexProjects --stacktrace
gradle -p build-bootstrap/tests/modules-conflict projects --stacktrace
```

Expected: first two PASS with exact logical names; conflict fixture FAILS with a Durex duplicate-project error.

- [ ] **Step 7: Commit**

```bash
git add -- build-bootstrap gradle/modules.toml
git commit -m "build: add Durex module discovery"
```

---

### Task 3: Bootstrap `build-logic` from the same manifest

**Files:**
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexBuildLogicSettingsPlugin.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexBuildLogicPlugin.groovy`
- Modify: `build-logic/settings.gradle.kts`
- Modify: `build-logic/build.gradle.kts`

**Interfaces:**
- `durex.build-logic-settings` loads the same Durex manifest into the `build-logic` Gradle build and registers `durexDependencyRegistry` there.
- `durex.build-logic` reads that build-local service and adds external plugin implementation modules required by convention plugins: Spring Boot, GraalVM Native, jOOQ codegen, plus jOOQ core/meta.

- [ ] **Step 1: Write RED build-logic settings/build files**

Target `build-logic/settings.gradle.kts`:

```kotlin
pluginManagement {
    includeBuild("../build-bootstrap")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    id("durex.build-logic-settings")
}

durexBuildLogicSettings {
    repositoryRoot = file("..")
}

rootProject.name = "durex-build-logic"
```

Target `build-logic/build.gradle.kts`:

```kotlin
plugins {
    `groovy-gradle-plugin`
    id("durex.build-logic")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}
```

- [ ] **Step 2: Run RED verification**

```bash
gradle -p build-logic tasks --stacktrace
```

Expected: FAIL because bootstrap build-logic plugins are not implemented.

- [ ] **Step 3: Implement build-local registry bootstrap and dependency injection**

`DurexBuildLogicPlugin` must add exactly these manifest-backed implementation dependencies:

```text
plugins.spring-boot.module
plugins.graalvm-native.module
plugins.jooq-codegen.module
libraries.jooq-core
libraries.jooq-meta
```

Do not reintroduce a catalog or hard-coded version string.

- [ ] **Step 4: Remove `dbLibs` catalog registration and direct catalog access**

`build-logic/settings.gradle.kts` must contain no `versionCatalogs` block; `build-logic/build.gradle.kts` must contain no `dbLibs` reference.

- [ ] **Step 5: Verify existing jOOQ schema plugin still compiles**

```bash
gradle -p build-logic tasks --stacktrace
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/QMusic.java
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/records/RMusic.java
```

Expected: all PASS.

- [ ] **Step 6: Commit**

```bash
git add -- build-bootstrap build-logic
git commit -m "build: bootstrap Durex build logic from manifest"
```

---

### Task 4: Module types, platform-aware dependency access, and generic library aliases

**Files:**
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/PersistenceExtension.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/DurexModuleState.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/ModuleKind.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/DurexDependencyAccess.groovy`
- Create: `build-logic/src/main/groovy/durex.java-base.gradle`
- Create: `build-logic/src/main/groovy/durex.spring-base.gradle`
- Create: `build-logic/src/main/groovy/durex.java-library.gradle`
- Create: `build-logic/src/main/groovy/durex.spring-library.gradle`
- Create: `build-logic/src/main/groovy/durex.spring-service.gradle`
- Create: `build-logic/tests/java-library-smoke/**`
- Create: `build-logic/tests/spring-service-smoke/**`
- Create: `build-logic/tests/module-conflict/**`

**Interfaces:**
- `ModuleKind`: `JAVA_LIBRARY`, `SPRING_LIBRARY`, `SPRING_SERVICE`.
- `DurexModuleState.claim(ModuleKind)` rejects a second different type.
- `DurexExtension.library(String alias)` returns normal Gradle dependency notation from the registry; for a platform-managed alias, the required platform must already be active or it throws a Durex error.
- `DurexDependencyAccess.add(Project, String configuration, String alias)` adds the required platform once per `(configuration, platform)` pair and then adds the library.

- [ ] **Step 1: Create RED smoke builds**

Spring service build:

```groovy
plugins {
    id 'durex.spring-service'
}

tasks.register('verifyDurexService') {
    doLast {
        assert plugins.hasPlugin('org.springframework.boot')
        assert java.toolchain.languageVersion.get().asInt() == 25
    }
}
```

Conflict build:

```groovy
plugins {
    id 'durex.spring-service'
    id 'durex.java-library'
}
```

- [ ] **Step 2: Run RED**

```bash
gradle -p build-logic/tests/java-library-smoke test --stacktrace
gradle -p build-logic/tests/spring-service-smoke verifyDurexService dependencies --configuration compileClasspath --stacktrace
gradle -p build-logic/tests/module-conflict help --stacktrace
```

Expected: missing Durex module-type plugins.

- [ ] **Step 3: Implement module state and dependency helper**

`DurexDependencyAccess.add` must resolve registry aliases dynamically through the build-local `durexDependencyRegistry` service, add `project.dependencies.platform(platform.coordinate())` before a platform-managed library, and de-duplicate platform insertion per configuration.

`DurexExtension.library(alias)` must support usage like:

```groovy
dependencies {
    implementation durex.library('json-schema-pojo')
    compileOnly durex.library('javax-cdi')
}
```

- [ ] **Step 4: Implement module defaults**

`durex.java-library`:

```text
java-library, Java 25, UTF-8, options.release=25,
JUnit Jupiter + launcher, useJUnitPlatform()
```

`durex.spring-library`:

```text
durex.java-library + Spring platform on api/implementation/testImplementation,
spring-core, spring-test
```

`durex.spring-service`:

```text
Java base + org.springframework.boot + Spring platform,
spring-core, spring-web, spring-validation, spring-observability,
spring-test, spring-web-test
```

- [ ] **Step 5: Verify platform de-duplication and conflict behavior**

Run:

```bash
gradle -p build-logic/tests/spring-service-smoke dependencies --configuration compileClasspath --stacktrace
gradle -p build-logic/tests/module-conflict help --stacktrace
```

Expected: Spring BOM appears once in the Durex model for `compileClasspath`; conflict build fails with both requested module kinds named.

- [ ] **Step 6: Commit**

```bash
git add -- build-logic
git commit -m "feat: add Durex module type plugins"
```

---

### Task 5: Feature DSL, prerequisites, and capabilities diagnostics

**Files:**
- Create: `build-logic/src/main/groovy/durex.feature.jpa.gradle`
- Create: `build-logic/src/main/groovy/durex.feature.jdbc.gradle`
- Create: `build-logic/src/main/groovy/durex.feature.jooq.gradle`
- Create: `build-logic/src/main/groovy/durex.feature.redis.gradle`
- Create: `build-logic/src/main/groovy/durex.feature.native.gradle`
- Create: `build-logic/src/main/groovy/durex.feature.lombok.gradle`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/PersistenceExtension.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy`
- Create: `build-logic/tests/features-smoke/**`
- Create: `build-logic/tests/feature-invalid/**`

**Interfaces:**
- `persistence.jpa()` -> applies `durex.feature.jpa`.
- `persistence.jdbc()` -> applies `durex.feature.jdbc`.
- `persistence.jooq()` -> applies `durex.feature.jooq`.
- `redis()` -> applies `durex.feature.redis`.
- `nativeImage()` -> applies `durex.feature.native`.
- `lombok()` -> applies `durex.feature.lombok`.
- Repeated activation is idempotent.

- [ ] **Step 1: Write RED coexistence fixture**

```groovy
plugins {
    id 'durex.spring-service'
}

durex {
    persistence {
        jpa()
        jooq()
    }
    redis()
}
```

Invalid fixture:

```groovy
plugins {
    id 'durex.java-library'
}

durex {
    nativeImage()
}
```

- [ ] **Step 2: Run RED**

```bash
gradle -p build-logic/tests/features-smoke dependencies --configuration compileClasspath --stacktrace
gradle -p build-logic/tests/feature-invalid help --stacktrace
```

Expected: DSL methods do not yet exist.

- [ ] **Step 3: Implement features and prerequisites**

Dependency aliases:

```text
jpa   -> spring-jpa
jdbc  -> spring-jdbc
jooq  -> spring-jooq
redis -> spring-redis
lombok -> lombok on compileOnly + annotationProcessor
native -> apply org.graalvm.buildtools.native
```

JPA/JDBC/jOOQ/Redis require `SPRING_LIBRARY` or `SPRING_SERVICE`; Native requires `SPRING_SERVICE`; Lombok is allowed for all module types.

- [ ] **Step 4: Add `durexCapabilities` task**

For the coexistence fixture output must include:

```text
Type: SPRING_SERVICE
Java: 25
Platforms: spring
Features: jpa,jooq,redis
Native: disabled
```

Keep feature output sorted for stable CI assertions.

- [ ] **Step 5: Verify GREEN**

```bash
gradle -p build-logic/tests/features-smoke durexCapabilities dependencies --configuration compileClasspath --stacktrace
gradle -p build-logic/tests/feature-invalid help --stacktrace
```

Expected: coexistence PASS; invalid fixture fails with `durex.feature.native requires durex.spring-service`.

- [ ] **Step 6: Commit**

```bash
git add -- build-logic
git commit -m "feat: add Durex dependency feature DSL"
```

---

### Task 6: Migrate Spring capability/native reference builds to Durex plugins

**Files:**
- Modify: `reference/spring-capabilities/settings.gradle.kts`
- Modify: `reference/spring-capabilities/build.gradle.kts`
- Modify: `reference/spring-native/settings.gradle.kts`
- Modify: `reference/spring-native/build.gradle.kts`
- Modify: `reference/spring-native/README.md`

**Interfaces:**
- Both isolated builds include `build-bootstrap` and `build-logic` through `pluginManagement` and apply `durex.settings` with `repositoryRoot = ../..` and `moduleDiscovery = false`.
- Neither reference build registers a Version Catalog.

- [ ] **Step 1: Convert reference settings first and verify RED**

Target shape:

```kotlin
pluginManagement {
    includeBuild("../../build-bootstrap")
    includeBuild("../../build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    id("durex.settings")
}

durexSettings {
    repositoryRoot = file("../..")
    moduleDiscovery = false
}
```

Run:

```bash
gradle -p reference/spring-native tasks --stacktrace
```

Expected RED until module build files stop using `sLibs`.

- [ ] **Step 2: Replace capability reference build**

Use:

```groovy
plugins {
    id 'durex.spring-service'
}

durex {
    persistence {
        jpa()
        jdbc()
        jooq()
    }
    redis()
}
```

No `apply from` and no catalog aliases remain.

- [ ] **Step 3: Replace native reference build**

Use `durex.spring-service` plus:

```groovy
durex {
    nativeImage()
}
```

Keep the existing application code and HTTP behavior unchanged.

- [ ] **Step 4: Run JVM/AOT/native verification**

```bash
gradle -p reference/spring-capabilities dependencies --configuration compileClasspath --stacktrace
gradle -p reference/spring-native test processAot bootJar --stacktrace
gradle -p reference/spring-native nativeTest --stacktrace
gradle -p reference/spring-native nativeCompile --stacktrace
```

Expected: all PASS; `/hello` behavior remains `Hello from Spring Native` in the existing smoke workflow.

- [ ] **Step 5: Update README ownership description**

Replace references to `gradle/versions/spring.versions.toml` and `gradle/library/spring-*.gradle` with `gradle/dependencies/*.toml`, `durex.spring-service`, and `nativeImage()`.

- [ ] **Step 6: Commit**

```bash
git add -- reference/spring-capabilities reference/spring-native
git commit -m "refactor: use Durex build platform in Spring references"
```

---

### Task 7: Move jOOQ/schema and supporting music modules off catalogs

**Files:**
- Modify: `build-logic/src/main/groovy/durex.jooq-schema.gradle`
- Modify: `build-logic/tests/jooq-schema-smoke/settings.gradle.kts`
- Modify: `core/schema/music/entity/build.spring.gradle`
- Modify: `core/schema/music/repo/build.spring.gradle`
- Modify: `core/schema/music/json/build.spring.gradle`
- Modify: `core/shared/utils/build.spring.gradle`
- Modify: `core/shared/jakarta/common/build.spring.gradle`
- Modify: `core/shared/json-schema-annotation/build.spring.gradle`

**Interfaces:**
- `durex.jooq-schema` adds the generated-code runtime `jooq-core` dependency itself because generated Q/R types require jOOQ API.
- `durex.jooq-schema` keeps DDLDatabase, Q/R naming, generated source directory, and compileJava dependency behavior unchanged.
- Direct external Gradle plugin `org.jsonschema2pojo` is version-resolved by `durex.settings` from `plugins.jsonschema2pojo`; its module need not be on `build-logic` compile classpath.

- [ ] **Step 1: Make catalog removal RED**

Remove `gLibs`, `dbLibs`, `uLibs`, `sLibs` use from the alternate module descriptors and replace only the plugin/module-type declarations, leaving external dependencies unresolved initially.

Run:

```bash
gradle -p migration/spring-music :music-entity:compileJava --stacktrace
```

Expected: RED on unresolved old aliases or missing Durex library aliases.

- [ ] **Step 2: Convert module types and library aliases**

Use these module types:

```text
shared-utils            -> durex.java-library
shared-common           -> durex.java-library + lombok()
json-schema-annotation  -> durex.java-library + lombok()
music-json              -> durex.spring-library + org.jsonschema2pojo
music-entity            -> durex.java-library + durex.jooq-schema + lombok()
music-repo              -> durex.spring-library + persistence.jooq() + lombok()
```

Use `durex.library(...)` for non-feature external dependencies such as `json-schema-pojo`, `swagger-annotations`, and legacy javax bridge APIs.

For `music-json`, use:

```groovy
dependencies {
    api durex.library('jackson-annotations')
    api durex.library('jackson-databind')
    api durex.library('jakarta-validation')
}
```

- [ ] **Step 3: Keep jOOQ generation contract green**

```bash
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/QMusic.java
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/records/RMusic.java
```

- [ ] **Step 4: Compile the supporting music graph**

```bash
gradle -p migration/spring-music :music-json:compileJava :music-entity:compileJava :music-repo:compileJava --stacktrace
```

Expected: PASS with no Version Catalog accessor use in these descriptors.

- [ ] **Step 5: Commit**

```bash
git add -- build-logic core/schema/music core/shared
git commit -m "refactor: migrate music support modules to Durex plugins"
```

---

### Task 8: Use Durex manual discovery and module types for the real music migration build

**Files:**
- Modify: `migration/spring-music/settings.gradle`
- Modify: `migration/spring-music/build.gradle`
- Create: `migration/spring-music/modules.toml`
- Modify: `core/music/build.spring.gradle`

**Interfaces:**
- Migration settings uses `durex.settings` with `repositoryRoot = ../..` and `modulesManifest = migration/spring-music/modules.toml`.
- `modules.toml` is `mode = "manual"` and every mapped project uses `build-file = "build.spring.gradle"`.
- `core/music/build.spring.gradle` becomes `durex.spring-service` + `persistence.jooq()` + `lombok()` plus business project dependencies and the temporary source-set/legacy javax bridge only.

- [ ] **Step 1: Write the manual module manifest**

```toml
[discovery]
mode = "manual"

[[module]]
name = "shared-utils"
path = "core/shared/utils"
build-file = "build.spring.gradle"

[[module]]
name = "shared-common"
path = "core/shared/jakarta/common"
build-file = "build.spring.gradle"

[[module]]
name = "json-schema-annotation"
path = "core/shared/json-schema-annotation"
build-file = "build.spring.gradle"

[[module]]
name = "music-json"
path = "core/schema/music/json"
build-file = "build.spring.gradle"

[[module]]
name = "music-entity"
path = "core/schema/music/entity"
build-file = "build.spring.gradle"

[[module]]
name = "music-repo"
path = "core/schema/music/repo"
build-file = "build.spring.gradle"

[[module]]
name = "music"
path = "core/music"
build-file = "build.spring.gradle"
```

- [ ] **Step 2: Convert settings and verify project graph before touching music build file**

`settings.gradle` must include bootstrap/build-logic, apply `durex.settings`, configure `repositoryRoot`, point `modulesManifest` to the migration-local file, and keep `mavenCentral()` in `dependencyResolutionManagement`. Delete all Version Catalog registration and all manual `springProject(...)` calls.

Run:

```bash
gradle -p migration/spring-music projects durexProjects --stacktrace
```

Expected: exactly the same seven logical projects as the current migration graph.

- [ ] **Step 3: Convert `core/music/build.spring.gradle`**

Top-level target:

```groovy
plugins {
    id 'durex.spring-service'
}

durex {
    persistence {
        jooq()
    }
    lombok()
}
```

Keep the current Spring migration source-set exclusions and business project dependencies. Replace hard-coded legacy bridge dependencies with `durex.library('javax-cdi')`, `durex.library('javax-inject')`, and `durex.library('javax-transaction')`; replace H2 with `durex.library('h2')`.

- [ ] **Step 4: Remove migration root Java conventions now owned by module types**

`migration/spring-music/build.gradle` keeps only group/version policy. Remove `apply plugin: 'java-library'`, Java source/target compatibility, and JavaCompile release/encoding settings from the root migration build.

- [ ] **Step 5: Run full existing real-runtime verification**

```bash
gradle -p migration/spring-music :music:compileJava --stacktrace
gradle -p migration/spring-music :music:test --stacktrace
```

Expected: all existing Spring MVC CRUD tests and the H2 -> DSLContext -> repository -> service -> HTTP integration test remain green.

- [ ] **Step 6: Assert the migration build is catalog-free**

Search command:

```bash
grep -R -n -E 'VersionCatalogsExtension|[a-z]Libs\.' migration/spring-music core/music/build.spring.gradle core/schema/music/*/build.spring.gradle core/shared/*/build.spring.gradle core/shared/*/*/build.spring.gradle || true
```

Expected: no catalog-access hits in the migrated Spring descriptors/settings.

- [ ] **Step 7: Commit**

```bash
git add -- migration/spring-music core/music/build.spring.gradle
git commit -m "refactor: run music migration on Durex build platform"
```

---

### Task 9: CI verification for bootstrap, references, and real music migration

**Files:**
- Create: `.github/workflows/durex-build-platform.yml`
- Modify: `.github/workflows/spring-native-reference.yml`
- Modify: `.github/workflows/spring-music.yml`

**Interfaces:**
- Build-platform workflow verifies bootstrap/discovery/module/feature fixtures independently of business code.
- Existing Spring Native and Spring Music workflows remain end-to-end evidence, updated to trigger on `build-bootstrap/**`, `build-logic/**`, and `gradle/dependencies/**`.

- [ ] **Step 1: Add build-platform CI**

Use Java 25 + Gradle 9.1 and run:

```bash
gradle -p build-bootstrap/tests/manifest-valid verifyDurexManifest --stacktrace
gradle -p build-bootstrap/tests/modules-auto projects durexProjects --stacktrace
gradle -p build-bootstrap/tests/modules-manual projects durexProjects --stacktrace
gradle -p build-logic/tests/java-library-smoke test --stacktrace
gradle -p build-logic/tests/spring-service-smoke verifyDurexService --stacktrace
gradle -p build-logic/tests/features-smoke durexCapabilities --stacktrace
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace
```

For intentional failure fixtures, shell steps must invert the result and assert the expected Durex error string; do not mark expected failures with `continue-on-error` without checking the message.

- [ ] **Step 2: Update workflow path filters**

Both Spring workflows must include:

```text
build-bootstrap/**
build-logic/**
gradle/dependencies/**
```

Remove migrated Spring-path dependence on `gradle/library/spring-*.gradle` and `gradle/versions/spring.versions.toml` where no longer consumed.

- [ ] **Step 3: Run/observe all three workflows on the branch**

Required green evidence:

```text
Durex Build Platform      -> green
Spring Music Migration    -> green
Spring Native Reference   -> JVM/AOT/native green
```

Do not claim repository-wide CI green while unrelated legacy Quarkus/Shared Utils workflows remain red.

- [ ] **Step 4: Final repository verification**

Confirm:

```bash
gradle -p migration/spring-music :music:test --stacktrace
gradle -p reference/spring-native test processAot --stacktrace
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace
```

and inspect `git diff master...HEAD` for accidental root `settings.gradle`, wrapper, or legacy Quarkus changes. There must be none in this plan.

- [ ] **Step 5: Commit**

```bash
git add -- .github/workflows
git commit -m "ci: verify Durex build platform"
```

---

## Follow-up Plan Boundary

This plan deliberately stops after Phase 3 of the design: platform core + isolated references + real `music` migration. A separate implementation plan is required for Phase 4/5 after the remaining legacy modules are inventoried and migrated/removed. That follow-up owns:

```text
remaining module conversion
root Gradle 9.1 promotion
root durex.settings activation
deletion of root Version Catalog registration
deletion of deprecated gradle/library/spring-*.gradle
deletion of old gradle/versions/*.toml after all consumers are gone
replacement/removal of legacy gradle/extensions/modules.gradle
```

Do not perform those cutover operations opportunistically while executing this plan.
