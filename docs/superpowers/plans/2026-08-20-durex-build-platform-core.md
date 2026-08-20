# Durex Build Platform Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the Durex-owned Gradle dependency/module platform, prove it with isolated Spring/native fixtures, and migrate the existing Spring `music` build away from Gradle Version Catalogs and repeated `apply from` capability scripts without cutting over the legacy root build yet.

**Architecture:** `build-bootstrap` owns Durex TOML parsing, build-local registries, settings/plugin bootstrap, and automatic/manual module discovery. The independent `build-logic` included build loads the same TOML source through its own build-local registry and provides module-type plus feature plugins. Consumer modules declare a module type, optional feature DSL such as `persistence.jooq()`, explicit business `project(...)` dependencies, and only use `durex.library("alias")` for uncommon external libraries that are intentionally not modeled as features.

**Tech Stack:** Gradle 9.1, Java/GraalVM 25, Groovy Gradle plugins, `org.tomlj:tomlj:1.1.1`, Spring Boot 4.1.0, GraalVM Build Tools 1.1.1, jOOQ 3.21.5, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-08-20-durex-build-platform-design.md`

## Global Constraints

- TOML under `gradle/dependencies/` is the single source of application/framework dependency and Gradle-plugin versions.
- New Durex build-platform code and migrated fixtures/modules must not use Gradle Version Catalog APIs (`VersionCatalogsExtension`, `sLibs`, `dbLibs`, `gLibs`, etc.).
- Gradle BOM/platform and Gradle's resolver remain authoritative for alignment and conflict resolution; Durex does not implement a dependency resolver.
- `build-bootstrap` may hard-code only its bootstrap implementation dependency `org.tomlj:tomlj:1.1.1`; Spring/jOOQ/GraalVM/Lombok versions come from Durex TOML.
- Included builds are isolated. The consumer/root build and `build-logic` each parse the same manifest once into separate build-local registries.
- Settings plugins must defer manifest loading until the settings extension has been configured; use `settings.gradle.settingsEvaluated { ... }` so isolated builds can set `repositoryRoot` before bootstrap runs.
- `durex.settings` owns known external project-plugin resolution. For a manifest plugin with `module`, use `useModule("<module>:<resolved-version>")`; otherwise use `useVersion(<resolved-version>)`.
- Module discovery supports `auto`, `manual`, and `strict-auto`; manual exclusion/override wins over automatic discovery.
- A manual `[[module]]` may specify `build-file = "build.spring.gradle"` so migration builds can point at alternate descriptors without changing physical module directories.
- Paths in `modules.toml` are resolved against `durexSettings.repositoryRoot`, not against the directory containing `modules.toml`.
- `durex.library("alias")` is the generic escape hatch for manifest-managed external libraries that are not represented by a feature plugin. Framework capabilities remain feature/module-type owned.
- `durex.jooq-schema` remains separate from runtime `durex.feature.jooq`.
- Java baseline for new Durex module types is 25. Existing business sources may remain Java-21-compatible; do not rewrite source merely to use Java 25 syntax.
- Keep the legacy root Gradle 7.4.2 / Quarkus build untouched in this plan. Root cutover is a follow-up plan after remaining legacy modules are migrated or removed.
- Preserve business `project(...)` dependencies explicitly.
- Use strict RED -> GREEN TDD and make one focused commit after every task.

---

## File Structure

```text
build-bootstrap/
  settings.gradle.kts
  build.gradle.kts
  src/main/groovy/com/github/durex/gradle/manifest/
    DependencyManifestLoader.groovy
    DependencyRegistry.groovy
    DependencyRegistryService.groovy
    VersionSpec.groovy
    PlatformSpec.groovy
    LibrarySpec.groovy
    PluginSpec.groovy
  src/main/groovy/com/github/durex/gradle/settings/
    DurexSettingsExtension.groovy
    DurexSettingsPlugin.groovy
    DurexBuildLogicSettingsExtension.groovy
    DurexBuildLogicSettingsPlugin.groovy
    DurexBuildLogicPlugin.groovy
    ProjectDiscovery.groovy
    ProjectRegistry.groovy
    ProjectSpec.groovy
  tests/
    manifest-valid/
    manifest-validation/
    plugin-resolution/
    modules-auto/
    modules-manual/
    modules-strict/
    modules-conflict/

gradle/dependencies/
  durex.toml
  spring.toml
  database.toml
  test.toml
  utils.toml

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

Functional fixtures are real Gradle builds invoked directly with Gradle 9.1. Bootstrap tests therefore do not need a second hard-coded unit-test framework dependency.

---

### Task 1: Durex manifest, registry service, and minimal settings bootstrap

**Files:**
- Create: `build-bootstrap/settings.gradle.kts`
- Create: `build-bootstrap/build.gradle.kts`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/DependencyManifestLoader.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/DependencyRegistry.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/DependencyRegistryService.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/VersionSpec.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/PlatformSpec.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/LibrarySpec.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/PluginSpec.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexSettingsExtension.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexSettingsPlugin.groovy`
- Create: `gradle/dependencies/durex.toml`
- Create: `gradle/dependencies/spring.toml`
- Create: `gradle/dependencies/database.toml`
- Create: `gradle/dependencies/test.toml`
- Create: `gradle/dependencies/utils.toml`
- Create: `build-bootstrap/tests/manifest-valid/settings.gradle`
- Create: `build-bootstrap/tests/manifest-valid/build.gradle`
- Create: `build-bootstrap/tests/manifest-validation/settings.gradle`
- Create: `build-bootstrap/tests/manifest-validation/build.gradle`
- Create: `build-bootstrap/tests/manifest-validation/valid.toml`
- Create: `build-bootstrap/tests/manifest-validation/cycle-a.toml`
- Create: `build-bootstrap/tests/manifest-validation/cycle-b.toml`
- Create: `build-bootstrap/tests/manifest-validation/duplicate-root.toml`
- Create: `build-bootstrap/tests/manifest-validation/duplicate-a.toml`
- Create: `build-bootstrap/tests/manifest-validation/duplicate-b.toml`
- Create: `build-bootstrap/tests/manifest-validation/unknown-ref.toml`
- Create: `build-bootstrap/tests/manifest-validation/version-conflict.toml`
- Create: `build-bootstrap/tests/manifest-validation/unsupported.toml`
- Create: `build-bootstrap/tests/plugin-resolution/settings.gradle`
- Create: `build-bootstrap/tests/plugin-resolution/build.gradle`

**Interfaces:**
- `DependencyRegistry DependencyManifestLoader.load(File rootManifest)`.
- `DependencyRegistry`: `int javaVersion()`, `VersionSpec version(String)`, `PlatformSpec platform(String)`, `LibrarySpec library(String)`, `PluginSpec plugin(String)`, `PluginSpec pluginByGradleId(String)`.
- `VersionSpec`: `String id`, `String value`.
- `PlatformSpec`: `String id`, `String module`, `String version`, `String coordinate()`.
- `LibrarySpec`: `String id`, `String module`, nullable `String version`, nullable `String platform`, `String notation()` where explicit-version libraries return `group:name:version` and platform-managed libraries return `group:name`.
- `PluginSpec`: `String alias`, `String id`, nullable `String module`, `String version`, `String coordinate()` when module is present.
- `DependencyRegistryService` is a Gradle `BuildService`; it lazily parses exactly once and delegates the registry methods above.
- `durexSettings`: mutable `File repositoryRoot`, `File dependencyManifest`, `File modulesManifest`, `boolean moduleDiscovery` with defaults derived from `repositoryRoot`.

- [ ] **Step 1: Write a RED fixture that applies `durex.settings` before it exists**

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
        def service = gradle.sharedServices.registrations
            .getByName('durexDependencyRegistry').service.get()
        assert service.javaVersion() == 25
        assert service.version('spring-boot').value == '4.1.0'
        assert service.platform('spring').coordinate() ==
            'org.springframework.boot:spring-boot-dependencies:4.1.0'
        assert service.library('spring-jooq').module ==
            'org.springframework.boot:spring-boot-starter-jooq'
    }
}
```

- [ ] **Step 2: Run RED**

```bash
gradle -p build-bootstrap/tests/manifest-valid verifyDurexManifest --stacktrace
```

Expected: FAIL because plugin `durex.settings` does not exist.

- [ ] **Step 3: Create bootstrap build; register only the plugin implemented in this task**

`build-bootstrap/build.gradle.kts`:

```kotlin
plugins {
    `groovy-gradle-plugin`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.tomlj:tomlj:1.1.1")
}

gradlePlugin {
    plugins {
        create("durexSettings") {
            id = "durex.settings"
            implementationClass = "com.github.durex.gradle.settings.DurexSettingsPlugin"
        }
    }
}
```

Do not register `durex.build-logic-settings` or `durex.build-logic` yet; their classes are created in Task 3.

- [ ] **Step 4: Create exact dependency-manifest baseline**

`gradle/dependencies/durex.toml`:

```toml
include = ["spring.toml", "database.toml", "test.toml", "utils.toml"]

[java]
version = 25
```

`spring.toml` must define:

```text
versions: spring-boot=4.1.0, graal-native=1.1.1
platform: spring -> org.springframework.boot:spring-boot-dependencies @ spring-boot
libraries: spring-core, spring-web, spring-validation, spring-observability,
           spring-test, spring-web-test, spring-jpa, spring-jdbc, spring-jooq,
           spring-redis, jackson-annotations, jackson-databind,
           jakarta-validation, h2
plugins: spring-boot, graalvm-native
```

Every Spring-managed library uses `platform = "spring"`; no Spring-managed library carries its own version.

`database.toml` must define `jooq = "3.21.5"`; `jooq-core`, `jooq-meta`, `jooq-codegen`, `jooq-meta-extensions`; and plugin alias `jooq-codegen`:

```toml
[plugins.jooq-codegen]
id = "org.jooq.jooq-codegen-gradle"
module = "org.jooq:jooq-codegen-gradle"
version.ref = "jooq"
```

`utils.toml` must define:

```text
lombok=1.18.46
jsonschema2pojo=1.3.3
json-schema-pojo=1.1.2
swagger-annotations=2.2.0
javax CDI=2.0.SP1
javax.inject=1
javax.transaction=1.3
javax.validation=2.0.1.Final
javax.interceptor=1.2.2
```

with aliases `lombok`, `json-schema-pojo`, `swagger-annotations`, `javax-cdi`, `javax-inject`, `javax-transaction`, `javax-validation`, `javax-interceptor`, plus:

```toml
[plugins.jsonschema2pojo]
id = "org.jsonschema2pojo"
version.ref = "jsonschema2pojo"
```

`test.toml` must define `junit = "5.8.2"`, `junit-jupiter`, and `junit-platform-launcher = "1.8.2"` for plain Java-library defaults.

- [ ] **Step 5: Implement strict parser and build service**

`DependencyManifestLoader.load(File)` must:

```text
canonicalize every included path
reject include cycles
reject missing includes
reject duplicate version/platform/library/plugin aliases
reject duplicate Gradle plugin ids
reject unknown version.ref
reject unknown platform
reject unsupported top-level keys/sections
reject malformed group:name module coordinates
require exactly one library version owner: version | version.ref | platform
reject platform + version/version.ref
resolve every version.ref before creating immutable specs
```

Errors must begin with `Durex dependency manifest error` and include source file + object id + problem.

`DependencyRegistryService` lazily calls `DependencyManifestLoader.load(parameters.manifestFile.get().asFile)` once and delegates registry accessors.

- [ ] **Step 6: Implement minimal `durex.settings` bootstrap and plugin resolution**

`DurexSettingsPlugin.apply(Settings)` must immediately create `durexSettings`, then defer bootstrap:

```groovy
settings.gradle.settingsEvaluated {
    File manifest = extension.dependencyManifest ?: new File(
        extension.repositoryRoot, 'gradle/dependencies/durex.toml')
    def service = settings.gradle.sharedServices.registerIfAbsent(
        'durexDependencyRegistry', DependencyRegistryService) {
        parameters.manifestFile.set(manifest)
    }
    def registry = service.get()
    settings.pluginManagement.resolutionStrategy.eachPlugin {
        def spec = registry.pluginByGradleId(requested.id.id)
        if (spec != null) {
            if (requested.version != null && requested.version != spec.version) {
                throw new GradleException(
                    "Durex plugin version conflict: ${requested.id.id} requested ${requested.version}, managed ${spec.version}")
            }
            if (spec.module != null) {
                useModule(spec.coordinate())
            } else {
                useVersion(spec.version)
            }
        }
    }
}
```

When `moduleDiscovery=false`, Task 1 stops after registry/plugin bootstrap.

- [ ] **Step 7: Add manifest validation matrix**

`build-bootstrap/tests/manifest-validation/settings.gradle` reads `-Pmanifest=<filename>`, applies `durex.settings`, sets `repositoryRoot = projectDir`, `dependencyManifest = file(providers.gradleProperty('manifest').get())`, and `moduleDiscovery=false`.

Run each case:

```bash
gradle -p build-bootstrap/tests/manifest-validation help -Pmanifest=cycle-a.toml --stacktrace
gradle -p build-bootstrap/tests/manifest-validation help -Pmanifest=duplicate-root.toml --stacktrace
gradle -p build-bootstrap/tests/manifest-validation help -Pmanifest=unknown-ref.toml --stacktrace
gradle -p build-bootstrap/tests/manifest-validation help -Pmanifest=version-conflict.toml --stacktrace
gradle -p build-bootstrap/tests/manifest-validation help -Pmanifest=unsupported.toml --stacktrace
```

Expected: each FAILS for its named Durex manifest error; no parser NPE/opaque Gradle error is accepted.

- [ ] **Step 8: Prove direct external plugin resolution from TOML**

`plugin-resolution/settings.gradle` applies `durex.settings` with `repositoryRoot=../../..`, `moduleDiscovery=false`; `plugin-resolution/build.gradle` contains:

```groovy
plugins {
    id 'org.jsonschema2pojo'
}

tasks.register('verifyPluginResolution') {
    doLast {
        assert plugins.hasPlugin('org.jsonschema2pojo')
    }
}
```

Run:

```bash
gradle -p build-bootstrap/tests/plugin-resolution verifyPluginResolution --stacktrace
```

Expected: PASS without any plugin version in the fixture.

- [ ] **Step 9: Add bootstrap dependency diagnostic**

`durex.settings` registers root task `durexDependencies` after the root project exists. Stable output includes:

```text
Manifest: <canonical path>/gradle/dependencies/durex.toml
Java: 25
Platform: spring=org.springframework.boot:spring-boot-dependencies:4.1.0
Plugin: org.springframework.boot=4.1.0
Plugin: org.graalvm.buildtools.native=1.1.1
Plugin: org.jooq.jooq-codegen-gradle=3.21.5
```

- [ ] **Step 10: Run GREEN and commit**

```bash
gradle -p build-bootstrap/tests/manifest-valid verifyDurexManifest durexDependencies --stacktrace
gradle -p build-bootstrap/tests/plugin-resolution verifyPluginResolution --stacktrace
```

Expected: PASS.

```bash
git add -- build-bootstrap gradle/dependencies
git commit -m "build: add Durex dependency manifest bootstrap"
```

---

### Task 2: Automatic/manual module discovery and project registry

**Files:**
- Modify: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexSettingsPlugin.groovy`
- Modify: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexSettingsExtension.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/ProjectDiscovery.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/ProjectRegistry.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/ProjectSpec.groovy`
- Create: `build-bootstrap/tests/modules-auto/settings.gradle`
- Create: `build-bootstrap/tests/modules-auto/modules.toml`
- Create: `build-bootstrap/tests/modules-auto/core/music/build.gradle`
- Create: `build-bootstrap/tests/modules-auto/core/shared/utils/build.gradle`
- Create: `build-bootstrap/tests/modules-auto/core/shared/jakarta/common/build.gradle`
- Create: `build-bootstrap/tests/modules-auto/core/schema/music/repo/build.gradle`
- Create: `build-bootstrap/tests/modules-auto/core/ignored/build.gradle`
- Create: `build-bootstrap/tests/modules-manual/settings.gradle`
- Create: `build-bootstrap/tests/modules-manual/modules.toml`
- Create: `build-bootstrap/tests/modules-manual/core/music/build.gradle`
- Create: `build-bootstrap/tests/modules-manual/core/music/build.spring.gradle`
- Create: `build-bootstrap/tests/modules-manual/tools/admin/build.gradle`
- Create: `build-bootstrap/tests/modules-strict/settings.gradle`
- Create: `build-bootstrap/tests/modules-strict/modules.toml`
- Create: `build-bootstrap/tests/modules-strict/core/deep/unmapped/module/build.gradle`
- Create: `build-bootstrap/tests/modules-conflict/settings.gradle`
- Create: `build-bootstrap/tests/modules-conflict/modules.toml`
- Create: `build-bootstrap/tests/modules-conflict/core/a/build.gradle`
- Create: `build-bootstrap/tests/modules-conflict/core/b/build.gradle`

**Interfaces:**
- `ProjectSpec`: `String gradlePath`, canonical `File directory`, `String source` (`AUTO`/`MANUAL`), `String buildFile`.
- `ProjectRegistry`: immutable lookup by Gradle path and canonical directory.
- `ProjectDiscovery.discover(File repositoryRoot, File modulesManifest)` returns one normalized `ProjectRegistry`.
- `durexSettings.modulesManifest` defaults to `${repositoryRoot}/gradle/modules.toml`; explicit isolated builds may point to another file.

- [ ] **Step 1: Write RED discovery fixtures**

`modules-auto/modules.toml`:

```toml
[discovery]
mode = "auto"
roots = ["core"]
exclude = ["core/ignored"]

[[module]]
name = "shared-common"
path = "core/shared/jakarta/common"
```

Expected names:

```text
core/music                  -> :music
core/shared/utils           -> :shared-utils
core/shared/jakarta/common  -> :shared-common (manual override)
core/schema/music/repo      -> :music-repo
core/ignored                -> excluded
```

`modules-manual/modules.toml`:

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

- [ ] **Step 2: Run RED**

```bash
gradle -p build-bootstrap/tests/modules-auto projects --stacktrace
gradle -p build-bootstrap/tests/modules-manual projects --stacktrace
```

Expected: projects are not registered yet.

- [ ] **Step 3: Implement deterministic naming and precedence**

Automatic naming rules, in order:

```text
core/<name>                 -> :<name>
core/shared/<name>          -> :shared-<name>
core/schema/<domain>/<kind> -> :<domain>-<kind>
auto fallback               -> ':' + path-under-discovery-root segments joined with '-'
```

Apply precedence:

```text
manual exclude > manual include/override > automatic discovery > naming fallback
```

A manual declaration for an automatically discovered physical path replaces the inferred logical name; it does not create a second project.

- [ ] **Step 4: Implement discovery modes and build-file override**

```text
auto        -> scan roots + fallback naming + manual declarations
manual      -> no scan; only [[module]]
strict-auto -> scan roots, but only the three explicit naming patterns are legal;
               a path requiring fallback naming is a Durex error
```

For each final `ProjectSpec`:

```groovy
settings.include(spec.gradlePath)
def descriptor = settings.project(spec.gradlePath)
descriptor.projectDir = spec.directory
if (spec.buildFile != null) {
    descriptor.buildFileName = spec.buildFile
}
```

Reject duplicate Gradle paths and one canonical directory mapped to multiple logical projects.

- [ ] **Step 5: Add `durexProjects` diagnostic**

Stable output is sorted by Gradle path:

```text
:music | core/music | auto | build.gradle
:music-repo | core/schema/music/repo | auto | build.gradle
:shared-common | core/shared/jakarta/common | manual | build.gradle
:shared-utils | core/shared/utils | auto | build.gradle
```

Manual fixture must print `:music | core/music | manual | build.spring.gradle`.

- [ ] **Step 6: Verify auto/manual/override/exclude**

```bash
gradle -p build-bootstrap/tests/modules-auto projects durexProjects --stacktrace
gradle -p build-bootstrap/tests/modules-manual projects durexProjects --stacktrace
```

Expected: PASS with exactly the names/build files above and no `:ignored`.

- [ ] **Step 7: Verify strict and duplicate failures**

`modules-strict` uses `mode="strict-auto"`, root `core`, and only `core/deep/unmapped/module/build.gradle`; expected error names that path as not matching a strict naming convention.

`modules-conflict/modules.toml` manually maps both physical projects to `name="same"`; expected duplicate logical-project error.

```bash
gradle -p build-bootstrap/tests/modules-strict projects --stacktrace
gradle -p build-bootstrap/tests/modules-conflict projects --stacktrace
```

Expected: both FAIL for their specific Durex errors.

- [ ] **Step 8: Commit**

```bash
git add -- build-bootstrap
git commit -m "build: add Durex module discovery"
```

---

### Task 3: Bootstrap the isolated `build-logic` build from the same TOML

**Files:**
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexBuildLogicSettingsExtension.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexBuildLogicSettingsPlugin.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexBuildLogicPlugin.groovy`
- Modify: `build-bootstrap/build.gradle.kts`
- Modify: `build-logic/settings.gradle.kts`
- Modify: `build-logic/build.gradle.kts`

**Interfaces:**
- `durexBuildLogicSettings.repositoryRoot` defaults to the parent of `build-logic` but is explicitly configured to `file("..")`.
- `durex.build-logic-settings` registers its own build-local `durexDependencyRegistry` using the same root manifest.
- `durex.build-logic` runs in the `build-logic` project and adds manifest-backed external Gradle plugin implementation modules.

- [ ] **Step 1: Change `build-logic` settings/build to RED target form**

`build-logic/settings.gradle.kts`:

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

`build-logic/build.gradle.kts`:

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

- [ ] **Step 2: Run RED**

```bash
gradle -p build-logic tasks --stacktrace
```

Expected: unknown `durex.build-logic-settings`/`durex.build-logic` plugins.

- [ ] **Step 3: Implement and only now register the two bootstrap plugins**

Extend `build-bootstrap/build.gradle.kts`:

```kotlin
gradlePlugin {
    plugins {
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

`DurexBuildLogicSettingsPlugin` uses the same deferred settings lifecycle as `durex.settings`, but performs no project discovery.

- [ ] **Step 4: Add build-logic implementation dependencies from its local registry**

`DurexBuildLogicPlugin` reads `durexDependencyRegistry` and adds exactly:

```text
plugins.spring-boot.coordinate()
plugins.graalvm-native.coordinate()
plugins.jooq-codegen.coordinate()
libraries.jooq-core.notation()
libraries.jooq-meta.notation()
```

to `implementation`. No Spring/jOOQ/GraalVM version string appears in `build-logic/build.gradle.kts`.

- [ ] **Step 5: Remove the last build-logic Version Catalog**

`build-logic/settings.gradle.kts` contains no `versionCatalogs`; `build-logic/build.gradle.kts` contains no `dbLibs`.

- [ ] **Step 6: Verify build isolation and existing schema plugin**

```bash
gradle -p build-logic tasks --stacktrace
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/QMusic.java
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/records/RMusic.java
```

Expected: PASS. This proves `build-logic` independently bootstraps from the same TOML source.

- [ ] **Step 7: Commit**

```bash
git add -- build-bootstrap build-logic
git commit -m "build: bootstrap Durex build logic from manifest"
```

---

### Task 4: Module types, dependency access, and generic library aliases

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
- Modify: `build-logic/build.gradle.kts`
- Create: `build-logic/tests/java-library-smoke/settings.gradle`
- Create: `build-logic/tests/java-library-smoke/build.gradle`
- Create: `build-logic/tests/java-library-smoke/src/test/java/example/SmokeTest.java`
- Create: `build-logic/tests/spring-service-smoke/settings.gradle`
- Create: `build-logic/tests/spring-service-smoke/build.gradle`
- Create: `build-logic/tests/module-conflict/settings.gradle`
- Create: `build-logic/tests/module-conflict/build.gradle`

**Interfaces:**
- Binary plugin id `durex.module` owns the extension/model shared by precompiled module/feature plugins.
- `ModuleKind`: `JAVA_LIBRARY`, `SPRING_LIBRARY`, `SPRING_SERVICE`.
- `DurexModuleState.claim(ModuleKind)` allows the same kind repeatedly and rejects a different second kind.
- Build-logic runtime accesses `durexDependencyRegistry` dynamically through Gradle shared-service registration; do not cast to bootstrap classes across included-build classloaders.
- `DurexDependencyAccess.add(Project, String configuration, String alias)` inserts the required platform once per `(configuration, platform)` then adds the library.
- `DurexExtension.library(String alias)` returns standard Gradle dependency notation. Explicit-version aliases return `group:name:version`; platform-managed aliases return `group:name` and require that platform to be active in `DurexModuleState`.

- [ ] **Step 1: Create RED module-type fixtures**

Spring service fixture:

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

Conflict fixture:

```groovy
plugins {
    id 'durex.spring-service'
    id 'durex.java-library'
}
```

- [ ] **Step 2: Run RED**

```bash
gradle -p build-logic/tests/java-library-smoke test --stacktrace
gradle -p build-logic/tests/spring-service-smoke verifyDurexService --stacktrace
gradle -p build-logic/tests/module-conflict help --stacktrace
```

Expected: missing Durex module-type plugins.

- [ ] **Step 3: Register `durex.module` and implement shared state/access**

Add to `build-logic/build.gradle.kts`:

```kotlin
gradlePlugin {
    plugins {
        create("durexModule") {
            id = "durex.module"
            implementationClass = "com.github.durex.gradle.DurexModulePlugin"
        }
    }
}
```

`DurexModulePlugin` creates one `DurexModuleState`, one `DurexExtension`, and nested `PersistenceExtension`; state tracks module kind, active platforms, active features, and native flag.

- [ ] **Step 4: Implement module-type composition without false conflicts**

Exact composition:

```text
durex.java-base
  -> java + durex.module + Java25/compiler conventions; does NOT claim a module kind

durex.java-library
  -> java-library + durex.java-base; claims JAVA_LIBRARY; adds plain JUnit

durex.spring-base
  -> durex.java-base; activates Spring platform; does NOT claim a module kind

durex.spring-library
  -> java-library + durex.spring-base; claims SPRING_LIBRARY; adds spring-core + spring-test

durex.spring-service
  -> org.springframework.boot + durex.spring-base; claims SPRING_SERVICE;
     adds spring-core + spring-web + spring-validation + spring-observability +
     spring-test + spring-web-test
```

Do not implement `spring-service` by applying Durex `java-library`, because that would claim the wrong kind before `SPRING_SERVICE`.

- [ ] **Step 5: Implement Java/test conventions**

`durex.java-base`:

```text
Java toolchain 25
JavaCompile options.release=25
UTF-8
```

`durex.java-library` adds `junit-jupiter` to `testImplementation`, `junit-platform-launcher` to `testRuntimeOnly`, and `useJUnitPlatform()`.

Spring module types rely on Spring Boot test starter instead of forcing the older plain-Java JUnit version into the Spring graph.

- [ ] **Step 6: Implement generic `durex.library(alias)`**

Usage contract:

```groovy
dependencies {
    implementation durex.library('json-schema-pojo')
    compileOnly durex.library('javax-cdi')
}
```

Unknown aliases fail with `Unknown Durex library '<alias>'`. A platform-managed alias fails if its platform has not been activated by the current module type/feature.

- [ ] **Step 7: Verify GREEN and module conflict**

```bash
gradle -p build-logic/tests/java-library-smoke test --stacktrace
gradle -p build-logic/tests/spring-service-smoke verifyDurexService dependencies --configuration compileClasspath --stacktrace
gradle -p build-logic/tests/module-conflict help --stacktrace
```

Expected: first two PASS; conflict fixture FAILS and names `SPRING_SERVICE` and `JAVA_LIBRARY`.

- [ ] **Step 8: Commit**

```bash
git add -- build-logic
git commit -m "feat: add Durex module type plugins"
```

---

### Task 5: Feature DSL, prerequisites, and capabilities diagnostic

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
- Create: `build-logic/tests/features-smoke/settings.gradle`
- Create: `build-logic/tests/features-smoke/build.gradle`
- Create: `build-logic/tests/feature-invalid/settings.gradle`
- Create: `build-logic/tests/feature-invalid/build.gradle`

**Interfaces:**
- `persistence.jpa()` -> `durex.feature.jpa`.
- `persistence.jdbc()` -> `durex.feature.jdbc`.
- `persistence.jooq()` -> `durex.feature.jooq`.
- `redis()` -> `durex.feature.redis`.
- `nativeImage()` -> `durex.feature.native`.
- `lombok()` -> `durex.feature.lombok`.
- Feature activation is idempotent; diagnostic feature names are sorted.

- [ ] **Step 1: Write RED coexistence and prerequisite fixtures**

Coexistence:

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

Invalid native:

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

Expected: DSL methods are missing.

- [ ] **Step 3: Implement feature dependency mappings and prerequisites**

```text
jpa    -> implementation spring-jpa
jdbc   -> implementation spring-jdbc
jooq   -> implementation spring-jooq
redis  -> implementation spring-redis
lombok -> compileOnly + annotationProcessor lombok
native -> apply org.graalvm.buildtools.native
```

JPA/JDBC/jOOQ/Redis require `SPRING_LIBRARY` or `SPRING_SERVICE`; Native requires `SPRING_SERVICE`; Lombok is valid for all three module types.

- [ ] **Step 4: Add `durexCapabilities`**

For coexistence fixture exact stable fields include:

```text
Type: SPRING_SERVICE
Java: 25
Platforms: spring
Features: jpa,jooq,redis
Native: disabled
```

- [ ] **Step 5: Verify GREEN and error contract**

```bash
gradle -p build-logic/tests/features-smoke durexCapabilities dependencies --configuration compileClasspath --stacktrace
gradle -p build-logic/tests/feature-invalid help --stacktrace
```

Expected: coexistence PASS; invalid fixture FAILS with `durex.feature.native requires durex.spring-service`.

- [ ] **Step 6: Commit**

```bash
git add -- build-logic
git commit -m "feat: add Durex dependency feature DSL"
```

---

### Task 6: Migrate isolated Spring capability/native references

**Files:**
- Modify: `reference/spring-capabilities/settings.gradle.kts`
- Modify: `reference/spring-capabilities/build.gradle.kts`
- Modify: `reference/spring-native/settings.gradle.kts`
- Modify: `reference/spring-native/build.gradle.kts`
- Modify: `reference/spring-native/README.md`

**Interfaces:**
- Both isolated builds include `../../build-bootstrap` and `../../build-logic` in `pluginManagement`.
- Both apply `durex.settings` and set `repositoryRoot=file("../..")`, `moduleDiscovery=false`.
- Neither reference build registers a Version Catalog.

- [ ] **Step 1: Convert only reference settings and observe RED**

Target settings shape:

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

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

Run `gradle -p reference/spring-native tasks --stacktrace`; expected RED because the old build file still references `sLibs`.

- [ ] **Step 2: Convert capability reference**

`reference/spring-capabilities/build.gradle.kts` uses only `durex.spring-service` and:

```groovy
durex {
    persistence {
        jpa()
        jdbc()
        jooq()
    }
    redis()
}
```

No `apply(from=...)`, alias, or `VersionCatalogsExtension` remains.

- [ ] **Step 3: Convert native reference**

Apply `durex.spring-service`; activate:

```groovy
durex {
    nativeImage()
}
```

Keep application code, artifact name, and `/hello` behavior unchanged.

- [ ] **Step 4: Verify JVM, AOT, native**

```bash
gradle -p reference/spring-capabilities dependencies --configuration compileClasspath --stacktrace
gradle -p reference/spring-native test processAot bootJar --stacktrace
gradle -p reference/spring-native nativeTest --stacktrace
gradle -p reference/spring-native nativeCompile --stacktrace
```

Expected: all PASS.

- [ ] **Step 5: Update README and commit**

README replaces old ownership references with `gradle/dependencies/*.toml`, `durex.spring-service`, and `nativeImage()`.

```bash
git add -- reference/spring-capabilities reference/spring-native
git commit -m "refactor: use Durex build platform in Spring references"
```

---

### Task 7: Migrate jOOQ schema and supporting music modules off catalogs

**Files:**
- Modify: `build-logic/src/main/groovy/durex.jooq-schema.gradle`
- Modify: `build-logic/tests/jooq-schema-smoke/settings.gradle.kts`
- Modify: `core/shared/utils/build.spring.gradle`
- Modify: `core/shared/jakarta/common/build.spring.gradle`
- Modify: `core/shared/json-schema-annotation/build.spring.gradle`
- Modify: `core/schema/music/json/build.spring.gradle`
- Modify: `core/schema/music/entity/build.spring.gradle`
- Modify: `core/schema/music/repo/build.spring.gradle`

**Interfaces:**
- `durex.jooq-schema` keeps official jOOQ codegen, DDLDatabase, Q/R naming, generated source directory, and `compileJava -> jooqCodegen` behavior.
- `durex.jooq-schema` also adds `jooq-core` to the generated-code module because generated Q/R types require jOOQ API.
- `org.jsonschema2pojo` is resolved by consumer `durex.settings` from the TOML plugin entry; it is not added to `build-logic` compile-time implementation dependencies.

- [ ] **Step 1: Remove catalog aliases from one supporting descriptor to establish RED**

Convert `music-entity` plugin declarations to Durex form but initially remove `dbLibs/gLibs` dependencies without replacing them.

```bash
gradle -p migration/spring-music :music-entity:compileJava --stacktrace
```

Expected: RED on missing jOOQ/Lombok dependency wiring, not settings/project mapping.

- [ ] **Step 2: Convert module types**

```text
shared-utils            -> durex.java-library
shared-common           -> durex.java-library + lombok()
json-schema-annotation  -> durex.java-library + lombok()
music-json              -> durex.spring-library + org.jsonschema2pojo
music-entity            -> durex.java-library + durex.jooq-schema + lombok()
music-repo              -> durex.spring-library + persistence.jooq() + lombok()
```

Retain existing source-set include/exclude rules.

- [ ] **Step 3: Replace uncommon external aliases with `durex.library`**

Examples:

```groovy
dependencies {
    implementation durex.library('json-schema-pojo')
    implementation durex.library('swagger-annotations')
    compileOnly durex.library('javax-cdi')
    compileOnly durex.library('javax-inject')
    compileOnly durex.library('javax-validation')
}
```

`music-json`:

```groovy
dependencies {
    api durex.library('jackson-annotations')
    api durex.library('jackson-databind')
    api durex.library('jakarta-validation')
}
```

- [ ] **Step 4: Keep jOOQ generation contract green**

```bash
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/QMusic.java
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/records/RMusic.java
```

Expected: PASS.

- [ ] **Step 5: Compile support graph**

```bash
gradle -p migration/spring-music :music-json:compileJava :music-entity:compileJava :music-repo:compileJava --stacktrace
```

Expected: PASS and no Version Catalog accessor use in these Spring descriptors.

- [ ] **Step 6: Commit**

```bash
git add -- build-logic core/shared core/schema/music
git commit -m "refactor: migrate music support modules to Durex plugins"
```

---

### Task 8: Run the real `music` migration on Durex manual discovery

**Files:**
- Modify: `migration/spring-music/settings.gradle`
- Modify: `migration/spring-music/build.gradle`
- Create: `migration/spring-music/modules.toml`
- Modify: `core/music/build.spring.gradle`

**Interfaces:**
- Migration settings applies `durex.settings`, sets `repositoryRoot=file('../..')`, and sets `modulesManifest=file('modules.toml')`.
- `modules.toml` uses `mode="manual"`; every project points at its existing physical directory and `build-file="build.spring.gradle"`.
- `core/music/build.spring.gradle` becomes `durex.spring-service` + `persistence.jooq()` + `lombok()` while retaining only business project dependencies, migration source-set bridge, and temporary legacy javax API dependencies.

- [ ] **Step 1: Create manual seven-project manifest**

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

- [ ] **Step 2: Convert migration settings before module build file**

Target responsibilities:

```text
pluginManagement: include ../../build-bootstrap and ../../build-logic
plugins: durex.settings
durexSettings.repositoryRoot: ../..
durexSettings.modulesManifest: migration/spring-music/modules.toml
dependencyResolutionManagement: mavenCentral only
```

Delete all Version Catalog registration and all `springProject(...)` calls.

```bash
gradle -p migration/spring-music projects durexProjects --stacktrace
```

Expected: exactly the same seven logical projects as before, all source=`manual`, all build file=`build.spring.gradle`.

- [ ] **Step 3: Convert `core/music/build.spring.gradle`**

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

Keep current `src/main/java` + `src/spring/java` source-set rules and exclusions. Keep business `project(...)` dependencies. Replace bridge coordinates with:

```groovy
compileOnly durex.library('javax-cdi')
compileOnly durex.library('javax-inject')
compileOnly durex.library('javax-transaction')
testRuntimeOnly durex.library('h2')
```

- [ ] **Step 4: Remove migration-root Java conventions now owned by module types**

`migration/spring-music/build.gradle` retains group/version policy only. Remove root `java-library` application, Java 21 source/target, release, and encoding conventions.

- [ ] **Step 5: Verify existing real runtime end to end**

```bash
gradle -p migration/spring-music :music:compileJava --stacktrace
gradle -p migration/spring-music :music:test --stacktrace
```

Expected: all existing CRUD MockMvc tests and the real H2 -> Boot DataSource -> jOOQ `DSLContext` -> repository -> service -> HTTP integration test remain green.

- [ ] **Step 6: Assert migrated Spring path is catalog-free**

```bash
grep -R -n -E 'VersionCatalogsExtension|[a-z]Libs\.' \
  migration/spring-music \
  core/music/build.spring.gradle \
  core/schema/music/*/build.spring.gradle \
  core/shared/*/build.spring.gradle \
  core/shared/*/*/build.spring.gradle || true
```

Expected: no hits from migrated descriptors/settings.

- [ ] **Step 7: Commit**

```bash
git add -- migration/spring-music core/music/build.spring.gradle
git commit -m "refactor: run music migration on Durex build platform"
```

---

### Task 9: CI verification for platform, references, and real music migration

**Files:**
- Create: `.github/workflows/durex-build-platform.yml`
- Modify: `.github/workflows/spring-native-reference.yml`
- Modify: `.github/workflows/spring-music.yml`

**Interfaces:**
- `Durex Build Platform` validates bootstrap/discovery/module/feature fixtures independently of business code.
- Existing Spring Native and Spring Music workflows remain end-to-end evidence and trigger on `build-bootstrap/**`, `build-logic/**`, and `gradle/dependencies/**`.

- [ ] **Step 1: Add positive build-platform CI**

Use Java 25 + Gradle 9.1. Positive commands:

```bash
gradle -p build-bootstrap/tests/manifest-valid verifyDurexManifest durexDependencies --stacktrace
gradle -p build-bootstrap/tests/plugin-resolution verifyPluginResolution --stacktrace
gradle -p build-bootstrap/tests/modules-auto projects durexProjects --stacktrace
gradle -p build-bootstrap/tests/modules-manual projects durexProjects --stacktrace
gradle -p build-logic/tests/java-library-smoke test --stacktrace
gradle -p build-logic/tests/spring-service-smoke verifyDurexService --stacktrace
gradle -p build-logic/tests/features-smoke durexCapabilities --stacktrace
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace
```

- [ ] **Step 2: Add expected-failure assertions**

For manifest-cycle, duplicate, unknown-ref, version-conflict, unsupported-key, strict-auto, duplicate-project, module-type-conflict, and invalid-native fixtures, shell must:

```bash
if gradle ... > build/expected-failure.log 2>&1; then
  cat build/expected-failure.log
  exit 1
fi
grep -F '<exact expected Durex error fragment>' build/expected-failure.log
```

Do not use an unchecked `continue-on-error`.

- [ ] **Step 3: Update Spring workflow path filters**

Both Spring workflows include:

```text
build-bootstrap/**
build-logic/**
gradle/dependencies/**
```

Remove `gradle/library/spring-*.gradle` / migrated version-catalog paths only after their corresponding reference/migration build no longer consumes them.

- [ ] **Step 4: Preserve end-to-end Spring verification**

Spring Music workflow still runs:

```bash
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace
gradle -p migration/spring-music projects --stacktrace
gradle -p migration/spring-music :music:compileJava --stacktrace
gradle -p migration/spring-music :music:test --stacktrace
```

Spring Native workflow still runs JVM tests, AOT HTTP smoke, `nativeTest`, `nativeCompile`, and native `/hello` smoke.

- [ ] **Step 5: Verify branch checks**

Required green evidence:

```text
Durex Build Platform    green
Spring Music Migration  green
Spring Native Reference JVM/AOT/native green
```

Do not claim repository-wide CI green while unrelated legacy Quarkus/Shared Utils workflows remain red.

- [ ] **Step 6: Verify scope before completion**

```bash
gradle -p migration/spring-music :music:test --stacktrace
gradle -p reference/spring-native test processAot --stacktrace
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace
```

Inspect `git diff master...HEAD`. This plan must not modify root `settings.gradle`, root Gradle wrapper, or legacy Quarkus module descriptors.

- [ ] **Step 7: Commit**

```bash
git add -- .github/workflows
git commit -m "ci: verify Durex build platform"
```

---

## Follow-up Plan Boundary

This plan deliberately stops after design Phase 3: build-platform core + isolated references + real `music` migration. A separate plan is required for Phase 4/5 after remaining legacy modules are inventoried and migrated/removed. That follow-up owns:

```text
remaining module conversion
root Gradle 9.1 promotion
root durex.settings activation
removal of root Version Catalog registration
removal of deprecated gradle/library/spring-*.gradle
removal of old gradle/versions/*.toml after every consumer is gone
replacement/removal of legacy gradle/extensions/modules.gradle
```

Do not perform those cutover operations opportunistically while executing this plan.
