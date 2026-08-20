# Durex Plugin Core v2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace duplicated Durex Gradle feature behavior with a typed capability kernel, configuration-aware dependency bridge, neutral bootstrap snapshot boundary, cache-safe diagnostics, and `durexDoctor` while preserving existing DSL and Spring/jOOQ/native behavior.

**Architecture:** `build-bootstrap` remains the ecosystem-neutral manifest/settings layer and exposes a versioned JDK-container snapshot. `build-logic` rebuilds its own immutable dependency catalog, owns a managed `DurexModuleModel`, routes all dependency insertion through `DependencyBridge`, and routes all feature activation through `CapabilityEngine`; existing convention scripts remain thin composition layers in v2.

**Tech Stack:** Gradle 9.1, Java 25, Groovy/Java Gradle plugins, Gradle managed `Property`/`SetProperty`/`ListProperty`, configuration cache, parallel execution, Spring Boot 4.1, GraalVM 25, jOOQ 3.21.5.

**Spec:** `docs/superpowers/specs/2026-08-20-durex-plugin-core-v2-design.md`

## Global Constraints

- TOML remains dependency/plugin data only; do not add capability behavior to TOML.
- Preserve `persistence.jpa()`, `persistence.jdbc()`, `persistence.jooq()`, `redis()`, `nativeImage()`, and `lombok()`.
- Add `durex.capability(pluginId)` without making `DurexExtension` a central feature registry.
- `durex.library(alias)` is valid only for libraries owning `version`/`version.ref`; platform-managed aliases use `durex.dependency(configuration, alias)` / `DependencyBridge.add(...)`.
- Platform bindings are configuration-scoped (`implementation:spring` != `api:spring`).
- All built-in feature execution converges on one `CapabilityEngine` path.
- `DurexModuleModel` is the sole source of module kind, capabilities, and platform bindings; no `nativeEnabled` duplicate state.
- Cross-build access uses a deeply immutable neutral snapshot containing only `String`, `Integer`, `Boolean`, `Map`, and `List`; build-logic must not cast bootstrap model classes or call bootstrap `library/platform/plugin/javaVersion` methods dynamically.
- Snapshot schema version is exactly `1` in v2.
- Bootstrap diagnostics must not hard-code `spring`, `spring-boot`, `graalvm-native`, `jooq-codegen`, or any capability.
- Diagnostic ordering is deterministic and lexicographically sorted.
- Diagnostic task actions read declared task inputs only; they do not access live `Project`, extension, BuildService, engine, or mutable registries.
- Gradle 9.1 configuration-cache reuse and representative `--parallel` execution are hard requirements.
- Keep convention scripts as thin wrappers; do not rewrite every plugin to binary form.
- Do not add Kafka/Mongo/Security/Flyway/OpenAPI/Testcontainers, perform root Gradle cutover, or publish plugins externally.
- Preserve Spring Music, Spring Native, and jOOQ schema/codegen behavior.

---

## Task 1 — Neutral dependency snapshot and local catalog bridge

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
- Create fixture: `build-logic/tests/registry-bridge-smoke/**`
- Create fixture: `build-logic/tests/registry-schema-mismatch/**`

**Interfaces:**
- `Map<String,Object> DependencyRegistry.snapshot()`
- `Map<String,Object> DependencyRegistryService.snapshot()`
- `DependencyCatalogSnapshot DurexRegistryBridge.fromSnapshot(Map raw)`
- `DependencyCatalogSnapshot DurexRegistryBridge.fromProject(Project project)`
- internal plugin `durex.catalog` installs extension `durexDependencyCatalog` of type `DependencyCatalogSnapshot`.

- [ ] **Step 1: Write RED registry bridge fixture**

`build-logic/tests/registry-bridge-smoke/settings.gradle`:

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

`build.gradle`:

```groovy
plugins { id 'durex.catalog' }

import com.github.durex.gradle.catalog.DependencyCatalogSnapshot

def catalog = extensions.getByType(DependencyCatalogSnapshot)
assert catalog.javaVersion() == 25
assert catalog.platform('spring').coordinate() ==
    'org.springframework.boot:spring-boot-dependencies:4.1.0'
assert catalog.library('spring-jooq').platform == 'spring'
assert catalog.plugin('graalvm-native').id == 'org.graalvm.buildtools.native'
```

Run:

```bash
gradle -p build-logic/tests/registry-bridge-smoke help --stacktrace
```

Expected RED: unknown plugin `durex.catalog`.

- [ ] **Step 2: Export snapshot schema 1 from bootstrap**

Add:

```groovy
static final int SNAPSHOT_SCHEMA_VERSION = 1

Map<String, Object> snapshot() {
    deepFreeze([
        schemaVersion: SNAPSHOT_SCHEMA_VERSION,
        javaVersion: javaVersion,
        platforms: platforms.collectEntries { alias, p ->
            [(alias): [module: p.module, version: p.version]]
        },
        libraries: libraries.collectEntries { alias, l ->
            [(alias): [module: l.module, version: l.version, platform: l.platform]]
        },
        plugins: plugins.collectEntries { alias, p ->
            [(alias): [id: p.id, module: p.module, version: p.version]]
        }
    ])
}
```

`deepFreeze` recursively copies and wraps maps/lists with `Collections.unmodifiableMap/List`; scalar values pass through. `DependencyRegistryService.snapshot()` delegates to the registry.

- [ ] **Step 3: Implement immutable build-logic catalog**

Use exact fields/semantics:

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

`DependencyCatalogSnapshot` exposes:

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

Unknown alias errors use prefix `Durex dependency catalog error`.

- [ ] **Step 4: Implement neutral reflective bridge**

`fromProject(Project)` performs exactly:

```text
find sharedServices registration `durexDependencyRegistry`
get Object service
find zero-argument Java method named `snapshot`
invoke it reflectively
require returned value is Map
call fromSnapshot(Map)
```

No bootstrap class cast and no Groovy `service.library/platform/plugin/javaVersion` calls. Unwrap `InvocationTargetException` to a `Durex bootstrap error` preserving the cause message.

`fromSnapshot(Map)` requires `schemaVersion == 1`; mismatch message:

```text
Durex bootstrap error
Problem: unsupported dependency snapshot schema
Expected: 1
Actual: 99
```

- [ ] **Step 5: Add `durex.catalog` binary plugin**

Register in `build-logic/build.gradle.kts`:

```kotlin
create("durexCatalog") {
    id = "durex.catalog"
    implementationClass = "com.github.durex.gradle.catalog.DurexCatalogPlugin"
}
```

`DurexCatalogPlugin.apply` loads the local snapshot once during configuration and adds:

```groovy
project.extensions.add(
    DependencyCatalogSnapshot,
    'durexDependencyCatalog',
    DurexRegistryBridge.fromProject(project)
)
```

- [ ] **Step 6: Route existing catalog users through local snapshot**

`DurexDependencyAccess` reads `DependencyCatalogSnapshot` from project extensions. `durex.module` and `durex.jooq-schema` apply `durex.catalog` before access. No build-logic production file calls the bootstrap registry methods directly after this step.

- [ ] **Step 7: Add schema mismatch fixture**

`build-logic/tests/registry-schema-mismatch/build.gradle`:

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

- [ ] **Step 8: Verify and commit**

```bash
gradle -p build-logic/tests/registry-bridge-smoke help --stacktrace
if gradle -p build-logic/tests/registry-schema-mismatch help --stacktrace > /tmp/schema.log 2>&1; then exit 1; fi
grep -Fq 'Expected: 1' /tmp/schema.log
grep -Fq 'Actual: 99' /tmp/schema.log
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace

git add -- build-bootstrap/src/main/groovy/com/github/durex/gradle/manifest \
  build-logic/build.gradle.kts build-logic/src/main/groovy/com/github/durex/gradle/catalog \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexDependencyAccess.groovy \
  build-logic/src/main/groovy/durex.jooq-schema.gradle \
  build-logic/tests/registry-bridge-smoke build-logic/tests/registry-schema-mismatch
git commit -m "refactor: add neutral Durex dependency snapshot bridge"
```

---

## Task 2 — Managed `DurexModuleModel`

**Files:**
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/model/DurexModuleModel.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/DurexConfigurationException.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy`
- Modify: `build-logic/src/main/groovy/durex.java-library.gradle`
- Modify: `build-logic/src/main/groovy/durex.spring-base.gradle`
- Modify: `build-logic/src/main/groovy/durex.spring-library.gradle`
- Modify: `build-logic/src/main/groovy/durex.spring-service.gradle`
- Delete: `build-logic/src/main/groovy/com/github/durex/gradle/DurexModuleState.groovy`
- Create fixture: `build-logic/tests/module-model-smoke/**`
- Modify fixture: `build-logic/tests/module-conflict/**`

**Interfaces:**

```groovy
abstract class DurexModuleModel {
    abstract Property<ModuleKind> getModuleKind()
    abstract SetProperty<String> getCapabilities()
    abstract SetProperty<String> getPlatformBindings()
    void claim(ModuleKind requested, String projectPath)
    void enableCapability(String capability)
    void bindPlatform(String configuration, String platformAlias)
}
```

- [ ] **Step 1: RED managed-model fixture**

`module-model-smoke/build.gradle`:

```groovy
plugins { id 'durex.spring-service' }

import com.github.durex.gradle.model.DurexModuleModel
import com.github.durex.gradle.ModuleKind

def model = extensions.getByType(DurexModuleModel)
assert model.moduleKind.get() == ModuleKind.SPRING_SERVICE
assert model.capabilities.get().isEmpty()
assert model.platformBindings.get().contains('implementation:spring')
```

Expected RED: `DurexModuleModel` missing.

- [ ] **Step 2: Implement managed model and configuration error**

Model methods:

```groovy
void claim(ModuleKind requested, String projectPath) {
    if (!moduleKind.isPresent()) {
        moduleKind.set(requested)
        return
    }
    ModuleKind existing = moduleKind.get()
    if (existing != requested) {
        throw DurexConfigurationException.moduleTypeConflict(projectPath, existing, requested)
    }
}
void enableCapability(String id) { capabilities.add(id) }
void bindPlatform(String configuration, String alias) {
    platformBindings.add("${configuration}:${alias}" as String)
}
```

Set empty-set conventions for capabilities/platformBindings.

`DurexConfigurationException.moduleTypeConflict` output:

```text
Durex configuration error
Project: :example
Problem: module type conflict
Existing: SPRING_SERVICE
Requested: JAVA_LIBRARY
```

- [ ] **Step 3: Migrate module scripts and remove old state**

`DurexModulePlugin` applies `durex.catalog`, creates `durexModuleModel`, then creates the existing `durex` extension. Module scripts use the managed model for `claim`. `durex.spring-base` records the exact configuration/platform pair each time it adds a Spring platform. Delete `DurexModuleState.groovy` after all source references are removed.

- [ ] **Step 4: Verify and commit**

```bash
gradle -p build-logic/tests/module-model-smoke help --stacktrace
if gradle -p build-logic/tests/module-conflict help --stacktrace > /tmp/module-conflict.log 2>&1; then exit 1; fi
grep -Fq 'Durex configuration error' /tmp/module-conflict.log
grep -Fq 'Existing: SPRING_SERVICE' /tmp/module-conflict.log

git add -- build-logic/src/main/groovy/com/github/durex/gradle/model \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexConfigurationException.groovy \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexModuleState.groovy \
  build-logic/src/main/groovy/durex.java-library.gradle build-logic/src/main/groovy/durex.spring-base.gradle \
  build-logic/src/main/groovy/durex.spring-library.gradle build-logic/src/main/groovy/durex.spring-service.gradle \
  build-logic/tests/module-model-smoke build-logic/tests/module-conflict
git commit -m "refactor: add managed Durex module model"
```

---

## Task 3 — Configuration-aware dependency bridge and DSL

**Files:**
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/dependency/DependencyBridge.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexDependencyAccess.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy`
- Modify: `build-logic/src/main/groovy/durex.spring-base.gradle`
- Modify: `core/schema/music/json/build.spring.gradle`
- Create fixture: `build-logic/tests/dependency-api-smoke/**`
- Create fixture: `build-logic/tests/dependency-library-invalid/**`

**Interfaces:**
- `void DependencyBridge.add(Project, DurexModuleModel, String configuration, String alias)`
- `String DependencyBridge.explicitNotation(Project, String alias)`
- public `durex.dependency(String configuration, String alias)`
- public `durex.library(String alias)` only for non-platform-managed libraries.

- [ ] **Step 1: RED scoped-platform fixture**

Fixture build:

```groovy
plugins { id 'durex.spring-library' }

durex {
    dependency('api', 'jackson-annotations')
    dependency('api', 'jackson-databind')
    dependency('implementation', 'spring-jooq')
}
```

Verification during configuration:

```groovy
import com.github.durex.gradle.model.DurexModuleModel

def model = extensions.getByType(DurexModuleModel)
assert model.platformBindings.get().containsAll(['api:spring', 'implementation:spring'])
```

Expected RED: `dependency` missing.

- [ ] **Step 2: Implement `DependencyBridge.add`**

Exact algorithm:

```text
require configuration exists
lookup CatalogLibrary
if platform-managed:
  binding = configuration + ':' + platformAlias
  if binding absent:
    lookup CatalogPlatform
    add Gradle platform(platform.coordinate()) to that configuration
    model.bindPlatform(configuration, platformAlias)
add library.notation() to configuration
```

Multiple platform-managed libraries in the same configuration add the platform once.

Missing configuration error includes project, configuration, dependency alias, and `Durex configuration error` prefix.

- [ ] **Step 3: Tighten `durex.library` and add `durex.dependency`**

`explicitNotation` rejects platform-managed libraries with exact guidance:

```text
Durex configuration error
Dependency alias: jackson-annotations
Problem: platform-managed library cannot be returned by durex.library(alias)
Use: durex.dependency(configuration, alias)
```

`DurexExtension.dependency` delegates to `DependencyBridge.add`. `DurexDependencyAccess.add` becomes a compatibility facade delegating to the same bridge; no second platform algorithm remains.

- [ ] **Step 4: Migrate the known production BOM-managed direct calls**

Replace `core/schema/music/json/build.spring.gradle` dependencies with:

```groovy
durex {
    dependency('api', 'jackson-annotations')
    dependency('api', 'jackson-databind')
    dependency('api', 'jakarta-validation')
}
```

Keep explicit-version `durex.library('javax-*')` calls unchanged.

- [ ] **Step 5: Add invalid-library fixture and verify**

Invalid fixture:

```groovy
plugins { id 'durex.spring-library' }
dependencies { api durex.library('jackson-annotations') }
```

Commands:

```bash
gradle -p build-logic/tests/dependency-api-smoke help --stacktrace
if gradle -p build-logic/tests/dependency-library-invalid help --stacktrace > /tmp/library-invalid.log 2>&1; then exit 1; fi
grep -Fq 'Use: durex.dependency(configuration, alias)' /tmp/library-invalid.log
gradle -p migration/spring-music :music-json:compileJava --stacktrace

git add -- build-logic/src/main/groovy/com/github/durex/gradle/dependency \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexDependencyAccess.groovy \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy \
  build-logic/src/main/groovy/durex.spring-base.gradle core/schema/music/json/build.spring.gradle \
  build-logic/tests/dependency-api-smoke build-logic/tests/dependency-library-invalid
git commit -m "refactor: make Durex dependency wiring configuration-aware"
```

---

## Task 4 — Capability specs, registries, and activation engine

**Files:**
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/capability/DependencyBinding.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/capability/CapabilitySpec.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/capability/CapabilityRegistry.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/capability/CapabilityPluginRegistry.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/capability/CapabilityEngine.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/capability/BuiltinCapabilities.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/capability/DurexCapabilitySupport.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy`
- Create fixtures: `build-logic/tests/capability-kernel-smoke/**`, `capability-missing-required/**`, `capability-cycle/**`, `capability-conflict/**`, `capability-module-invalid/**`

**Interfaces:**

```groovy
CapabilityRegistry.register(CapabilitySpec spec)
CapabilitySpec CapabilityRegistry.get(String id)
CapabilityPluginRegistry.register(String pluginId, String capabilityId)
String CapabilityPluginRegistry.capabilityForPlugin(String pluginId)
void CapabilityEngine.enable(String capabilityId)
void DurexCapabilitySupport.registerAndEnable(Project project, String pluginId, CapabilitySpec spec)
```

- [ ] **Step 1: Implement immutable capability values**

Builder supports:

```groovy
CapabilitySpec.builder('jooq')
    .allow(ModuleKind.SPRING_LIBRARY, ModuleKind.SPRING_SERVICE)
    .require('other-capability')
    .conflict('conflicting-capability')
    .dependency('implementation', 'spring-jooq')
    .externalPlugin('graalvm-native')
    .build()
```

Structural equality includes id, allowed modules, requires, conflicts, dependency bindings, and external plugin aliases. Collections are immutable; semantically unordered sets are canonicalized.

- [ ] **Step 2: Implement registry semantics**

- same id + structurally equal spec: no-op;
- same id + different spec: `Durex configuration error`;
- same plugin id + same capability id mapping: no-op;
- same plugin id + different capability id: `Durex configuration error`.

- [ ] **Step 3: Implement `CapabilityEngine.enable`**

Use one engine instance per project and a per-engine activation stack. Exact order:

```text
return if already enabled
lookup spec
cycle check
validate allowed module kind
for each sorted required capability:
  require it is registered
  recursively enable it
check declared conflicts against enabled set
check reverse conflicts from enabled registered specs
for each sorted external plugin alias:
  lookup CatalogPlugin and pluginManager.apply(plugin.id)
for each DependencyBinding:
  DependencyBridge.add(project, model, configuration, libraryAlias)
model.enableCapability(id)
```

No static/global mutable activation state.

- [ ] **Step 4: Register built-in specs in module plugin**

`BuiltinCapabilities.registerAll` adds exactly:

```text
jpa   -> Spring library/service; implementation spring-jpa
jdbc  -> Spring library/service; implementation spring-jdbc
jooq  -> Spring library/service; implementation spring-jooq
redis -> Spring library/service; implementation spring-redis
native -> Spring service; external plugin graalvm-native
lombok -> compileOnly lombok + annotationProcessor lombok
```

`DurexModulePlugin.apply` order after Task 4 is exactly:

```text
apply durex.catalog
create DurexModuleModel
create CapabilityRegistry
create CapabilityPluginRegistry
create CapabilityEngine
register built-ins
create public DurexExtension
register diagnostics placeholders/tasks currently present
```

- [ ] **Step 5: Add positive and negative fixtures**

Fixture scripts obtain registries/engine by type. Define exact invalid cases:

```groovy
// missing required
registry.register(CapabilitySpec.builder('foo').require('bar').build())
engine.enable('foo')

// cycle
registry.register(CapabilitySpec.builder('foo').require('bar').build())
registry.register(CapabilitySpec.builder('bar').require('foo').build())
engine.enable('foo')

// conflict
registry.register(CapabilitySpec.builder('foo').conflict('bar').build())
registry.register(CapabilitySpec.builder('bar').build())
engine.enable('bar')
engine.enable('foo')
```

Module-invalid fixture uses JAVA_LIBRARY and a synthetic capability allowed only on SPRING_SERVICE.

- [ ] **Step 6: Verify and commit**

```bash
gradle -p build-logic/tests/capability-kernel-smoke help --stacktrace
for f in capability-missing-required capability-cycle capability-conflict capability-module-invalid; do
  if gradle -p "build-logic/tests/$f" help --stacktrace > "/tmp/$f.log" 2>&1; then exit 1; fi
  grep -Fq 'Durex configuration error' "/tmp/$f.log"
done

git add -- build-logic/src/main/groovy/com/github/durex/gradle/capability \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy build-logic/tests/capability-*
git commit -m "feat: add Durex capability kernel"
```

---

## Task 5 — Route built-in and generic capability plugins through the engine

**Files:**
- Modify: `build-logic/src/main/groovy/durex.feature.jpa.gradle`
- Modify: `build-logic/src/main/groovy/durex.feature.jdbc.gradle`
- Modify: `build-logic/src/main/groovy/durex.feature.jooq.gradle`
- Modify: `build-logic/src/main/groovy/durex.feature.redis.gradle`
- Modify: `build-logic/src/main/groovy/durex.feature.native.gradle`
- Modify: `build-logic/src/main/groovy/durex.feature.lombok.gradle`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/PersistenceExtension.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/internaltesting/FixtureCapabilityPlugin.groovy`
- Modify: `build-logic/build.gradle.kts`
- Modify fixture: `build-logic/tests/features-smoke/**`
- Create fixture: `build-logic/tests/generic-capability-smoke/**`
- Create fixture: `build-logic/tests/generic-capability-missing-mapping/**`

- [ ] **Step 1: Thin every built-in feature script**

Target shape for jOOQ:

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

Apply the same shape to JPA/JDBC/Redis/Native/Lombok. Remove direct module checks, dependency insertion, feature state mutation, and native boolean mutation from feature scripts.

- [ ] **Step 2: Keep typed DSL as facade-only plugin application**

`PersistenceExtension.jpa/jdbc/jooq` and `DurexExtension.redis/nativeImage/lombok` only apply their feature plugin ids.

- [ ] **Step 3: Add generic capability API**

```groovy
void capability(String pluginId) {
    project.pluginManager.apply(pluginId)
    CapabilityPluginRegistry mapping = project.extensions.getByType(CapabilityPluginRegistry)
    String capabilityId = mapping.capabilityForPlugin(pluginId)
    if (capabilityId == null) {
        throw DurexConfigurationException.missingCapabilityMapping(project.path, pluginId)
    }
    project.extensions.getByType(CapabilityEngine).enable(capabilityId)
}
```

Missing mapping message contains `Gradle plugin id: <id>` and `did not register a Durex primary capability`.

- [ ] **Step 4: Add fixture-only third-party-style plugin**

`FixtureCapabilityPlugin.groovy`:

```groovy
package com.github.durex.gradle.internaltesting

import com.github.durex.gradle.capability.CapabilitySpec
import com.github.durex.gradle.capability.DurexCapabilitySupport
import org.gradle.api.Plugin
import org.gradle.api.Project

class FixtureCapabilityPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply('durex.module')
        DurexCapabilitySupport.registerAndEnable(
            project,
            'com.acme.durex.fixture',
            CapabilitySpec.builder('fixture').build()
        )
    }
}
```

Register only for dogfood testing:

```kotlin
create("fixtureCapability") {
    id = "com.acme.durex.fixture"
    implementationClass = "com.github.durex.gradle.internaltesting.FixtureCapabilityPlugin"
}
```

No typed facade method is added for `fixture`.

- [ ] **Step 5: Generic fixtures**

Success:

```groovy
plugins { id 'durex.java-library' }
durex { capability('com.acme.durex.fixture') }
import com.github.durex.gradle.model.DurexModuleModel
assert extensions.getByType(DurexModuleModel).capabilities.get() == ['fixture'] as Set
```

Missing mapping:

```groovy
plugins { id 'durex.java-library' }
durex { capability('java') }
```

- [ ] **Step 6: Verify and commit**

```bash
gradle -p build-logic/tests/features-smoke durexCapabilities dependencies --configuration compileClasspath --stacktrace
gradle -p build-logic/tests/generic-capability-smoke help --stacktrace
if gradle -p build-logic/tests/generic-capability-missing-mapping help --stacktrace > /tmp/mapping.log 2>&1; then exit 1; fi
grep -Fq 'did not register a Durex primary capability' /tmp/mapping.log

git add -- build-logic/build.gradle.kts build-logic/src/main/groovy/durex.feature.*.gradle \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy \
  build-logic/src/main/groovy/com/github/durex/gradle/PersistenceExtension.groovy \
  build-logic/src/main/groovy/com/github/durex/gradle/internaltesting \
  build-logic/tests/features-smoke build-logic/tests/generic-capability-*
git commit -m "refactor: route Durex features through capability engine"
```

---

## Task 6 — Generic cache-safe diagnostics and `durexDoctor`

**Files:**
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexDependenciesTask.groovy`
- Create: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexProjectsTask.groovy`
- Modify: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexSettingsPlugin.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/diagnostics/DurexCapabilitiesTask.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/diagnostics/DurexDoctorTask.groovy`
- Create: `build-logic/src/main/groovy/com/github/durex/gradle/diagnostics/DurexDoctorValidator.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy`
- Modify fixtures: `build-bootstrap/tests/manifest-valid/**`, `build-bootstrap/tests/modules-auto/**`
- Create fixtures: `build-logic/tests/doctor-smoke/**`, `build-logic/tests/doctor-invalid/**`

- [ ] **Step 1: Binary bootstrap diagnostic tasks**

`DurexDependenciesTask` inputs:

```groovy
@Input abstract Property<Integer> getJavaVersion()
@Input abstract ListProperty<String> getPlatformLines()
@Input abstract ListProperty<String> getPluginLines()
@Input abstract ListProperty<String> getLibraryLines()
```

`DurexProjectsTask` input:

```groovy
@Input abstract ListProperty<String> getProjectLines()
```

Task actions print only inputs.

- [ ] **Step 2: Make settings diagnostics generic**

During `settingsEvaluated`, call `serviceProvider.get().snapshot()` once during configuration and build sorted lines from every entry in `platforms`, `plugins`, and `libraries`. Register binary tasks at root and set their input properties. Delete the current allowlist that prints only Spring/GraalVM/jOOQ plugin versions.

- [ ] **Step 3: Binary `durexCapabilities`**

Inputs:

```groovy
@Input abstract Property<String> getModuleKind()
@Input abstract Property<Integer> getJavaVersion()
@Input abstract ListProperty<String> getCapabilities()
@Input abstract ListProperty<String> getPlatformBindings()
```

Wire sorted providers/values during configuration. `Native: enabled` is derived from the capability list in the task action; there is no native input flag.

- [ ] **Step 4: Doctor validator and task**

`DurexDoctorValidator.validate(...)` returns a sorted `List<String>` of violations and checks:

```text
module kind present
enabled capabilities registered
plugin mappings reference registered capabilities
allowed module kinds
required capabilities present
no declared or reverse conflicts
dependency aliases exist
external plugin aliases exist
platform-managed dependency bindings have exact configuration:platform binding
no duplicate/inconsistent model values
```

`DurexDoctorTask` inputs:

```groovy
@Input abstract Property<String> getProjectPathInput()
@Input abstract Property<String> getModuleKind()
@Input abstract ListProperty<String> getCapabilities()
@Input abstract ListProperty<String> getPlatformBindings()
@Input abstract ListProperty<String> getViolations()
```

Task action prints `Configuration    OK` when empty and fails with a Durex Doctor report when non-empty. It reads no project/model/registry/service.

- [ ] **Step 5: Wire doctor inputs after DSL evaluation**

`DurexModulePlugin` registers the task during `apply`. Its `project.afterEvaluate` callback computes the validator result and sets task inputs only; it does not replace/add task actions.

- [ ] **Step 6: Exact doctor fixtures**

`doctor-smoke`:

```groovy
plugins { id 'durex.spring-service' }
durex {
    persistence { jooq() }
    lombok()
}
```

`doctor-invalid`:

```groovy
plugins { id 'durex.java-library' }
import com.github.durex.gradle.capability.CapabilityPluginRegistry
extensions.getByType(CapabilityPluginRegistry)
    .register('com.acme.durex.broken', 'missing-capability')
```

Doctor-invalid must fail because mapping references an unregistered capability; no engine invocation occurs first.

- [ ] **Step 7: Verify deterministic diagnostics and commit**

```bash
gradle -p build-bootstrap/tests/manifest-valid durexDependencies --stacktrace | tee /tmp/deps.log
gradle -p build-bootstrap/tests/modules-auto durexProjects --stacktrace | tee /tmp/projects.log
gradle -p build-logic/tests/doctor-smoke durexCapabilities durexDoctor --stacktrace | tee /tmp/doctor.log
if gradle -p build-logic/tests/doctor-invalid durexDoctor --stacktrace > /tmp/doctor-invalid.log 2>&1; then exit 1; fi
grep -Fq 'missing-capability' /tmp/doctor-invalid.log

git add -- build-bootstrap/src/main/groovy/com/github/durex/gradle/settings \
  build-bootstrap/tests/manifest-valid build-bootstrap/tests/modules-auto \
  build-logic/src/main/groovy/com/github/durex/gradle/diagnostics \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy \
  build-logic/tests/doctor-smoke build-logic/tests/doctor-invalid
git commit -m "feat: add cache-safe Durex diagnostics and doctor"
```

---

## Task 7 — Configuration-cache and parallel-build hardening

**Files:**
- Create: `build-logic/tests/config-cache-groovy/settings.gradle`
- Create: `build-logic/tests/config-cache-groovy/build.gradle`
- Create: `build-logic/tests/config-cache-kotlin/settings.gradle.kts`
- Create: `build-logic/tests/config-cache-kotlin/build.gradle.kts`
- Create: `build-logic/tests/parallel-multiproject/settings.gradle`
- Create: `build-logic/tests/parallel-multiproject/modules.toml`
- Create: `build-logic/tests/parallel-multiproject/java-lib/build.gradle`
- Create: `build-logic/tests/parallel-multiproject/spring-lib/build.gradle`
- Create: `build-logic/tests/parallel-multiproject/spring-service/build.gradle`
- Modify: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexSettingsPlugin.groovy`
- Modify: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexDependenciesTask.groovy`
- Modify: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexProjectsTask.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/catalog/DurexRegistryBridge.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/capability/CapabilityEngine.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/capability/CapabilityRegistry.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/capability/CapabilityPluginRegistry.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/diagnostics/DurexCapabilitiesTask.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/diagnostics/DurexDoctorTask.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy`
- Modify: `.github/workflows/durex-build-platform.yml`

The listed production files are the only lifecycle/state files changed in this hardening task. Do not change public DSL semantics here.

- [ ] **Step 1: Groovy configuration-cache fixture**

`settings.gradle`:

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
dependencyResolutionManagement { repositories { mavenCentral() } }
rootProject.name = 'config-cache-groovy'
```

`build.gradle`:

```groovy
plugins { id 'durex.spring-service' }
durex {
    persistence { jooq() }
    redis()
}
```

Run twice:

```bash
rm -rf build-logic/tests/config-cache-groovy/.gradle/configuration-cache
gradle -p build-logic/tests/config-cache-groovy durexDoctor --configuration-cache --stacktrace | tee /tmp/cc-g1.log
gradle -p build-logic/tests/config-cache-groovy durexDoctor --configuration-cache --stacktrace | tee /tmp/cc-g2.log
grep -Fq 'Reusing configuration cache.' /tmp/cc-g2.log
```

- [ ] **Step 2: Kotlin configuration-cache fixture**

`settings.gradle.kts` is the Kotlin equivalent of Step 1 with the same included builds/repository root. `build.gradle.kts`:

```kotlin
plugins { id("durex.spring-service") }
durex {
    persistence { jooq() }
    redis()
}
```

Run twice and require `Reusing configuration cache.` on the second run. Do not convert this fixture to Groovy.

- [ ] **Step 3: Settings diagnostic cache reuse**

```bash
rm -rf build-bootstrap/tests/modules-auto/.gradle/configuration-cache
gradle -p build-bootstrap/tests/modules-auto durexProjects --configuration-cache --stacktrace | tee /tmp/cc-s1.log
gradle -p build-bootstrap/tests/modules-auto durexProjects --configuration-cache --stacktrace | tee /tmp/cc-s2.log
grep -Fq 'Reusing configuration cache.' /tmp/cc-s2.log
```

- [ ] **Step 4: Parallel multi-project fixture**

`settings.gradle`:

```groovy
pluginManagement {
    includeBuild('../../../build-bootstrap')
    includeBuild('../..')
    repositories { gradlePluginPortal(); mavenCentral() }
}
plugins { id 'durex.settings' }
durexSettings {
    repositoryRoot.set(file('.'))
    dependencyManifest.set(file('../../../gradle/dependencies/durex.toml'))
    modulesManifest.set(file('modules.toml'))
}
dependencyResolutionManagement { repositories { mavenCentral() } }
rootProject.name = 'parallel-multiproject'
```

`modules.toml`:

```toml
[discovery]
mode = "manual"

[[module]]
name = "java-lib"
path = "java-lib"

[[module]]
name = "spring-lib"
path = "spring-lib"

[[module]]
name = "spring-service"
path = "spring-service"
```

Subproject builds:

```groovy
// java-lib/build.gradle
plugins { id 'durex.java-library' }

// spring-lib/build.gradle
plugins { id 'durex.spring-library' }
durex { persistence { jooq() } }

// spring-service/build.gradle
plugins { id 'durex.spring-service' }
durex { redis() }
dependencies {
    implementation project(':java-lib')
    implementation project(':spring-lib')
}
```

Run:

```bash
gradle -p build-logic/tests/parallel-multiproject build --parallel --stacktrace
```

- [ ] **Step 5: Apply configuration-cache diagnostics only to the listed lifecycle files**

Use Gradle's reported problem to map the fix:

```text
settings/root task captures -> DurexSettingsPlugin / DurexDependenciesTask / DurexProjectsTask
project diagnostic task captures -> DurexModulePlugin / DurexCapabilitiesTask / DurexDoctorTask
cross-build service capture -> DurexRegistryBridge
mutable shared capability state -> CapabilityEngine / CapabilityRegistry / CapabilityPluginRegistry
```

Do not suppress problems and do not add `--no-configuration-cache`.

- [ ] **Step 6: Add exact CI commands and commit**

Add the four commands from Steps 1-4 to `Durex Build Platform` CI. Then:

```bash
git add -- build-logic/tests/config-cache-groovy build-logic/tests/config-cache-kotlin \
  build-logic/tests/parallel-multiproject \
  build-bootstrap/src/main/groovy/com/github/durex/gradle/settings \
  build-logic/src/main/groovy/com/github/durex/gradle/catalog/DurexRegistryBridge.groovy \
  build-logic/src/main/groovy/com/github/durex/gradle/capability \
  build-logic/src/main/groovy/com/github/durex/gradle/diagnostics \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy \
  .github/workflows/durex-build-platform.yml
git commit -m "test: harden Durex plugins for configuration cache"
```

---

## Task 8 — Real regressions and CI evidence

**Files:**
- Modify: `.github/workflows/durex-build-platform.yml`
- Modify: `.github/workflows/spring-music.yml`
- Modify: `.github/workflows/spring-native-reference.yml`
- Verify without planned source edits: `core/**/build.spring.gradle`, `migration/spring-music/**`, `reference/spring-capabilities/**`, `reference/spring-native/**`, `build-logic/src/main/**`.

No new public/core API is introduced in this task. Any source failure here is treated as a defect in Tasks 1-7 and fixed in the owning file before final verification, not as a new design change.

- [ ] **Step 1: Source invariants**

```bash
grep -R -n 'DurexModuleState\|nativeEnabled' build-logic/src/main && exit 1 || true
grep -R -n 'service.get().\(library\|platform\|plugin\|javaVersion\)' build-logic/src/main && exit 1 || true
```

Search all production `durex.library` calls:

```bash
grep -R -n "durex.library" core reference migration
```

Every returned alias must be explicit-version/version-ref in the Durex manifest. Platform-managed production calls are a Task 3 defect and must already have been replaced by `durex.dependency`.

- [ ] **Step 2: Spring Music**

Update `spring-music.yml` to run:

```bash
gradle -p migration/spring-music :music:durexDoctor :music:compileJava :music:test --configuration-cache --stacktrace
```

Keep project graph, jOOQ schema smoke, and catalog-free descriptor checks.

- [ ] **Step 3: jOOQ schema**

`Durex Build Platform` keeps:

```bash
gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --configuration-cache --stacktrace
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/QMusic.java
test -f build-logic/tests/jooq-schema-smoke/build/generated-src/jooq/main/com/example/schema/tables/records/RMusic.java
```

- [ ] **Step 4: Spring capabilities/Kotlin DSL**

Add to `Durex Build Platform` or preserve in Spring Native JVM job:

```bash
gradle -p reference/spring-capabilities durexDoctor dependencies --configuration compileClasspath --configuration-cache --stacktrace
```

- [ ] **Step 5: Spring Native**

Update JVM job to run:

```bash
gradle -p reference/spring-native durexDoctor test processAot bootJar --configuration-cache --stacktrace
```

Preserve existing AOT HTTP smoke. Native job remains:

```bash
gradle -p reference/spring-native nativeTest --stacktrace
gradle -p reference/spring-native nativeCompile --stacktrace
```

and existing native `/hello` HTTP smoke.

- [ ] **Step 6: Required PR-head evidence**

All must be green:

```text
Durex Build Platform
  snapshot bridge + schema mismatch negative
  dependency API + invalid library negative
  capability kernel positive/negative
  built-in + generic capability DSL
  doctor positive/negative
  Groovy/Kotlin configuration-cache reuse
  settings configuration-cache reuse
  parallel multi-project build
  jOOQ schema

Spring Music Migration
  project graph
  :music:durexDoctor
  compile
  CRUD/runtime integration
  catalog-free descriptors

Spring Native Reference
  Spring capabilities resolution
  JVM tests
  durexDoctor
  AOT HTTP smoke
  nativeTest
  nativeCompile
  native HTTP smoke
```

Do not describe legacy Quarkus/Shared Utils workflows as green if they remain unrelated/red.

- [ ] **Step 7: Final scope check and commit**

Diff against base must not add business features, root cutover, publishing, capability TOML schema, or wholesale convention-to-binary rewrites.

```bash
git add -- .github/workflows/durex-build-platform.yml \
  .github/workflows/spring-music.yml .github/workflows/spring-native-reference.yml
git commit -m "ci: verify Durex Plugin Core v2"
```

---

## Execution Notes

- Execute Tasks 1-8 in order; later tasks rely on interfaces introduced earlier.
- Prefer existing Gradle functional fixtures; do not add a new unit-test framework solely for this refactor.
- Intentionally failing fixtures must invert command status and grep Durex-specific messages in CI.
- Configuration-cache failures are fixed in the exact lifecycle files listed in Task 7; acceptance commands are not weakened.
- If a built-in capability needs behavior beyond dependency bindings or external plugin application, stop and compare against the spec before adding a new generic kernel hook; do not add feature-name conditionals to `CapabilityEngine`.
- `com.acme.durex.fixture` is an internal dogfood-only plugin proving the third-party-style mapping contract; external artifact publication/classloader packaging remains outside v2.
