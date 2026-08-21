# Durex Public Plugin API Cleanup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the supported Durex Gradle plugin API explicit, move implementation plugins under `durex.internal.*`, and rename the framework-neutral jOOQ schema plugin to `durex.schema.jooq`.

**Architecture:** Keep public module/feature plugins stable and change only the namespace boundary. Public plugins compose internal convention plugins; schema code generation remains independent of Spring runtime conventions. No compatibility aliases are retained.

**Tech Stack:** Gradle 9.1 build logic, Groovy precompiled script plugins, Kotlin Gradle build scripts, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-08-21-durex-public-plugin-api-design.md`

## Global Constraints

- Public IDs are exactly the Platform, Module Types, Schema, and Features listed in the spec.
- Internal implementation IDs use `durex.internal.*`.
- `durex.jooq-schema` and the other legacy implementation IDs must not remain as aliases.
- User-facing diagnostics must recommend public plugins, not internal plugins.
- Runtime/capability semantics and dependency versions do not change.

---

### Task 1: Lock the plugin namespace contract

**Files:**
- Modify: `build-logic/tests/jooq-schema-smoke/build.gradle`
- Create: `build-logic/tests/legacy-jooq-plugin-id-invalid/settings.gradle.kts`
- Create: `build-logic/tests/legacy-jooq-plugin-id-invalid/build.gradle`
- Create: `build-logic/tests/verify-plugin-namespaces.sh`
- Modify: `.github/workflows/durex-build-platform.yml`

**Interfaces:**
- Consumes: current plugin IDs from build-bootstrap/build-logic.
- Produces: a failing contract that requires `durex.schema.jooq`, rejects `durex.jooq-schema`, and checks the internal namespace.

- [ ] **Step 1: Change the jOOQ smoke fixture to the new public ID**

```groovy
plugins {
    id 'java'
    id 'durex.schema.jooq'
}
```

- [ ] **Step 2: Add a legacy-ID negative fixture that still applies `durex.jooq-schema`**

The fixture configures `durexJooq.packageName` so it succeeds before the rename and fails specifically at plugin resolution after the rename.

- [ ] **Step 3: Add namespace source contract**

The script checks expected public/internal files/registered IDs and rejects legacy production IDs/files.

- [ ] **Step 4: Run the contract and confirm RED**

Run via GitHub Actions `Durex Build Platform`. Expected before implementation: the new schema ID cannot resolve and/or namespace contract rejects the old names.

- [ ] **Step 5: Commit the failing contract**

Commit message: `test: define Durex public plugin API contract`.

---

### Task 2: Internalize bootstrap and dependency-catalog plugins

**Files:**
- Modify: `build-bootstrap/build.gradle.kts`
- Modify: `build-logic/settings.gradle.kts`
- Modify: `build-logic/build.gradle.kts`
- Modify: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexBuildLogicPlugin.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexModulePlugin.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexDependencyAccess.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/dependency/DependencyBridge.groovy`
- Modify: `build-logic/tests/registry-bridge-smoke/build.gradle`
- Modify: `build-logic/tests/registry-schema-mismatch/build.gradle`

**Interfaces:**
- Consumes: build bootstrap shared dependency registry.
- Produces: `durex.internal.build-logic-settings`, `durex.internal.build-logic`, `durex.internal.catalog`.

- [ ] **Step 1: Rename registered bootstrap plugin IDs**

```kotlin
id = "durex.internal.build-logic-settings"
id = "durex.internal.build-logic"
```

- [ ] **Step 2: Update build-logic bootstrap callers**

```kotlin
plugins { id("durex.internal.build-logic-settings") }
plugins { id("durex.internal.build-logic") }
```

- [ ] **Step 3: Rename catalog registration and internal callers**

```kotlin
id = "durex.internal.catalog"
```

`DurexModulePlugin` and schema convention code apply this ID internally.

- [ ] **Step 4: Hide internal IDs from recovery diagnostics**

Catalog errors recommend `durex.module` only; bootstrap errors describe the build-logic bootstrap phase.

- [ ] **Step 5: Update catalog-focused test fixtures**

They may directly apply `durex.internal.catalog` because they test internal catalog mechanics.

---

### Task 3: Rename internal base conventions and public schema plugin

**Files:**
- Rename: `build-logic/src/main/groovy/durex.java-base.gradle` -> `build-logic/src/main/groovy/durex.internal.java-base.gradle`
- Rename: `build-logic/src/main/groovy/durex.spring-base.gradle` -> `build-logic/src/main/groovy/durex.internal.spring-base.gradle`
- Rename: `build-logic/src/main/groovy/durex.jooq-schema.gradle` -> `build-logic/src/main/groovy/durex.schema.jooq.gradle`
- Modify: `build-logic/src/main/groovy/durex.java-library.gradle`
- Modify: `build-logic/src/main/groovy/durex.spring-library.gradle`
- Modify: `build-logic/src/main/groovy/durex.spring-service.gradle`
- Modify: `core/schema/music/entity/build.spring.gradle`

**Interfaces:**
- Consumes: public `durex.module`, internal catalog, existing module model.
- Produces: internal base composition and public `durex.schema.jooq`.

- [ ] **Step 1: Rename the precompiled script plugin files**

Gradle derives precompiled plugin IDs from these file names, so no alias remains.

- [ ] **Step 2: Update module plugin composition**

`durex.java-library` applies `durex.internal.java-base`; Spring module types apply `durex.internal.spring-base`; Spring base applies the internal Java base.

- [ ] **Step 3: Update jOOQ schema convention to use `durex.internal.catalog`**

Keep all existing jOOQ codegen behavior unchanged.

- [ ] **Step 4: Migrate Spring music schema module to `durex.schema.jooq`**

No change to its `durexJooq` configuration.

---

### Task 4: Internalize the capability fixture plugin

**Files:**
- Modify: `build-logic/build.gradle.kts`
- Modify: `build-logic/tests/generic-capability-smoke/build.gradle`
- Modify: `build-logic/tests/generic-capability-direct/build.gradle`

**Interfaces:**
- Consumes: generic capability plugin registry.
- Produces: `durex.internal.fixture` test-only plugin ID.

- [ ] **Step 1: Rename the registered fixture plugin ID**

```kotlin
id = "durex.internal.fixture"
```

- [ ] **Step 2: Update both generic capability fixtures**

Use `durex.internal.fixture` for DSL activation and direct plugin application.

---

### Task 5: Verify and publish

**Files:**
- Verification only.

- [ ] **Step 1: Verify namespace contract**

Run: `bash build-logic/tests/verify-plugin-namespaces.sh`
Expected: PASS.

- [ ] **Step 2: Verify build logic bootstrap and jOOQ smoke**

Run: `gradle -p build-logic tasks --stacktrace`
Run: `gradle -p build-logic/tests/jooq-schema-smoke jooqCodegen --stacktrace`
Expected: PASS and generated `QMusic.java` / `RMusic.java`.

- [ ] **Step 3: Verify legacy schema ID rejection**

Run: `gradle -p build-logic/tests/legacy-jooq-plugin-id-invalid help --stacktrace`
Expected: FAIL because `durex.jooq-schema` is not found.

- [ ] **Step 4: Run Durex Build Platform CI and Spring migration/reference CI**

Expected: all triggered checks pass.

- [ ] **Step 5: Commit implementation**

Commit message: `refactor: define Durex public plugin namespace`.

- [ ] **Step 6: Open a draft PR from `design/plugin-public-api` to `master`**

The PR should describe the breaking plugin-ID mapping and validation results.
