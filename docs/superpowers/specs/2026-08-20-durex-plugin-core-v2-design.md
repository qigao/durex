# Durex Plugin Core v2 Design

## 1. Purpose

Durex Plugin Core v2 turns the current Gradle conventions into a reusable build-platform kernel that can eventually be published independently while remaining dogfooded inside the Durex monorepo first.

The goal is not to add more framework features. The goal is to make existing module types, features, dependency access, diagnostics, and build state predictable, extensible, configuration-cache-safe, and independent from Spring/jOOQ-specific bootstrap assumptions.

This design builds on the already merged Durex build platform:

- Durex-owned TOML dependency manifests;
- `durex.settings`;
- automatic/manual/strict module discovery;
- build-local dependency registries;
- `durex.java-library`, `durex.spring-library`, `durex.spring-service`;
- JPA/JDBC/jOOQ/Redis/Native/Lombok feature DSL;
- `durex.library(alias)`;
- jOOQ schema/codegen convention;
- Spring Music and Spring Native reference builds.

Plugin Core v2 must preserve the existing user-facing DSL where practical while replacing duplicated feature behavior with one typed capability kernel.

## 2. Product Direction

Durex Gradle plugins are designed as a potentially publishable product, but v2 is implemented and validated inside this monorepo before any external publication contract is declared stable.

This means the design must avoid assumptions that only hold when:

- bootstrap and build logic share one classloader;
- the repository has fixed Spring/jOOQ aliases;
- all features are first-party;
- diagnostics can inspect live `Project` state at task execution time;
- Gradle Version Catalogs are present.

Publication itself, semantic-versioning policy, plugin portal publishing, and external repository examples are outside v2 scope.

## 3. Core Principles

1. **TOML describes dependency facts, not build behavior.**
   - versions, platforms, libraries, Gradle plugins remain in TOML;
   - capability prerequisites, conflicts, dependency bindings, and module compatibility remain plugin code.

2. **Feature plugins declare capabilities; the kernel executes capabilities.**
   - feature plugins do not each implement validation, dependency wiring, state mutation, and diagnostics.

3. **One source of module truth.**
   - module kind, enabled capabilities, and configuration-scoped platform bindings live in one model;
   - derived flags such as `nativeEnabled` do not become independent state.

4. **Configuration scope is explicit.**
   - `implementation:spring` and `api:spring` are distinct bindings.

5. **Cross-build boundaries use neutral snapshots.**
   - no public reliance on Groovy dynamic calls against bootstrap implementation classes from another included build.

6. **Diagnostics are generic and task inputs are cache-safe.**
   - no diagnostic task action captures `Project`, extensions, services, engines, or mutable live state.

7. **Typed first-party DSL + generic third-party extension point.**
   - first-party convenience methods remain ergonomic;
   - new capabilities do not require editing the central Durex extension.

8. **Gradle remains the resolver and execution engine.**
   - Durex does not reimplement Maven/Gradle dependency conflict resolution, task scheduling, variants, or repositories.

## 4. Target Architecture

```text
                    Public Durex DSL
                           │
          ┌────────────────┴────────────────┐
          │                                 │
   typed first-party facade         capability(pluginId)
          │                                 │
          └────────────────┬────────────────┘
                           ▼
                    Feature Plugin
                           │
                           ▼
                    CapabilitySpec
                           │
                           ▼
                   CapabilityEngine
                 ┌─────────┴─────────┐
                 ▼                   ▼
          DurexModuleModel     DependencyBridge
                                     │
                                     ▼
                         DependencyCatalogSnapshot
                                     │
                                     ▼
                              Gradle Resolver
```

Settings/bootstrap side:

```text
gradle/dependencies/*.toml
        ↓
DependencyManifestLoader
        ↓
DependencyRegistryService
        ↓ neutral snapshot
DurexRegistryBridge
        ↓
DependencyCatalogSnapshot
```

## 5. Capability Model

### 5.1 `CapabilitySpec`

`CapabilitySpec` is an immutable description of one build capability.

Conceptual API:

```java
CapabilitySpec {
    String id;
    Set<ModuleKind> allowedModules;
    Set<String> requires;
    Set<String> conflicts;
    List<DependencyBinding> dependencies;
    List<String> externalPluginAliases;
}
```

The spec owns no mutable project state and performs no Gradle mutation by itself.

### 5.2 `DependencyBinding`

A dependency binding is configuration-aware:

```java
DependencyBinding {
    String configuration;
    String libraryAlias;
}
```

Examples:

```text
jooq
  implementation -> spring-jooq

lombok
  compileOnly         -> lombok
  annotationProcessor -> lombok
```

### 5.3 Built-in capability definitions

First-version built-ins preserve current behavior:

```text
jpa
  allowed: SPRING_LIBRARY, SPRING_SERVICE
  implementation -> spring-jpa

jdbc
  allowed: SPRING_LIBRARY, SPRING_SERVICE
  implementation -> spring-jdbc

jooq
  allowed: SPRING_LIBRARY, SPRING_SERVICE
  implementation -> spring-jooq

redis
  allowed: SPRING_LIBRARY, SPRING_SERVICE
  implementation -> spring-redis

native
  allowed: SPRING_SERVICE
  external plugin alias -> graalvm-native

lombok
  compileOnly -> lombok
  annotationProcessor -> lombok
```

No new Kafka/Mongo/Security/Flyway capabilities are added in v2.

## 6. Capability Registry

`CapabilityRegistry` owns registered capability descriptions inside a project/plugin runtime.

Required semantics:

- capability IDs are unique;
- duplicate registration is an error unless it is the exact same immutable spec instance/definition according to an explicitly defined equality rule;
- lookup by capability ID is deterministic;
- registry registration is complete before a capability is enabled;
- registry itself contains no dependency versions.

Built-in capabilities are registered by Durex core.

Third-party capability plugins may register their own `CapabilitySpec` before enabling it.

The central Durex core must not require modification to know every future capability.

## 7. Capability Engine

`CapabilityEngine` is the only implementation path that enables capabilities.

Conceptual operation:

```text
engine.enable(capabilityId)
    ↓
lookup CapabilitySpec
    ↓
return if already enabled
    ↓
validate module kind
    ↓
resolve + enable required capabilities
    ↓
validate conflicts
    ↓
apply external Gradle plugins
    ↓
add configuration-aware dependencies/platforms
    ↓
record capability in DurexModuleModel
```

### 7.1 Idempotency

Enabling the same capability multiple times is a no-op after the first successful activation.

This applies whether activation comes from:

- typed DSL;
- generic `capability(pluginId)` API;
- direct Gradle plugin application;
- another capability's `requires` relationship.

### 7.2 Requirements

`requires` means the required capability must also be enabled.

The engine may recursively enable required capabilities, but must detect cycles and fail with a Durex-specific configuration error.

Example error:

```text
Durex configuration error
Project: :example
Capability: foo
Problem: capability requirement cycle detected: foo -> bar -> foo
```

### 7.3 Conflicts

A capability cannot be enabled if any declared conflict is already active. Enabling a later conflicting capability also fails.

Error messages identify:

- project path;
- requested capability;
- conflicting capability;
- module type where relevant.

### 7.4 Module-type validation

A capability with non-empty `allowedModules` may only run after a compatible module type is known.

Example:

```text
Durex configuration error
Project: :example
Capability: native
Module type: JAVA_LIBRARY
Problem: capability 'native' is not supported by JAVA_LIBRARY
Allowed module types: SPRING_SERVICE
```

Capabilities with an empty `allowedModules` set are module-type agnostic.

## 8. Public DSL

### 8.1 Existing typed DSL remains

The existing first-party API remains recognizable:

```kotlin
durex {
    persistence {
        jpa()
        jooq()
    }
    redis()
    nativeImage()
    lombok()
}
```

These methods are facades only. They do not implement validation or dependency wiring.

The call chain is conceptually:

```text
jooq()
  -> apply `durex.feature.jooq`
  -> feature plugin registers/locates JOOQ spec
  -> CapabilityEngine.enable("jooq")
```

### 8.2 Generic third-party capability API

Durex also exposes:

```kotlin
durex {
    capability("com.acme.durex.foo")
}
```

The argument is a Gradle plugin ID, not a capability ID.

The generic API:

1. applies the requested Gradle plugin;
2. that plugin registers and enables its own capability through the kernel;
3. Durex core does not need to know the third-party capability ID in advance.

Applying a plugin that does not register/enable a Durex capability may fail `durexDoctor`, but the generic API itself must surface a clear configuration error if the plugin cannot be resolved or does not integrate as required by the final implementation contract.

### 8.3 Avoid unbounded facade growth

First-party ergonomic methods may exist for built-ins, but the extension must not become the implementation registry for every capability.

Adding Kafka later must not require changing validation, state, dependency wiring, or diagnostics core.

## 9. Module Model

`DurexModuleState` is replaced by a typed managed model, conceptually `DurexModuleModel`.

Required properties:

```java
abstract class DurexModuleModel {
    abstract Property<ModuleKind> getModuleKind();
    abstract SetProperty<String> getCapabilities();
    abstract SetProperty<String> getPlatformBindings();
}
```

Exact implementation may use additional internal properties when needed for lazy Gradle wiring, but these are the semantic facts.

### 9.1 Module kind

Exactly one module kind is allowed:

```text
JAVA_LIBRARY
SPRING_LIBRARY
SPRING_SERVICE
```

Claiming the same kind repeatedly is idempotent. Claiming a different kind fails.

### 9.2 Capabilities

The capabilities set is the sole source of truth for enabled features.

There is no separate `nativeEnabled` boolean.

`Native: enabled` in diagnostics is derived from `capabilities.contains("native")`.

### 9.3 Platform bindings

Platform bindings are configuration-scoped and stored canonically, for example:

```text
api:spring
implementation:spring
testImplementation:spring
```

The internal representation may become a typed `PlatformBinding` value object if Gradle managed-property constraints make that preferable, but diagnostics and semantics must preserve both fields explicitly.

## 10. Dependency Access v2

### 10.1 Problem with current `durex.library(alias)`

Current syntax such as:

```groovy
api durex.library('jackson-annotations')
```

cannot know that the returned notation will later be inserted into `api`. Therefore it cannot reliably verify that the required platform is active on the same configuration.

### 10.2 Explicit-version library escape hatch

`durex.library(alias)` remains supported for libraries with their own `version` or `version.ref`.

Example:

```groovy
dependencies {
    compileOnly durex.library('javax-cdi')
}
```

Calling `durex.library(alias)` for a platform-managed library is invalid in v2 and must produce a clear Durex error directing the user to configuration-aware dependency wiring.

### 10.3 Configuration-aware dependency API

For platform-managed or general managed insertion:

```groovy
durex {
    dependency('api', 'jackson-annotations')
    dependency('api', 'jackson-databind')
}
```

Conceptually:

```text
dependency(configuration, alias)
    ↓
resolve library
    ↓
if platform-managed: ensure configuration:platform binding
    ↓
add dependency to that configuration
```

Feature/module plugins always use the same internal `DependencyBridge.add(configuration, alias)` path.

There must not be a separate special wiring mechanism for capabilities.

### 10.4 Platform de-duplication

A platform is added once per `(configuration, platformAlias)` pair.

Adding multiple Spring-managed libraries to `implementation` yields one Durex platform binding for `implementation:spring`.

## 11. Registry Snapshot Boundary

### 11.1 Motivation

The current included-build integration relies on dynamic method calls against a BuildService object whose implementation classes come from bootstrap build logic.

That has worked, but it is not a desirable long-term product boundary.

### 11.2 Neutral snapshot protocol

`DependencyRegistryService` exposes a neutral snapshot consisting only of JDK/Gradle-safe primitive container values:

```text
String
Integer
Boolean
Map
List
```

Conceptual snapshot:

```text
schemaVersion: 1
javaVersion: 25
platforms: {...}
libraries: {...}
plugins: {...}
```

No bootstrap-specific `LibrarySpec`, `PluginSpec`, `PlatformSpec`, or `DependencyRegistry` object crosses the build/plugin classloader boundary.

### 11.3 `DurexRegistryBridge`

Build logic reads the neutral snapshot and constructs its own immutable local model:

```text
DependencyRegistryService.snapshot()
        ↓
DurexRegistryBridge
        ↓
DependencyCatalogSnapshot
```

`DependencyCatalogSnapshot` is owned by build-logic/core, not by bootstrap.

### 11.4 Snapshot schema version

The snapshot contains `schemaVersion`.

An unsupported snapshot schema is a hard Durex bootstrap error with both expected and actual versions.

The v2 schema starts at version `1`.

## 12. Bootstrap Must Be Ecosystem-Neutral

`durex.settings` and bootstrap diagnostics must not hard-code knowledge of:

- `spring`;
- `spring-boot`;
- `graalvm-native`;
- `jooq-codegen`;
- any future application capability.

Bootstrap understands only generic manifest concepts:

```text
java
versions
platforms
libraries
plugins
modules
```

`durexDependencies` enumerates manifest contents generically in stable sorted order.

Representative output:

```text
Durex Dependencies

Java
  25

Platforms
  spring -> org.springframework.boot:spring-boot-dependencies:4.1.0

Plugins
  graalvm-native -> 1.1.1
  jooq-codegen -> 3.21.5
  spring-boot -> 4.1.0
```

Libraries may be shown in full or behind an optional detail flag/task variant if output size becomes excessive; v2 must at minimum keep the generic model and stable machine-testable output.

## 13. Diagnostic Tasks

The v2 diagnostic surface is:

```text
durexDependencies
durexProjects
durexCapabilities
durexDoctor
```

### 13.1 Binary/cache-safe tasks

These become explicit task classes with Gradle input properties.

Representative shape:

```java
abstract class DurexCapabilitiesTask extends DefaultTask {
    @Input
    abstract Property<String> getModuleKind();

    @Input
    abstract Property<Integer> getJavaVersion();

    @Input
    abstract ListProperty<String> getCapabilities();

    @Input
    abstract ListProperty<String> getPlatformBindings();
}
```

Task actions do not access live `Project`, extension, BuildService, `CapabilityEngine`, or mutable registry objects.

The same rule applies to settings-level diagnostics where technically applicable. Values are materialized/wired during configuration through providers and task inputs rather than fetched ad hoc during execution.

### 13.2 Stable ordering

Capabilities, platforms, libraries, plugins, and projects must be printed in deterministic sorted order.

Tests assert the actual canonical ordering. The earlier `jooq,jpa,redis` CI mismatch is treated as evidence that output ordering is part of the diagnostic contract and must not be implicit.

## 14. `durexDoctor`

`durexDoctor` is a consistency validator, not another dependency resolver.

It validates the Durex model and configuration contract:

- dependency manifest snapshot is available and schema-compatible;
- module kind is selected for Durex module projects;
- every enabled capability is registered;
- capability module-kind constraints are satisfied;
- required capabilities are present;
- no conflicts exist;
- required dependency aliases exist;
- required external plugin aliases exist;
- required platform bindings exist for dependency bindings;
- project model contains no duplicate/inconsistent state.

Representative output:

```text
Durex Doctor — :music

Module
  type            SPRING_SERVICE

Capabilities
  jooq            OK
  lombok          OK

Platforms
  implementation:spring      OK
  testImplementation:spring  OK

Manifest         OK
Configuration    OK
```

Any failure makes the task fail with a Durex-specific error.

The task is expected to become the main CI consistency entry point, reducing custom grep logic over time.

## 15. Error Model

Kernel errors share a common prefix/category:

```text
Durex configuration error
```

Where applicable errors include:

```text
Project
Module type
Capability
Dependency alias
Configuration
Problem
Expected/allowed values
```

Bootstrap/snapshot failures retain a distinct bootstrap/manifest category where that makes diagnosis clearer.

Errors must not expose internal Groovy missing-method errors, classloader casts, NPEs, or opaque Gradle exceptions when Durex can provide the relevant domain context.

## 16. Convention Plugins vs Binary Plugins

v2 does not require rewriting every existing precompiled convention script immediately.

The following kernel pieces should become binary/typed implementation code first:

- `CapabilitySpec`;
- `CapabilityRegistry`;
- `CapabilityEngine`;
- `DurexModuleModel`;
- `DependencyBridge`;
- `DurexRegistryBridge`;
- snapshot model;
- diagnostic task classes;
- doctor validation.

Existing convention plugins such as:

```text
durex.spring-service.gradle
durex.feature.jooq.gradle
```

may remain temporarily as thin composition layers that delegate to the kernel.

Once v2 is stable, a later bounded/refactoring phase may convert individual convention scripts into binary plugins such as `DurexSpringServicePlugin` and `DurexJooqFeaturePlugin`.

That conversion is not required for Plugin Core v2 acceptance.

## 17. Configuration Cache and Parallel Build Requirements

Plugin Core v2 treats Gradle 9.1 configuration-cache compatibility as a hard acceptance requirement.

Required positive checks include two consecutive invocations where the second one reuses the cache, for representative builds such as:

```bash
gradle durexDoctor --configuration-cache
gradle durexDoctor --configuration-cache
```

The exact fixture paths will be defined in the implementation plan.

Also required:

```bash
gradle build --parallel
```

for a representative multi-project fixture.

Tests must cover:

- Groovy DSL;
- Kotlin DSL;
- included build bootstrap;
- multi-project build;
- automatic discovery;
- manual discovery;
- Java library;
- Spring library/service;
- JPA+jOOQ coexistence;
- Native capability;
- generic third-party capability registration/application;
- configuration-aware platform binding;
- doctor success/failure;
- snapshot schema mismatch.

No task may rely on accidental mutable global state that becomes incorrect under parallel configuration/execution.

## 18. Compatibility and Migration

### 18.1 User DSL

The following remain supported:

```text
persistence.jpa()
persistence.jdbc()
persistence.jooq()
redis()
nativeImage()
lombok()
```

The implementation changes behind them.

### 18.2 `durex.library`

Behavior intentionally tightens:

- explicit-version/version-ref libraries: supported;
- platform-managed libraries: rejected with migration guidance to `durex.dependency(configuration, alias)`.

Existing migrated descriptors using platform-managed `durex.library(...)` must be converted in the same implementation series so the repository remains green.

### 18.3 TOML

No capability schema is added to TOML.

The existing dependency manifest structure remains conceptually compatible.

Snapshot schema is an internal bootstrap/build-logic bridge contract, distinct from TOML schema.

## 19. Non-Goals

Plugin Core v2 does not include:

- Kafka/Mongo/Security/Flyway/OpenAPI/Testcontainers feature expansion;
- root Gradle cutover/removal of all legacy scripts;
- publication to Gradle Plugin Portal;
- repository policy redesign;
- custom dependency resolution;
- custom variant engine;
- replacing Gradle tasks/configurations;
- putting capability behavior into TOML;
- rewriting every convention plugin to binary form in one change.

## 20. Acceptance Criteria

Plugin Core v2 is complete when all of the following are true:

1. All current built-in features execute through one `CapabilityEngine` path.
2. Feature plugins no longer independently implement module validation, dependency wiring, or state tracking.
3. Module state has one typed source of truth for kind, capabilities, and configuration-scoped platform bindings.
4. `nativeEnabled`-style duplicate state is gone.
5. Platform bindings are correct per configuration.
6. `durex.library` cannot silently bypass platform/configuration correctness.
7. Bootstrap/build-logic cross-build access uses a neutral versioned snapshot, not bootstrap implementation classes.
8. Bootstrap diagnostics contain no Spring/jOOQ/GraalVM special cases.
9. `durexDependencies`, `durexProjects`, and `durexCapabilities` have deterministic generic output.
10. `durexDoctor` validates Durex configuration and fails invalid builds clearly.
11. Representative Groovy and Kotlin DSL builds pass.
12. Representative builds pass and reuse Gradle 9.1 configuration cache.
13. Representative multi-project builds pass under `--parallel`.
14. Spring Music migration remains green.
15. Spring Native JVM/AOT/native behavior remains green.
16. Existing jOOQ schema/codegen behavior remains green.
17. No new business feature category is introduced as part of this core refactor.

## 21. Resulting Extension Model

After v2, adding a future first-party feature should normally require only:

```text
1. dependency/plugin aliases in Durex TOML if new coordinates are needed
2. one CapabilitySpec
3. one thin feature plugin/facade entry point
4. focused functional tests
```

It should not require modifying:

```text
CapabilityEngine
module state semantics
dependency platform logic
diagnostics core
error formatting core
settings bootstrap
```

That is the primary maintainability outcome of Plugin Core v2.