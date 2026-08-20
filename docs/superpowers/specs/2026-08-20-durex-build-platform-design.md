# Durex Build Platform Design

Date: 2026-08-20
Status: Proposed
Branch: `design/durex-build-platform`

## 1. Purpose

Durex should make Gradle modules declare only two things:

1. what kind of module they are;
2. which additional capabilities they need.

Global dependency versions, BOMs, framework wiring, plugin versions, Java/toolchain policy, test conventions, native-image setup, and module discovery should be owned by Durex build infrastructure instead of repeated in module build files or registered manually in the root project.

The target module experience is:

```gradle
plugins {
    id 'durex.spring-service'
}

dependencies {
    implementation project(':shared-common')
    implementation project(':music-json')
    implementation project(':music-repo')
}

durex {
    persistence {
        jooq()
    }
}
```

The target root settings file is intentionally small and stable:

```gradle
pluginManagement {
    includeBuild('build-bootstrap')
    includeBuild('build-logic')
}

plugins {
    id 'durex.settings'
}
```

No module should need to be manually registered in the root settings file during normal development.

## 2. Goals

- Replace Gradle Version Catalogs as the final global dependency API.
- Keep TOML as the human-editable dependency manifest format, but make its schema and lifecycle Durex-owned.
- Preserve Gradle BOM/platform and dependency resolution instead of reimplementing them.
- Introduce module-type plugins with sensible defaults.
- Introduce feature plugins for optional capabilities such as JPA, jOOQ, Redis, Native Image, and legacy Lombok.
- Keep jOOQ runtime usage separate from jOOQ schema/code generation.
- Support automatic module discovery and explicit/manual module declaration in one unified project registry.
- Remove routine root-level module registration.
- Fail fast on ambiguous modules, conflicting module types, invalid manifests, and invalid feature combinations.
- Allow incremental migration from the current `gradle/library/*.gradle`, legacy root Gradle build, and alternate Spring build descriptors.

## 3. Non-goals

The first implementation will not:

- replace Gradle dependency resolution;
- implement a custom Maven repository resolver;
- build a Durex mega-BOM;
- invent a full TOML replacement for Gradle build scripts;
- move business project dependencies such as `project(':music-repo')` into TOML;
- support profiles, environments, repository declarations, classifiers, dependency substitutions, arbitrary exclusions, or bundles in the Durex dependency manifest;
- require every module to have a descriptor file.

## 4. Architecture

The build platform has two build-infrastructure layers:

```text
gradle/dependencies/*.toml
          |
          +--------------------+
          |                    |
          v                    v
   root Gradle build      build-logic build
          |                    |
   durex.settings      durex.build-logic-settings
          |                    |
 DependencyRegistry      DependencyRegistry
 ProjectRegistry               |
          |                    v
          |              build-logic plugins
          |                    |
          +----------+---------+
                     v
                  modules
```

The TOML files are the single source of dependency/plugin truth. Registries are **per Gradle build**, not globally shared across a composite build. This is required because included builds are isolated Gradle builds and do not share settings, version catalogs, repositories, custom properties, or build services with the root build.

### 4.1 `build-bootstrap`

`build-bootstrap` is deliberately small. It may depend on:

- Gradle API;
- Groovy/Kotlin runtime already required by the build implementation;
- one mature TOML parser.

It must not depend on Spring, Spring Boot, jOOQ, GraalVM, Jackson, Lombok, or application/runtime libraries.

It provides reusable binary plugins and model code:

```text
durex.settings
durex.build-logic-settings
durex.build-logic
ManifestLoader
ManifestValidator
DependencyRegistry model
ProjectRegistry model
```

Its responsibilities are:

- parse Durex TOML manifests;
- resolve `include` files;
- validate manifest schema and references;
- build read-only dependency registry instances;
- discover modules and build the root project registry;
- register root projects with Gradle;
- expose registry instances as build-scoped services inside the Gradle build in which the plugin is running;
- bootstrap `build-logic` from the same TOML source without hard-coding application dependency/plugin versions there.

### 4.2 `build-logic`

`build-logic` contains module-type and feature convention plugins. It is an independent included build, so it initializes its own dependency registry from the same root Durex manifest.

Its settings file uses bootstrap:

```gradle
pluginManagement {
    includeBuild('../build-bootstrap')
}

plugins {
    id 'durex.build-logic-settings'
}
```

Its build file uses a bootstrap project plugin:

```gradle
plugins {
    id 'groovy-gradle-plugin'
    id 'durex.build-logic'
}
```

`durex.build-logic-settings` loads and validates the same `gradle/dependencies/durex.toml` file for the `build-logic` Gradle build. `durex.build-logic` consumes that build-local registry to add implementation dependencies for external Gradle plugin APIs such as Spring Boot, jOOQ codegen, and GraalVM Native.

This means:

```text
TOML version source: one
registry instances: one per Gradle build
```

No cross-composite-build Shared Build Service is assumed.

Representative project plugins remain:

```text
durex.java-base
durex.spring-base

durex.java-library
durex.spring-library
durex.spring-service

durex.feature.jpa
durex.feature.jdbc
durex.feature.jooq
durex.feature.redis
durex.feature.native
durex.feature.lombok

durex.jooq-schema
```

## 5. Dependency Manifest

The final dependency files live under:

```text
gradle/dependencies/
    durex.toml
    spring.toml
    database.toml
    test.toml
    utils.toml
```

`durex.toml` is the root manifest:

```toml
include = [
  "spring.toml",
  "database.toml",
  "test.toml",
  "utils.toml"
]

[java]
version = 25
```

The first-version schema contains only:

```text
versions
platforms
libraries
plugins
```

Example:

```toml
[versions]
spring-boot = "4.1.0"
graal-native = "1.1.1"
jooq = "3.21.5"

[platforms.spring]
module = "org.springframework.boot:spring-boot-dependencies"
version.ref = "spring-boot"

[libraries.spring-web]
module = "org.springframework.boot:spring-boot-starter-webmvc"
platform = "spring"

[libraries.spring-jooq]
module = "org.springframework.boot:spring-boot-starter-jooq"
platform = "spring"

[libraries.jooq-meta]
module = "org.jooq:jooq-meta"
version.ref = "jooq"

[plugins.spring-boot]
id = "org.springframework.boot"
module = "org.springframework.boot:spring-boot-gradle-plugin"
version.ref = "spring-boot"

[plugins.graalvm-native]
id = "org.graalvm.buildtools.native"
module = "org.graalvm.buildtools:native-gradle-plugin"
version.ref = "graal-native"
```

### 5.1 Version ownership rules

A normal library must have exactly one version owner:

- `version`;
- `version.ref`;
- `platform`.

`platform` is mutually exclusive with `version` and `version.ref` in the first version. This avoids ambiguous BOM override behavior.

Libraries managed by Spring Boot therefore omit explicit versions:

```toml
[libraries.spring-jooq]
module = "org.springframework.boot:spring-boot-starter-jooq"
platform = "spring"
```

Durex turns this into standard Gradle declarations:

```text
implementation platform(Spring Boot BOM)
implementation spring-boot-starter-jooq
```

Gradle remains responsible for actual dependency conflict resolution.

### 5.2 Manifest merging and validation

Includes are explicit files only in the first version; globs are not supported.

The loader fails on:

- include cycles;
- missing include files;
- duplicate version/platform/library/plugin ids;
- unknown `version.ref`;
- unknown platform ids;
- malformed module coordinates;
- libraries without a version owner;
- libraries with both platform and explicit version ownership;
- duplicate plugin ids;
- unsupported keys/sections.

No later file silently overrides an earlier file.

Errors must identify the source file, object id, problem, and known valid alternatives where useful.

## 6. Bootstrap Lifecycle

### 6.1 Root build

Root settings evaluation is:

```text
pluginManagement includes build-bootstrap/build-logic
        |
        v
apply durex.settings
        |
        +-- load Durex dependency manifest
        +-- validate
        +-- create root DependencyRegistry
        +-- load modules.toml
        +-- discover/manual-merge projects
        +-- create ProjectRegistry
        +-- settings.include(...) projects
        +-- register root-build services
        |
        v
project build scripts
        |
        v
Durex module/feature plugins consume root-build registry
```

### 6.2 `build-logic` included build

`build-logic` is isolated and performs its own bootstrap:

```text
build-logic/settings.gradle
        |
        v
apply durex.build-logic-settings
        |
        +-- load same root Durex manifest
        +-- validate
        +-- create build-logic DependencyRegistry
        +-- register build-logic service
        |
        v
build-logic/build.gradle
        |
        v
apply durex.build-logic
        |
        +-- add external plugin implementation dependencies
        |
        v
compile Durex convention plugins
```

The same source files are parsed once per participating Gradle build. No dependency version is duplicated in `build-logic/build.gradle`.

The only unavoidable hard-coded third-party version in bootstrap is the TOML parser implementation version. It is a bootstrap implementation detail, not application dependency policy.

## 7. Module Types

A project may declare exactly one Durex module type.

### 7.1 `durex.java-library`

Defaults:

- `java-library`;
- Java 25 toolchain/release policy;
- common compiler conventions;
- JUnit/test conventions.

### 7.2 `durex.spring-library`

Defaults:

- Java library baseline;
- Spring dependency platform;
- Spring core;
- Spring test support.

It does not imply MVC, Actuator, executable Boot application behavior, or native image.

### 7.3 `durex.spring-service`

Defaults:

- Java 25;
- Spring Boot plugin;
- Spring Boot BOM/platform;
- Spring core;
- Spring MVC;
- validation;
- observability/Actuator;
- Spring/JUnit/Mockito testing;
- MVC testing;
- application/Boot packaging conventions.

Optional persistence/cache/native capabilities are not automatically enabled.

Applying two Durex module types to one project is a hard error.

## 8. Feature DSL

The user-facing DSL expresses only optional capabilities:

```gradle
durex {
    persistence {
        jpa()
        jooq()
    }

    redis()
    nativeImage()
}
```

The DSL routes capabilities to independent feature plugins. It is not a second dependency engine.

```text
persistence.jpa()  -> durex.feature.jpa
persistence.jdbc() -> durex.feature.jdbc
persistence.jooq() -> durex.feature.jooq
redis()            -> durex.feature.redis
nativeImage()      -> durex.feature.native
lombok()           -> durex.feature.lombok
```

Feature activation is idempotent. JPA and jOOQ are deliberately compatible and may be enabled together.

### 8.1 Feature prerequisites

First-version policy:

- JPA/JDBC/jOOQ/Redis: valid on Spring library or Spring service modules where the corresponding feature semantics make sense;
- Native Image: valid only on Spring service modules;
- Lombok: optional legacy feature, never a global default.

Invalid combinations fail with a Durex-specific message identifying the module type and requested feature.

## 9. jOOQ Boundary

`durex.feature.jooq` means the runtime module uses Spring-managed jOOQ. It adds runtime dependencies and integration only.

`durex.jooq-schema` remains a separate code-generation/schema plugin. It owns:

- official jOOQ codegen plugin;
- DDLDatabase configuration;
- generated source wiring;
- matcher naming such as `Q*` and `R*`;
- schema-specific code generation settings.

Business repository modules depend on generated schema modules; they do not own code generation configuration.

## 10. Module Discovery

Durex supports automatic and manual discovery simultaneously.

The default is:

> automatic discovery for normal modules; manual declarations for exceptions and control.

Module discovery is configured independently from dependency manifests:

```text
gradle/modules.toml
```

Example:

```toml
[discovery]
mode = "auto"
roots = ["core"]

exclude = [
  "core/legacy"
]

[[module]]
name = "shared-common"
path = "core/shared/jakarta/common"

[[module]]
name = "admin-tool"
path = "tools/admin"
```

### 10.1 Discovery modes

First-version modes:

- `auto`: automatic scanning plus manual include/override; default;
- `manual`: no scanning, only explicitly declared modules;
- `strict-auto`: automatic scanning, but any ambiguous/unmappable module fails rather than falling back silently.

### 10.2 Automatic discovery

Automatic discovery scans configured roots for supported build files:

```text
build.gradle
build.gradle.kts
```

Build/output/source directories and configured exclusions are ignored.

The initial intended naming conventions include:

```text
core/music                    -> :music
core/shared/utils             -> :shared-utils
core/schema/music/json        -> :music-json
core/schema/music/entity      -> :music-entity
core/schema/music/repo        -> :music-repo
```

The naming function must be deterministic and covered by functional tests.

### 10.3 Manual include and override

A manual module declaration may:

- include a project outside auto-discovery roots;
- assign an explicit logical project name;
- override the logical name inferred for an automatically discovered path.

Automatic and manual modules are normalized into one `ProjectRegistry`; there are not two independent registries.

The effective precedence is:

```text
manual exclude
    > manual include/override
    > automatic discovery
    > default naming convention
```

Duplicate logical project names or conflicting physical paths are hard errors.

### 10.4 Root registration

Normal module additions require no edit to root `settings.gradle`.

Adding:

```text
core/user/build.gradle
```

under an automatic discovery root is enough for the next Gradle invocation to register `:user`, subject to naming rules and validation.

This capability replaces the existing root-level `modules.gradle` discovery mechanism in the final architecture.

## 11. Registries and Build Isolation

### 11.1 `DependencyRegistry`

Contains resolved and validated:

- Java/toolchain policy;
- versions;
- platforms;
- libraries;
- Gradle plugins.

A registry instance is local to one Gradle build. The root build and `build-logic` included build each create a registry from the same Durex TOML source.

### 11.2 `ProjectRegistry`

Exists in the root build and contains normalized project specifications:

```text
logical Gradle path
physical directory
source: AUTO or MANUAL
optional override metadata
```

### 11.3 Build-scoped services

Registry parsing is not repeated per project. Within a given Gradle build, the settings/bootstrap plugin parses once and exposes the resulting immutable model through a build-scoped/shared service.

Cross-build sharing is explicitly not assumed because Gradle included builds are isolated.

## 12. Repositories

Durex module/feature plugins do not inject Maven repositories.

Repository policy remains settings-level build policy through `dependencyResolutionManagement` or the corresponding Durex settings configuration.

```text
settings/build policy -> where dependencies come from
Durex manifest        -> what versions/dependencies/plugins exist
module/feature plugin -> how a module is assembled
module build file     -> business project dependencies and local configuration
```

## 13. Diagnostics

Durex should provide explicit diagnostics so abstraction does not hide build behavior.

Representative project diagnostic:

```text
:music:durexCapabilities
```

prints:

```text
Type: SPRING_SERVICE
Java: 25
Platforms: spring
Defaults: spring-web, validation, observability, spring-test, web-test
Features: jooq
Native: disabled
```

A settings-level diagnostic such as `durexProjects` should print:

```text
:music
  path: core/music
  source: auto

:shared-common
  path: core/shared/jakarta/common
  source: manual
```

A bootstrap diagnostic should also be able to print the dependency manifest sources and resolved platform/plugin versions for the current Gradle build.

Gradle's native `dependencies` and `dependencyInsight` remain the authoritative dependency-resolution diagnostics.

## 14. Migration Strategy

Migration must not break the existing legacy root build while Spring migration continues.

### Phase 1: bootstrap and registry

- add `build-bootstrap`;
- add TOML loader/model/validation;
- add `durex.settings`, `durex.build-logic-settings`, and `durex.build-logic`;
- add dependency/project registry tests;
- leave existing business modules unchanged.

### Phase 2: module-type/feature smoke builds

Create isolated functional tests for:

- Java library;
- Spring library;
- Spring service;
- Spring service + JPA;
- Spring service + jOOQ;
- Spring service + JPA + jOOQ;
- Redis;
- Native Image;
- invalid module-type conflicts;
- invalid feature prerequisites;
- manifest validation;
- auto/manual project discovery;
- independent root/build-logic registry initialization from the same TOML source.

Preserve the existing jOOQ schema smoke test.

### Phase 3: migrate `core/music`

Replace the current Spring alternate build's direct Spring Boot alias, catalog access, repeated `apply from`, and framework dependency wiring with `durex.spring-service` and `persistence.jooq()`.

Preserve business `project(...)` dependencies explicitly.

### Phase 4: migrate remaining Spring modules

Convert modules incrementally while retaining `gradle/library/*.gradle` for modules not yet migrated.

### Phase 5: promote new root build

After module migration is complete:

- promote the root Gradle version/build to the new baseline;
- enable `durex.settings` at root;
- remove Gradle Version Catalog registration from root settings;
- remove deprecated Spring `gradle/library/*.gradle` and temporary migration settings;
- replace legacy `modules.gradle` with Durex project discovery;
- remove `gradle/versions/*.toml` after their data has migrated to `gradle/dependencies/*.toml`.

## 15. Testing Requirements

Build platform changes require functional tests independent of business modules.

Minimum test matrix:

- manifest parse and include;
- include-cycle failure;
- duplicate-id failure;
- missing/unknown version/platform failure;
- platform/version ownership conflicts;
- root-build registry initialization;
- build-logic-build registry initialization from the same source;
- module type conflict;
- feature prerequisite validation;
- platform de-duplication;
- JPA + jOOQ coexistence;
- jOOQ schema generation;
- Spring Boot context/MVC;
- Spring jOOQ `DSLContext` wiring;
- Native AOT/native smoke;
- automatic discovery;
- manual-only discovery;
- manual override of automatic naming;
- exclusions;
- duplicate logical project name failure.

## 16. Final Ownership Model

```text
gradle/dependencies/*.toml
    -> single global dependency/plugin version source

build-bootstrap
    -> TOML semantics, validation, per-build registry bootstrap,
       root project discovery, build-logic bootstrap

build-logic
    -> module types, framework defaults, optional features, codegen

Gradle platform/BOM + resolver
    -> actual dependency alignment and conflict resolution

module build files
    -> module identity via plugin, optional Durex features,
       business project dependencies
```

The resulting rule for developers is:

> A module declares what it is, what optional capabilities it needs, and which business modules it depends on. Everything else is build-platform policy.
