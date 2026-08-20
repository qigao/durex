# Durex Plugin Core v2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace duplicated Durex Gradle feature behavior with a typed capability kernel, configuration-aware dependency bridge, neutral bootstrap snapshot boundary, cache-safe diagnostics, and `durexDoctor` while preserving existing user DSL and real Spring/jOOQ behavior.

**Architecture:** `build-bootstrap` remains the ecosystem-neutral manifest/settings layer and exposes only a versioned JDK-container snapshot across the included-build boundary. `build-logic` reconstructs its own immutable dependency catalog, owns a managed `DurexModuleModel`, routes all dependency insertion through `DependencyBridge`, and routes all feature activation through `CapabilityEngine`; existing convention scripts stay as thin composition layers during v2.

**Tech Stack:** Gradle 9.1, Java 25, Groovy/Java binary Gradle plugins, Gradle managed `Property`/`SetProperty`/`ListProperty`, configuration cache, parallel execution, Spring Boot 4.1, GraalVM 25, jOOQ 3.21.5.

**Spec:** `docs/superpowers/specs/2026-08-20-durex-plugin-core-v2-design.md`

## Global Constraints

- TOML remains dependency/plugin data only; do not add capability behavior to TOML.
- Preserve first-party DSL: `persistence.jpa()`, `persistence.jdbc()`, `persistence.jooq()`, `redis()`, `nativeImage()`, `lombok()`.
- Add generic `durex.capability(pluginId)` without making the central extension a feature registry.
- `durex.library(alias)` is valid only for libraries owning `version` or `version.ref`; platform-managed aliases must use configuration-aware `durex.dependency(configuration, alias)` / `DependencyBridge.add(...)`.
- Platform state is configuration-scoped: `implementation:spring` and `api:spring` are different bindings.
- All built-in feature execution converges on one `CapabilityEngine` path.
- Module kind, capabilities, and platform bindings have one source of truth in a managed `DurexModuleModel`; remove duplicate `nativeEnabled` state.
- Cross-build access must use a neutral deeply immutable snapshot containing only JDK container/scalar values; do not cast bootstrap model classes in build-logic and do not use Groovy dynamic calls such as `service.library(...)` across the boundary.
- Snapshot schema version is exactly `1` in v2; mismatch is a hard Durex bootstrap error showing expected and actual versions.
- `durex.settings` / bootstrap diagnostics must not hard-code `spring`, `spring-boot`, `graalvm-native`, `jooq-codegen`, or any capability.
- Diagnostic output is deterministic and sorted; output order is part of the test contract.
- Diagnostic task actions must read only declared task inputs; they must not capture/read live `Project`, extension, BuildService, capability engine, or mutable registry objects at execution time.
- Gradle 9.1 configuration-cache reuse and representative `--parallel` execution are hard acceptance requirements.
- Keep existing convention scripts as thin wrappers in v2; do not rewrite every plugin to a binary plugin in this plan.
- Do not add Kafka/Mongo/Security/Flyway/OpenAPI/Testcontainers features, do not perform root Gradle cutover, and do not publish to the Gradle Plugin Portal in this plan.
- Preserve Spring Music, Spring Native, and jOOQ schema/codegen behavior.

---

## File Structure

New/changed kernel responsibilities:

```text
build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/
  DependencyRegistry.groovy             # existing manifest model; gains neutral snapshot export
  DependencyRegistryService.groovy      # BuildService; gains snapshot() only-neutral ABI

build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/
  DurexSettingsPlugin.groovy            # generic bootstrap + cache-safe diagnostic task wiring
  DurexDependenciesTask.groovy          # binary generic manifest report
  DurexProjectsTask.groovy              # binary project discovery report

build-logic/src/main/groovy/com/github/durex/gradle/catalog/
  DependencyCatalogSnapshot.groovy      # local immutable build-logic model
  CatalogPlatform.groovy
  CatalogLibrary.groovy
  CatalogPlugin.groovy
  DurexRegistryBridge.groovy            # reflective neutral-snapshot ABI bridge
  DurexCatalogPlugin.groovy             # internal binary plugin installing local catalog per project

build-logic/src/main/groovy/com/github/durex/gradle/model/
  DurexModuleModel.groovy               # managed source of module facts

build-logic/src/main/groovy/com/github/durex/gradle/dependency/
  DependencyBridge.groovy               # only path for configuration-aware dependency insertion

build-logic/src/main/groovy/com/github/durex/gradle/capability/
  CapabilitySpec.groovy
  DependencyBinding.groovy
  CapabilityRegistry.groovy
  CapabilityPluginRegistry.groovy
  CapabilityEngine.groovy
  BuiltinCapabilities.groovy
  DurexCapabilitySupport.groovy         # helper used by thin feature plugins

build-logic/src/main/groovy/com/github/durex/gradle/diagnostics/
  DurexCapabilitiesTask.groovy
  DurexDoctorTask.groovy
  DurexDoctorValidator.groovy

build-logic/src/main/groovy/com/github/durex/gradle/
  DurexConfigurationException.groovy
  DurexModulePlugin.groovy
  DurexExtension.groovy
  PersistenceExtension.groovy

existing convention scripts
  durex.java-base.gradle
  durex.java-library.gradle
  durex.spring-base.gradle
  durex.spring-library.gradle
  durex.spring-service.gradle
  durex.feature.*.gradle
  durex.jooq-schema.gradle
```

Functional fixtures stay under `build-bootstrap/tests/**` and `build-logic/tests/**`; real regressions remain in `migration/spring-music` and `reference/spring-*`.

---

### Task 1: Neutral dependency snapshot and build-logic registry bridge

**Files:**
- Modify: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/DependencyRegistry.groovy`
- Modify: `build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest/DependencyRegistryService.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/catalog/CatalogPlatform.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/catalog/CatalogLibrary.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/catalog/CatalogPlugin.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/catalog/DependencyCatalogSnapshot.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/catalog/DurexRegistryBridge.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/catalog/DurexCatalogPlugin.groovy`
- Modify: `build-logic/build.gradle.kts`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexDependencyAccess.groovy`
- Modify: `build-logic/src/main/groovy/durex.jooq-schema.gradle`
- Test: `build-logic/tests/registry-bridge-smoke/**`
- Test: `build-logic/tests/registry-schema-mismatch/**`

**Interfaces:**
- Produces: `Map<String, Object> DependencyRegistry.snapshot()` and `DependencyRegistryService.snapshot()`.
- Produces: `DependencyCatalogSnapshot DurexRegistryBridge.fromSnapshot(Map raw)`.
- Produces: `DependencyCatalogSnapshot DurexRegistryBridge.fromProject(Project project)` using neutral reflection only.
- Produces: internal plugin id `durex.catalog`; applying it installs one local `DependencyCatalogSnapshot` project extension named `durexDependencyCatalog`.
- Later tasks consume: `CatalogLibrary`, `CatalogPlatform`, `CatalogPlugin`, `DependencyCatalogSnapshot`.

- [ ] **Step 1: Add a RED registry bridge fixture**

Create `build-logic/tests/registry-bridge-smoke/settings.gradle`:

```groovy
pluginManagement {
    includeBuild('../../../build-bootstrap')
    includeBuild('../..')
    repositories { gradlePluginPortal(); mavenCentral() }
}
plugins { id 'durex.settings' }

durexSettings {
    repositoryRoot.set(file('../../..'))
    moduleDiscovery.set(false)
}

rootProject.name = 'registry-bridge-smoke'
```

Create `build-logic/tests/registry-bridge-smoke/build.gradle`:

```groovy
plugins { id 'durex.catalog' }

import com.github.durex.gradle.catalog.DependencyCatalogSnapshot

tasks.register('verifyCatalog') {
    def catalog = extensions.getByType(DependencyCatalogSnapshot)
    doLast {
        assert catalog.javaVersion == 25
        assert catalog.platform('spring').coordinate() ==
            'org.springframework.boot:spring-boot-dependencies:4.1.0'
        assert catalog.library('spring-jooq').platform == 'spring'
        assert catalog.plugin('graalvm-native').id == 'org.graalvm.buildtools.native'
    }
}
```

Run:

```bash
gradle -p build-logic/tests/registry-bridge-smoke verifyCatalog --stacktrace
```

Expected RED: unknown plugin `durex.catalog`.

- [ ] **Step 2: Add snapshot schema-mismatch RED fixture**

Create `build-logic/tests/registry-schema-mismatch/settings.gradle` with the same two included builds and `durex.settings`, then `build.gradle`:

```groovy
plugins { id 'durex.catalog' }

import com.github.durex.gradle.catalog.DurexRegistryBridge

DurexRegistryBridge.fromSnapshot([
    schemaVersion: 99,
    javaVersion: 25,
    platforms: [:],
    libraries: [:],
    plugins: [:]
])
```

After `durex.catalog` exists this fixture must fail with:

```text
Durex bootstrap error
Problem: unsupported dependency snapshot schema
Expected: 1
Actual: 99
```

- [ ] **Step 3: Export a deeply immutable neutral snapshot from bootstrap**

Add to `DependencyRegistry`:

```groovy
static final int SNAPSHOT_SCHEMA_VERSION = 1

Map<String, Object> snapshot() {
    Map<String, Object> raw = [
        schemaVersion: SNAPSHOT_SCHEMA_VERSION,
        javaVersion: javaVersion,
        platforms: platforms.collectEntries { id, value ->
            [(id): [module: value.module, version: value.version]]
        },
        libraries: libraries.collectEntries { id, value ->
            [(id): [module: value.module, version: value.version, platform: value.platform]]
        },
        plugins: plugins.collectEntries { id, value ->
            [(id): [id: value.id, module: value.module, version: value.version]]
        }
    ]
    deepFreeze(raw)
}
```

Implement `deepFreeze` recursively so every returned map/list is a copied unmodifiable container. `DependencyRegistryService.snapshot()` delegates to `registry().snapshot()`.

- [ ] **Step 4: Implement the local immutable catalog**

Use exact semantic getters:

```groovy
final class CatalogPlatform {
    final String alias
    final String module
    final String version
    String coordinate() { "${module}:${version}" }
}

final class CatalogLibrary {
    final String alias
    final String module
    final String version
    final String platform
    boolean isPlatformManaged() { platform != null }
    String notation() { version ? "${module}:${version}" : module }
}

final class CatalogPlugin {
    final String alias
    final String id
    final String module
    final String version
    String coordinate() { module ? "${module}:${version}" : null }
}
```

`DependencyCatalogSnapshot` owns immutable maps and exposes:

```groovy
int javaVersion()
CatalogPlatform platform(String alias)
CatalogLibrary library(String alias)
CatalogPlugin plugin(String alias)
CatalogPlugin pluginByGradleId(String id)
Collection<CatalogPlatform> platforms()
Collection<CatalogLibrary> libraries()
Collection<CatalogPlugin> plugins()
```

Unknown aliases fail with `Durex dependency catalog error` and the alias kind.

- [ ] **Step 5: Implement the neutral bridge without bootstrap type casts or Groovy dynamic registry calls**

`DurexRegistryBridge.fromProject(Project)` must:

```text
1. locate shared-service registration `durexDependencyRegistry`
2. obtain `Object service = registration.service.get()`
3. use Java reflection to invoke the zero-argument method named `snapshot`
4. require the result to be a Map
5. call fromSnapshot(raw)
```

Do not call `service.library(...)`, `service.platform(...)`, or cast `service` to a bootstrap class. Unwrap `InvocationTargetException` into a Durex bootstrap error with the original message.

- [ ] **Step 6: Add internal binary plugin `durex.catalog`**

Register in `build-logic/build.gradle.kts`:

```kotlin
gradlePlugin {
    plugins {
        create("durexCatalog") {
            id = "durex.catalog"
            implementationClass = "com.github.durex.gradle.catalog.DurexCatalogPlugin"
        }
        // keep existing durex.module registration
    }
}
```

`DurexCatalogPlugin.apply(Project)` loads the snapshot once during project configuration and adds it by type/name:

```groovy
DependencyCatalogSnapshot catalog = DurexRegistryBridge.fromProject(project)
project.extensions.add(DependencyCatalogSnapshot, 'durexDependencyCatalog', catalog)
```

- [ ] **Step 7: Migrate current build-logic consumers to the local catalog**

Change `DurexDependencyAccess` so `javaVersion`, platform/library/plugin lookups read `extensions.getByType(DependencyCatalogSnapshot)` and never call the bootstrap service directly. `durex.module` and `durex.jooq-schema` must apply `durex.catalog` before catalog access.

- [ ] **Step 8: Verify bridge and jOOQ behavior**

Run:

```bash
gradle -p build-logic/tests/registry-bridge-smoke verifyCatalog --stacktrace
gradle -p build-logic/tests/registry-schema-mismatch help --stacktrace
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace
```

Expected: bridge PASS; schema-mismatch FAILS with exact schema error; Q/R generation PASS.

- [ ] **Step 9: Commit**

```bash
git add -- build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest \
  build-logic/build.gradle.kts \
  build-logic/src/main/groovy/com/github/durex/gradle/catalog \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexDependencyAccess.groovy \
  build-logic/src/main/groovy/durex.jooq-schema.gradle \
  build-logic/tests/registry-bridge-smoke \
  build-logic/tests/registry-schema-mismatch
git commit -m "refactor: add neutral Durex dependency snapshot bridge"
```

---

### Task 2: Managed `DurexModuleModel` as the single source of module state

**Files:**
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/model/DurexModuleModel.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/DurexConfigurationException.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy`
- Modify: `build-logic/src/main/groovy/durex.java-library.gradle`
- Modify: `build-logic/src/main/groovy/durex.spring-library.gradle`
- Modify: `build-logic/src/main/groovy/durex.spring-service.gradle`
- Modify: `build-logic/src/main/groovy/durex.spring-base.gradle`
- Delete after migration: `build-logic/src/main/groovy/com/github/durex/gradle/DurexModuleState.groovy`
- Test: `build-logic/tests/module-model-smoke/**`
- Modify test: `build-logic/tests/module-conflict/**`

**Interfaces:**
- Consumes: `DependencyCatalogSnapshot` installed by `durex.catalog`.
- Produces managed extension type `DurexModuleModel` named `durexModuleModel`.
- Produces methods `claim(ModuleKind, String projectPath)`, `enableCapability(String)`, `bindPlatform(String configuration, String platformAlias)`.
- Later tasks consume: `moduleKind`, `capabilities`, `platformBindings` managed properties.

- [ ] **Step 1: RED fixture for managed module facts**

Create `build-logic/tests/module-model-smoke/build.gradle`:

```groovy
plugins { id 'durex.spring-service' }

import com.github.durex.gradle.model.DurexModuleModel
import com.github.durex.gradle.ModuleKind

tasks.register('verifyModuleModel') {
    def model = extensions.getByType(DurexModuleModel)
    doLast {
        assert model.moduleKind.get() == ModuleKind.SPRING_SERVICE
        assert model.capabilities.get().isEmpty()
        assert model.platformBindings.get().contains('implementation:spring')
        assert !model.metaClass.hasProperty(model, 'nativeEnabled')
    }
}
```

Run and expect RED because `DurexModuleModel` does not exist.

- [ ] **Step 2: Implement managed model**

Use:

```groovy
abstract class DurexModuleModel {
    abstract Property<ModuleKind> getModuleKind()
    abstract SetProperty<String> getCapabilities()
    abstract SetProperty<String> getPlatformBindings()

    void claim(ModuleKind requested, String projectPath) {
        if (!moduleKind.present) {
            moduleKind.set(requested)
            return
        }
        ModuleKind existing = moduleKind.get()
        if (existing != requested) {
            throw DurexConfigurationException.moduleTypeConflict(projectPath, existing, requested)
        }
    }

    void enableCapability(String capability) {
        capabilities.add(capability)
    }

    void bindPlatform(String configuration, String platformAlias) {
        platformBindings.add("${configuration}:${platformAlias}" as String)
    }
}
```

Set conventions for both sets to empty sets when the model is created.

- [ ] **Step 3: Centralize configuration errors**

`DurexConfigurationException` extends `GradleException` and provides factory methods. The module conflict output is exactly:

```text
Durex configuration error
Project: :example
Problem: module type conflict
Existing: SPRING_SERVICE
Requested: JAVA_LIBRARY
```

Later capability/dependency errors use the same prefix.

- [ ] **Step 4: Migrate module type scripts**

`DurexModulePlugin` applies `durex.catalog`, creates the managed model, and creates the existing `durex` extension. Replace every `extensions.getByType(DurexModuleState)` and `state.claim(...)` with the managed model.

`durex.spring-base` records exact bindings for every configuration where it adds the Spring platform; do not store only `spring`.

- [ ] **Step 5: Remove duplicate state class**

Delete `DurexModuleState.groovy` only after all module-type scripts compile without it. Do not add any replacement boolean for native.

- [ ] **Step 6: Verify**

```bash
gradle -p build-logic/tests/module-model-smoke verifyModuleModel --stacktrace
gradle -p build-logic/tests/module-conflict help --stacktrace
```

First PASS; second FAILS with the exact `Durex configuration error` module conflict.

- [ ] **Step 7: Commit**

```bash
git add -- build-logic/src/main/groovy/com/github/durex/gradle/model \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexConfigurationException.groovy \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy \
  build-logic/src/main/groovy/durex.java-library.gradle \
  build-logic/src/main/groovy/durex.spring-library.gradle \
  build-logic/src/main/groovy/durex.spring-service.gradle \
  build-logic/src/main/groovy/durex.spring-base.gradle \
  build-logic/tests/module-model-smoke \
  build-logic/tests/module-conflict \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexModuleState.groovy
git commit -m "refactor: add managed Durex module model"
```

---

### Task 3: Configuration-aware `DependencyBridge` and v2 dependency DSL

**Files:**
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/dependency/DependencyBridge.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexDependencyAccess.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy`
- Modify: `build-logic/src/main/groovy/durex.spring-base.gradle`
- Modify: `core/schema/music/json/build.spring.gradle`
- Test: `build-logic/tests/dependency-api-smoke/**`
- Test: `build-logic/tests/dependency-library-invalid/**`

**Interfaces:**
- Consumes: `DependencyCatalogSnapshot`, `DurexModuleModel`.
- Produces: `void DependencyBridge.add(Project, DurexModuleModel, String configuration, String alias)`.
- Produces: `String DependencyBridge.explicitNotation(Project, String alias)`.
- Produces public DSL `durex.dependency(String configuration, String alias)`.
- Keeps public `durex.library(String alias)` only for explicit-version/version-ref catalog entries.

- [ ] **Step 1: RED configuration-scoped dependency fixture**

Create a Spring-library fixture whose build file contains:

```groovy
plugins { id 'durex.spring-library' }

durex {
    dependency('api', 'jackson-annotations')
    dependency('implementation', 'spring-jooq')
}
```

Its verification task asserts the module model contains both `api:spring` and `implementation:spring`, and the `api` configuration contains only one Spring platform dependency even if two Spring-managed APIs are added.

Run and expect RED because `dependency(...)` does not exist.

- [ ] **Step 2: RED invalid `durex.library` fixture**

`build-logic/tests/dependency-library-invalid/build.gradle`:

```groovy
plugins { id 'durex.spring-library' }

dependencies {
    api durex.library('jackson-annotations')
}
```

After v2 implementation, `help` must fail with:

```text
Durex configuration error
Dependency alias: jackson-annotations
Problem: platform-managed library cannot be returned by durex.library(alias)
Use: durex.dependency(configuration, alias)
```

- [ ] **Step 3: Implement `DependencyBridge.add`**

Exact algorithm:

```text
require target configuration exists
lookup CatalogLibrary
if library.platform != null:
    binding = configuration + ':' + platform
    if model does not already contain binding:
        lookup CatalogPlatform
        dependencies.add(configuration, dependencies.platform(platform.coordinate()))
        model.bindPlatform(configuration, platform)
dependencies.add(configuration, library.notation())
```

If the configuration does not exist, fail with project, configuration, alias, and problem.

- [ ] **Step 4: Tighten `durex.library`**

`explicitNotation` rejects any `CatalogLibrary.isPlatformManaged()` entry with the migration guidance above. Explicit-version entries such as `javax-cdi` and `lombok` return `group:name:version` unchanged.

- [ ] **Step 5: Add public DSL and migrate real descriptor**

`DurexExtension` gains:

```groovy
void dependency(String configuration, String alias) {
    DependencyBridge.add(project, model, configuration, alias)
}
```

Migrate `core/schema/music/json/build.spring.gradle` from:

```groovy
dependencies {
    api durex.library('jackson-annotations')
    api durex.library('jackson-databind')
    api durex.library('jakarta-validation')
}
```

to:

```groovy
durex {
    dependency('api', 'jackson-annotations')
    dependency('api', 'jackson-databind')
    dependency('api', 'jakarta-validation')
}
```

Keep explicit-version `durex.library(...)` uses in music/common modules unchanged.

- [ ] **Step 6: Route existing internal dependency helpers through `DependencyBridge`**

Keep `DurexDependencyAccess` temporarily as a compatibility/internal facade if needed by convention scripts, but its `add(...)` delegates to `DependencyBridge.add(...)`; it must not implement a second platform algorithm.

- [ ] **Step 7: Verify**

```bash
gradle -p build-logic/tests/dependency-api-smoke verifyDependencyBindings dependencies --configuration api --stacktrace
gradle -p build-logic/tests/dependency-library-invalid help --stacktrace
gradle -p migration/spring-music :music-json:compileJava --stacktrace
```

Expected: first and third PASS; invalid library fixture FAILS with migration guidance.

- [ ] **Step 8: Commit**

```bash
git add -- build-logic/src/main/groovy/com/github/durex/gradle/dependency \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexDependencyAccess.groovy \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy \
  build-logic/src/main/groovy/durex.spring-base.gradle \
  core/schema/music/json/build.spring.gradle \
  build-logic/tests/dependency-api-smoke \
  build-logic/tests/dependency-library-invalid
git commit -m "refactor: make Durex dependency wiring configuration-aware"
```

---

### Task 4: Capability specs, registries, and one activation engine

**Files:**
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/capability/DependencyBinding.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/capability/CapabilitySpec.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/capability/CapabilityRegistry.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/capability/CapabilityPluginRegistry.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/capability/CapabilityEngine.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/capability/BuiltinCapabilities.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/capability/DurexCapabilitySupport.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy`
- Test: `build-logic/tests/capability-kernel-smoke/**`
- Test: `build-logic/tests/capability-missing-required/**`
- Test: `build-logic/tests/capability-cycle/**`
- Test: `build-logic/tests/capability-conflict/**`
- Test: `build-logic/tests/capability-module-invalid/**`

**Interfaces:**
- Consumes: managed module model and `DependencyBridge`.
- Produces immutable `CapabilitySpec` and `DependencyBinding`.
- Produces project extensions by type: `CapabilityRegistry`, `CapabilityPluginRegistry`, `CapabilityEngine`.
- Produces helper `DurexCapabilitySupport.registerAndEnable(Project, String pluginId, CapabilitySpec spec)`.

- [ ] **Step 1: Define immutable capability value objects**

Use builder/static factory semantics equivalent to:

```groovy
CapabilitySpec.builder('jooq')
    .allow(ModuleKind.SPRING_LIBRARY, ModuleKind.SPRING_SERVICE)
    .dependency('implementation', 'spring-jooq')
    .build()
```

Structural equality includes id, allowed module set, requires, conflicts, dependency bindings, and external plugin aliases. Collections are copied, immutable, and sorted/canonicalized where order is semantically irrelevant.

- [ ] **Step 2: RED registry semantics in a functional fixture**

After applying `durex.module`, fixture code obtains `CapabilityRegistry` and asserts:

```groovy
def foo = CapabilitySpec.builder('foo').build()
registry.register(foo)
registry.register(foo) // idempotent
registry.register(CapabilitySpec.builder('foo').dependency('implementation', 'spring-jooq').build())
```

The third registration must fail with `Durex configuration error` and `capability 'foo' is already registered with a different definition`.

- [ ] **Step 3: Implement capability and plugin registries**

`CapabilityRegistry.register` follows exact structural-idempotency semantics. `CapabilityPluginRegistry.register(pluginId, capabilityId)` is idempotent for the same pair and fails if one plugin id is mapped to another primary capability.

- [ ] **Step 4: Implement `CapabilityEngine.enable`**

Use a per-engine activation stack for cycle detection; do not use static/global mutable state.

Algorithm:

```text
if model.capabilities contains id: return
lookup spec or fail
if id is already on activation stack: fail with full cycle
validate module kind when allowedModules non-empty
for requiredId in sorted(spec.requires):
    require registry contains requiredId
    enable(requiredId)
for conflictId in sorted(spec.conflicts):
    if model.capabilities contains conflictId: fail
for externalPluginAlias in sorted(spec.externalPluginAliases):
    lookup catalog plugin alias
    pluginManager.apply(catalogPlugin.id)
for dependency binding in spec.dependencies:
    DependencyBridge.add(project, model, binding.configuration, binding.libraryAlias)
model.enableCapability(id)
```

Before enabling, also check reverse conflicts: any already-enabled registered spec whose `conflicts` contains the requested id blocks activation.

- [ ] **Step 5: Register built-ins without enabling them**

`BuiltinCapabilities.registerAll(registry)` adds exactly `jpa`, `jdbc`, `jooq`, `redis`, `native`, `lombok` with the semantics from the spec. `DurexModulePlugin` creates registries/engine and registers built-ins before the public extension can be used.

- [ ] **Step 6: Implement failure fixtures**

Use fixture build scripts to register synthetic specs directly in the registry and call `engine.enable(...)`:

```text
missing-required: foo requires bar, bar unregistered
cycle: foo requires bar; bar requires foo
conflict: foo conflicts bar; enable bar then foo
module-invalid: native-like capability allowed only SPRING_SERVICE on JAVA_LIBRARY
```

Assert the exact Durex error category plus missing/cycle/conflict/module details; no NPE or MissingMethodException.

- [ ] **Step 7: Verify**

```bash
gradle -p build-logic/tests/capability-kernel-smoke verifyCapabilityKernel --stacktrace
for fixture in capability-missing-required capability-cycle capability-conflict capability-module-invalid; do
  gradle -p "build-logic/tests/$fixture" help --stacktrace && exit 1 || true
done
```

Positive fixture PASS; all invalid fixtures fail for their intended Durex errors.

- [ ] **Step 8: Commit**

```bash
git add -- build-logic/src/main/groovy/com/github/durex/gradle/capability \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy \
  build-logic/tests/capability-*
git commit -m "feat: add Durex capability kernel"
```

---

### Task 5: Route built-in feature plugins and generic capability DSL through the engine

**Files:**
- Modify: `build-logic/src/main/groovy/durex.feature.jpa.gradle`
- Modify: `build-logic/src/main/groovy/durex.feature.jdbc.gradle`
- Modify: `build-logic/src/main/groovy/durex.feature.jooq.gradle`
- Modify: `build-logic/src/main/groovy/durex.feature.redis.gradle`
- Modify: `build-logic/src/main/groovy/durex.feature.native.gradle`
- Modify: `build-logic/src/main/groovy/durex.feature.lombok.gradle`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/PersistenceExtension.groovy`
- Test: `build-logic/tests/features-smoke/**`
- Test: `build-logic/tests/generic-capability-smoke/**`
- Test: `build-logic/tests/generic-capability-missing-mapping/**`

**Interfaces:**
- Consumes: `DurexCapabilitySupport`, built-in specs, capability/plugin registries, engine.
- Produces generic public method `void DurexExtension.capability(String pluginId)`.
- Preserves all existing typed first-party methods.

- [ ] **Step 1: Make every feature script a thin declaration/activation layer**

Example target for jOOQ:

```groovy
import com.github.durex.gradle.capability.BuiltinCapabilities
import com.github.durex.gradle.capability.DurexCapabilitySupport

plugins { id 'durex.module' }

DurexCapabilitySupport.registerAndEnable(
    project,
    'durex.feature.jooq',
    BuiltinCapabilities.JOOQ
)
```

Apply the same shape to JPA/JDBC/Redis/Native/Lombok. Remove each script's direct module checks, dependency additions, feature-state mutation, and native flag mutation.

- [ ] **Step 2: Preserve typed facades as plugin application only**

`PersistenceExtension.jpa/jdbc/jooq` and `DurexExtension.redis/nativeImage/lombok` continue to call `project.pluginManager.apply(featurePluginId)` and contain no semantic validation/wiring.

- [ ] **Step 3: Add generic capability API**

Implement:

```groovy
void capability(String pluginId) {
    project.pluginManager.apply(pluginId)
    String capabilityId = pluginRegistry.capabilityForPlugin(pluginId)
    if (capabilityId == null) {
        throw DurexConfigurationException.missingCapabilityMapping(project.path, pluginId)
    }
    engine.enable(capabilityId)
}
```

The final enable is intentionally idempotent because compliant feature plugins enable themselves when directly applied.

- [ ] **Step 4: Prove generic success using a capability plugin without typed DSL**

For v2 dogfood, add one internal fixture-only mapping/plugin entry point with plugin id `com.acme.durex.fixture` and capability id `fixture`, no typed method on `DurexExtension`. It uses `DurexCapabilitySupport.registerAndEnable(...)` and has no new business dependency. Keep the class clearly under an `internaltesting` package and document that publication-phase tests replace this synthetic entry point before external distribution is declared stable.

`generic-capability-smoke` uses only:

```groovy
plugins { id 'durex.java-library' }

durex {
    capability('com.acme.durex.fixture')
}
```

and asserts `capabilities == ['fixture']`.

- [ ] **Step 5: Prove missing mapping failure**

Fixture:

```groovy
plugins { id 'durex.java-library' }

durex {
    capability('java')
}
```

`java` applies successfully but has no Durex capability mapping; configuration must fail immediately with plugin id and `did not register a Durex primary capability`.

- [ ] **Step 6: Verify built-ins still coexist**

```bash
gradle -p build-logic/tests/features-smoke durexCapabilities dependencies --configuration compileClasspath --stacktrace
gradle -p build-logic/tests/generic-capability-smoke verifyGenericCapability --stacktrace
gradle -p build-logic/tests/generic-capability-missing-mapping help --stacktrace
```

Expected feature set remains sorted canonical output `jooq,jpa,redis`; missing mapping fails intentionally.

- [ ] **Step 7: Commit**

```bash
git add -- build-logic/src/main/groovy/durex.feature.*.gradle \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy \
  build-logic/src/main/groovy/com/github/durex/gradle/PersistenceExtension.groovy \
  build-logic/src/main/groovy/com/github/durex/gradle/internaltesting \
  build-logic/tests/features-smoke \
  build-logic/tests/generic-capability-*
git commit -m "refactor: route Durex features through capability engine"
```

---

### Task 6: Generic cache-safe diagnostics and `durexDoctor`

**Files:**
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexDependenciesTask.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexProjectsTask.groovy`
- Modify: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexSettingsPlugin.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/diagnostics/DurexCapabilitiesTask.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/diagnostics/DurexDoctorTask.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/diagnostics/DurexDoctorValidator.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy`
- Test: `build-bootstrap/tests/manifest-valid/**`
- Test: `build-bootstrap/tests/modules-auto/**`
- Test: `build-logic/tests/doctor-smoke/**`
- Test: `build-logic/tests/doctor-invalid/**`

**Interfaces:**
- Produces four diagnostic tasks: `durexDependencies`, `durexProjects`, `durexCapabilities`, `durexDoctor`.
- Task actions consume only Gradle input properties/collections.
- `DurexDoctorValidator.validate(...) -> List<String>` returns deterministic violations during configuration, not task execution.

- [ ] **Step 1: Replace bootstrap diagnostic closures with binary tasks**

`DurexDependenciesTask` has only inputs:

```groovy
@Input abstract Property<Integer> getJavaVersion()
@Input abstract ListProperty<String> getPlatformLines()
@Input abstract ListProperty<String> getPluginLines()
@Input abstract ListProperty<String> getLibraryLines()
```

Task action prints a fixed header and these already-sorted lines. It does not call the registry service.

`DurexProjectsTask` takes `ListProperty<String> projectLines` and prints it; it does not read settings/project registry during execution.

- [ ] **Step 2: Make bootstrap diagnostics ecosystem-neutral**

During settings configuration, build sorted lines by enumerating all platforms/plugins/libraries from the parsed registry/snapshot. Remove literal checks for `spring`, `spring-boot`, `graalvm-native`, and `jooq-codegen` from `DurexSettingsPlugin`.

Representative stable output includes all platform/plugin aliases and no special-case allowlist.

- [ ] **Step 3: Replace `durexCapabilities` closure with binary task**

Use inputs:

```groovy
@Input abstract Property<String> getModuleKind()
@Input abstract Property<Integer> getJavaVersion()
@Input abstract ListProperty<String> getCapabilities()
@Input abstract ListProperty<String> getPlatformBindings()
```

Project configuration wires providers/materialized sorted values. Task action only prints its inputs. `Native: enabled|disabled` is derived inside task action from the `capabilities` input, not a separate property.

- [ ] **Step 4: Implement doctor validator**

`DurexDoctorValidator` receives immutable/configuration-phase facts: project path, module model values, capability registry specs, plugin mappings, and local dependency catalog. It returns sorted violation strings for exactly these checks:

```text
module kind present
all enabled capabilities registered
plugin mapping references registered capabilities
allowed module kinds
all requires present
no conflicts
all dependency aliases exist
all external plugin aliases exist
all platform-managed dependency bindings have matching configuration:platform binding
no duplicate/inconsistent model entries
```

Do not resolve Maven artifacts or duplicate Gradle dependency resolution.

- [ ] **Step 5: Implement cache-safe `DurexDoctorTask`**

Inputs:

```groovy
@Input abstract Property<String> getProjectPathInput()
@Input abstract Property<String> getModuleKind()
@Input abstract ListProperty<String> getCapabilities()
@Input abstract ListProperty<String> getPlatformBindings()
@Input abstract ListProperty<String> getViolations()
```

If violations is non-empty, task action throws `GradleException` with `Durex Doctor` report and non-zero task result. Otherwise prints `Configuration    OK`.

Wire `violations` during configuration after the project DSL has been evaluated; `afterEvaluate` is allowed only to materialize these inputs and must not install task actions or read state at execution time.

- [ ] **Step 6: RED/green doctor fixtures**

`doctor-smoke` applies Spring service + jOOQ + lombok and expects:

```text
Module: SPRING_SERVICE
Capabilities: jooq,lombok
Configuration: OK
```

`doctor-invalid` intentionally mutates/registers a synthetic inconsistent spec/mapping so doctor fails with the exact violation instead of the capability engine failing first.

- [ ] **Step 7: Verify deterministic output**

Run each diagnostic twice and compare normalized output snippets. Assert canonical sort order lexicographically; do not hard-code a different semantic order.

```bash
gradle -p build-bootstrap/tests/manifest-valid durexDependencies --stacktrace
gradle -p build-bootstrap/tests/modules-auto durexProjects --stacktrace
gradle -p build-logic/tests/doctor-smoke durexCapabilities durexDoctor --stacktrace
gradle -p build-logic/tests/doctor-invalid durexDoctor --stacktrace
```

- [ ] **Step 8: Commit**

```bash
git add -- build-bootstrap/src/main/groovy/com/github/durex/gradle/settings \
  build-bootstrap/tests/manifest-valid \
  build-bootstrap/tests/modules-auto \
  build-logic/src/main/groovy/com/github/durex/gradle/diagnostics \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy \
  build-logic/tests/doctor-*
git commit -m "feat: add cache-safe Durex diagnostics and doctor"
```

---

### Task 7: Configuration-cache and parallel-build hardening

**Files:**
- Create: `build-logic/tests/config-cache-groovy/**`
- Create: `build-logic/tests/config-cache-kotlin/**`
- Create: `build-logic/tests/parallel-multiproject/**`
- Modify as failures require: only Plugin Core v2 implementation files from Tasks 1-6
- Modify: `.github/workflows/durex-build-platform.yml`

**Interfaces:**
- Consumes complete v2 kernel and diagnostics.
- Produces no new public DSL; this task hardens lifecycle behavior.

- [ ] **Step 1: Groovy configuration-cache fixture**

Fixture applies `durex.spring-service`, enables `jooq` and `redis`, and runs `durexDoctor`.

CI/local script:

```bash
rm -rf build-logic/tests/config-cache-groovy/.gradle/configuration-cache
gradle -p build-logic/tests/config-cache-groovy durexDoctor --configuration-cache --stacktrace | tee /tmp/durex-cc1.log
gradle -p build-logic/tests/config-cache-groovy durexDoctor --configuration-cache --stacktrace | tee /tmp/durex-cc2.log
grep -F 'Reusing configuration cache.' /tmp/durex-cc2.log
```

Both commands exit 0 and the second proves reuse.

- [ ] **Step 2: Kotlin DSL configuration-cache fixture**

Keep `settings.gradle.kts` and `build.gradle.kts`; use:

```kotlin
plugins { id("durex.spring-service") }

durex {
    persistence { jooq() }
    redis()
}
```

Run the same two-invocation cache-reuse check. If Kotlin type-safe accessors fail, fix the public plugin/extension API; do not convert the fixture to Groovy.

- [ ] **Step 3: Settings diagnostics configuration-cache proof**

Run twice:

```bash
gradle -p build-bootstrap/tests/modules-auto durexProjects --configuration-cache --stacktrace
gradle -p build-bootstrap/tests/modules-auto durexProjects --configuration-cache --stacktrace
```

Second invocation must reuse cache. Fix settings/task wiring rather than disabling configuration cache.

- [ ] **Step 4: Parallel multi-project fixture**

Create three modules via Durex settings discovery/manual mapping:

```text
:java-lib       -> durex.java-library
:spring-lib     -> durex.spring-library + jooq
:spring-service -> durex.spring-service + redis, depends on both libraries
```

Run:

```bash
gradle -p build-logic/tests/parallel-multiproject build --parallel --stacktrace
```

Expected PASS with no cross-project/global mutable-state failures.

- [ ] **Step 5: Remove configuration-cache violations found by Gradle**

Typical fixes are constrained to:

```text
task inputs/provider wiring
project/configuration access timing
mutable static/global state
live extension/service capture in task actions
```

Do not suppress configuration-cache problems or use `--no-configuration-cache` in acceptance tests.

- [ ] **Step 6: Add CI contract**

Extend `Durex Build Platform` workflow with the exact Groovy/Kotlin/settings cache-reuse commands and `--parallel` fixture. Intended-failure fixtures must still invert exit status and grep Durex-specific errors.

- [ ] **Step 7: Commit**

```bash
git add -- build-logic/tests/config-cache-* \
  build-logic/tests/parallel-multiproject \
  build-bootstrap/tests/modules-auto \
  build-bootstrap/src/main/groovy/com/github/durex/gradle \
  build-logic/src/main/groovy/com/github/durex/gradle \
  .github/workflows/durex-build-platform.yml
git commit -m "test: harden Durex plugins for configuration cache"
```

---

### Task 8: Real-build migration, regression matrix, and final CI evidence

**Files:**
- Modify only as required by v2 API migration:
  - `core/music/build.spring.gradle`
  - `core/schema/music/json/build.spring.gradle`
  - `core/schema/music/entity/build.spring.gradle`
  - `core/schema/music/repo/build.spring.gradle`
  - `core/shared/jakarta/common/build.spring.gradle`
  - `core/shared/json-schema-annotation/build.spring.gradle`
  - `reference/spring-capabilities/**`
  - `reference/spring-native/**`
- Modify: `.github/workflows/durex-build-platform.yml`
- Modify if needed: `.github/workflows/spring-music.yml`
- Modify if needed: `.github/workflows/spring-native-reference.yml`

**Interfaces:**
- Consumes final v2 public API; produces no new core API.
- Acceptance is real application/schema/native behavior plus source-level removal of obsolete v1 paths.

- [ ] **Step 1: Migrate all platform-managed `durex.library(...)` callers**

Search:

```bash
grep -R -n "durex.library" core reference migration build-logic/tests
```

Every remaining production use must refer to an explicit-version/version-ref library. Any Spring-BOM-managed use is replaced with `durex.dependency(configuration, alias)`.

- [ ] **Step 2: Verify no v1 state or direct cross-build registry path remains**

These searches must produce no production hits:

```bash
grep -R -n 'DurexModuleState\|nativeEnabled' build-logic/src/main || true
grep -R -n 'service.get().\(library\|platform\|plugin\|javaVersion\)' build-logic/src/main || true
```

`DurexSettingsPlugin.groovy` must contain no literal `spring-boot`, `graalvm-native`, or `jooq-codegen` diagnostic allowlist.

- [ ] **Step 3: Real Spring Music regression**

Run:

```bash
gradle -p migration/spring-music :music:durexDoctor :music:compileJava :music:test --configuration-cache --stacktrace
```

Expected: doctor OK; all CRUD MockMvc + H2/jOOQ integration tests green.

- [ ] **Step 4: jOOQ schema regression**

```bash
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --configuration-cache --stacktrace
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/QMusic.java
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/records/RMusic.java
```

Expected Q/R generation unchanged.

- [ ] **Step 5: Spring Native regression**

Run JVM/AOT first:

```bash
gradle -p reference/spring-native durexDoctor test processAot bootJar --configuration-cache --stacktrace
```

Then GraalVM-capable CI runs:

```bash
gradle -p reference/spring-native nativeTest --stacktrace
gradle -p reference/spring-native nativeCompile --stacktrace
```

and the existing `/hello` native HTTP smoke remains required.

- [ ] **Step 6: Spring capabilities/Kotlin DSL regression**

```bash
gradle -p reference/spring-capabilities durexDoctor dependencies --configuration compileClasspath --configuration-cache --stacktrace
```

Expected JPA/JDBC/jOOQ/Redis dependencies aligned by Spring BOM and doctor OK.

- [ ] **Step 7: Final CI matrix**

Required green checks on the PR head:

```text
Durex Build Platform
  snapshot bridge
  schema mismatch negative test
  dependency API
  capability kernel positive/negative tests
  typed + generic capability DSL
  doctor positive/negative
  Groovy/Kotlin configuration-cache reuse
  settings cache reuse
  parallel multi-project build
  jOOQ schema

Spring Music Migration
  project graph
  compile
  CRUD/runtime integration
  catalog-free descriptors
  :music:durexDoctor

Spring Native Reference
  capability resolution
  JVM tests
  AOT HTTP smoke
  nativeTest
  nativeCompile
  native HTTP smoke
```

Legacy Quarkus/Shared Utils workflow failures are not to be described as green if they remain unrelated/red.

- [ ] **Step 8: Final scope review**

Compare against the base branch and verify this plan did not:

```text
add Kafka/Mongo/etc.
change root Gradle cutover policy
publish plugins externally
move capability behavior into TOML
rewrite every convention script into binary form
```

- [ ] **Step 9: Commit**

```bash
git add -- core/music/build.spring.gradle core/schema/music core/shared \
  reference/spring-capabilities reference/spring-native \
  .github/workflows/durex-build-platform.yml \
  .github/workflows/spring-music.yml \
  .github/workflows/spring-native-reference.yml
git commit -m "refactor: complete Durex Plugin Core v2 migration"
```

---

## Execution Notes

- Execute Tasks 1-8 in order; each task has interfaces consumed by later tasks.
- Keep RED commits/tests focused and temporary only when the task explicitly calls for a RED checkpoint; final task commits must leave branch buildable except intentionally failing fixtures.
- Prefer existing Gradle functional fixture style over introducing a new unit-test framework merely for this refactor.
- If Gradle 9.1 configuration-cache diagnostics expose a design conflict, the spec is authoritative: change implementation lifecycle/wiring rather than weakening the acceptance command.
- If a capability needs special behavior beyond dependencies/external plugin application during implementation, stop and assess whether it belongs in the generic kernel as an explicit hook or is outside v2; do not silently add feature-specific branches to `CapabilityEngine`.
- The synthetic `com.acme.durex.fixture` plugin validates the third-party-style public contract only; external publication/classloader packaging is deferred to the publication phase defined as out-of-scope by the spec.
