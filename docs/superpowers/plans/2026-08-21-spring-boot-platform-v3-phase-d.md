# Spring Boot Platform v3 Phase D Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish the Spring-first migration by removing Quarkus/CDI runtime remnants, making Durex settings the authoritative project-discovery mechanism, promoting JSON Schema to a first-class Durex schema plugin, and modernizing the root build around Gradle 9.1/Spring Boot.

**Architecture:** Durex owns build-time conventions, dependency wiring, schema generation, and module discovery. Spring owns runtime behavior. Phase D removes legacy Quarkus/Jakarta runtime integration while retaining framework-neutral Jakarta standards such as Validation where Spring uses them. Automatic discovery recognizes Durex module build files, while manifests remain optional overrides/manual declarations.

**Tech Stack:** Gradle 9.1, Groovy/Kotlin Gradle DSL, Durex build-bootstrap/build-logic, Spring Boot 4.1, jOOQ, jsonSchema2Pojo, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-08-21-durex-spring-boot-platform-v3-design.md`

## Global Constraints

- Spring is the only target runtime; do not add a dual-runtime Durex abstraction.
- Preserve `jakarta.validation.*`, `jakarta.persistence.*`, and other standard APIs when Spring itself consumes them.
- Remove Quarkus/CDI/SmallRye/Jandex runtime integration from the active source/build graph.
- Durex settings/project discovery becomes authoritative; do not maintain two recursive module scanners.
- Schema plugins are build-time capabilities under `durex.schema.*` and remain runtime-framework-neutral.
- Gradle 9.1 is the root target once legacy Quarkus build constraints are removed.
- Each task must leave an independently verifiable build state.

---

### Task 1: Remove legacy Quarkus runtime graph

**Files:**
- Delete: `core/music-reactive/**`
- Delete: `core/shared/jakarta/**`
- Delete: `gradle/library/quarkus-core.gradle`
- Delete: `gradle/library/quarkus-imperative.gradle`
- Delete: `gradle/library/quarkus-reactive.gradle`
- Delete: `gradle/library/quarkus-test.gradle`
- Delete: `gradle/versions/quarkus.versions.toml`
- Delete legacy-only portions of `core/music` while retaining Spring-reused service/domain code.
- Modify: `.github/workflows/durex-build-platform.yml` or add a focused legacy-boundary guard.
- Modify: `settings.gradle` only as required to remove the `qLibs` catalog reference after the Quarkus catalog is deleted.

**Interfaces:**
- Consumes: the current Spring Music and Spring Messaging migration graphs.
- Produces: repository state with no active Quarkus/CDI runtime module or Quarkus dependency catalog.

- [ ] **Step 1: Add a failing legacy-runtime boundary check**

The check must fail while any of these active artifacts exist: `core/music-reactive`, `core/shared/jakarta`, `gradle/versions/quarkus.versions.toml`, `gradle/library/quarkus-*.gradle`, or active source/build references to `io.quarkus`, `javax.enterprise`, `javax.inject`, `javax.interceptor`, `smallrye`, or `org.jboss.jandex` outside historical docs/plans.

- [ ] **Step 2: Verify RED structurally**

Expected: the new guard identifies the current legacy directories/catalogs before deletion.

- [ ] **Step 3: Remove complete legacy leaves**

Delete `core/music-reactive/**` and `core/shared/jakarta/**`. Remove Quarkus Gradle library fragments and version catalog.

- [ ] **Step 4: Remove Quarkus-only pieces from shared Spring-reused modules**

For `core/music`, retain `src/main/java` classes required by the Spring build, `src/spring/**`, `src/springTest/**`, and `build.spring.gradle`; remove legacy `build.gradle`, excluded Quarkus controllers/config, legacy tests, SmallRye resource config, and Quarkus Docker artifacts.

- [ ] **Step 5: Remove the `qLibs` root catalog**

Update root settings so deleted Quarkus catalog files are no longer referenced.

- [ ] **Step 6: Verify GREEN**

Run the legacy boundary guard plus Spring Music, Spring Messaging, plugin API, and build-platform workflows in CI.

- [ ] **Step 7: Commit**

Commit message: `refactor: remove legacy Quarkus runtime graph`.

---

### Task 2: Make Durex settings authoritative for module discovery

**Files:**
- Modify: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/ProjectDiscovery.groovy`
- Modify: `build-bootstrap/src/main/groovy/com/github/durex/gradle/settings/DurexSettingsPlugin.groovy`
- Modify/create fixtures under `build-bootstrap/tests/modules-*`
- Modify: root `settings.gradle`
- Delete: `gradle/extensions/modules.gradle`

**Interfaces:**
- Consumes: `ProjectSpec`, `ProjectRegistry`, Durex settings extension.
- Produces: one discovery engine supporting automatic `build.gradle`, `build.gradle.kts`, and Durex alternate build files such as `build.spring.gradle`, with optional manifest overrides.

- [ ] **Step 1: Add failing discovery fixtures**

Cover automatic discovery of `build.spring.gradle`, operation with no `modules.toml`, manual override, exclude, and duplicate-path conflict behavior.

- [ ] **Step 2: Verify RED**

Expected: automatic `build.spring.gradle` discovery/no-manifest fixture fails with the current implementation.

- [ ] **Step 3: Implement build-file candidate discovery**

Teach `ProjectDiscovery` to recognize supported build-file names without recursively including structural parent directories as modules.

- [ ] **Step 4: Make manifest optional in automatic mode**

A missing manifest must mean “use discovery defaults”, not an error. Explicit manual mode still requires declared modules.

- [ ] **Step 5: Migrate root settings to `durex.settings`**

Remove the old `modules.gradle` scanner and use the Durex settings plugin as the single project graph authority.

- [ ] **Step 6: Verify**

Run all `build-bootstrap/tests/modules-*` fixtures, `durexProjects`, configuration-cache checks, and Spring migration project graphs.

- [ ] **Step 7: Commit**

Commit message: `refactor: make Durex settings authoritative`.

---

### Task 3: Add `durex.schema.json`

**Files:**
- Create: `build-logic/src/main/groovy/durex.schema.json.gradle`
- Create focused extension/support classes only if the convention script would otherwise become stateful/complex.
- Create: `build-logic/tests/json-schema-smoke/**`
- Modify: `build-logic/tests/verify-plugin-namespaces.sh`
- Modify: `core/schema/music/json/build.spring.gradle`
- Modify dependency manifest/catalog only for jsonSchema2Pojo plugin/library declarations required by the Durex plugin.

**Interfaces:**
- Produces public plugin `durex.schema.json` and a concise extension such as:

```gradle
durexJsonSchema {
    source = 'json'
    packageName = 'com.github.durex.music.model'
    validation = true
    builders = true
}
```

- [ ] **Step 1: Add failing plugin contract fixture**

Verify public plugin ID, generated source task wiring, Java compile dependency, Jakarta Validation option, and generated package.

- [ ] **Step 2: Verify RED**

Expected: plugin ID is missing.

- [ ] **Step 3: Implement minimal convention plugin**

Apply/configure jsonSchema2Pojo, add generated sources to Java compilation, and expose only Durex-owned configuration knobs needed by existing Music schemas.

- [ ] **Step 4: Migrate Music JSON schema module**

Replace direct `org.jsonschema2pojo` configuration with `durex.schema.json`.

- [ ] **Step 5: Verify generated model consumers**

Run `music-json`, `music-entity`, `music-repo`, and `music` compile/tests.

- [ ] **Step 6: Commit**

Commit message: `feat: add Durex JSON schema plugin`.

---

### Task 4: Modernize root build and final repository boundary

**Files:**
- Modify: `settings.gradle`
- Modify: `build.gradle`
- Modify: `gradle/wrapper/gradle-wrapper.properties` and wrapper scripts/artifacts only through standard Gradle wrapper generation when available.
- Delete obsolete `gradle/library/**`, `gradle/versions/**`, and `gradle/extensions/**` files once confirmed unused by the Durex platform.
- Modify: `README.adoc`
- Modify/add GitHub Actions verification.

**Interfaces:**
- Produces a Spring Boot/Durex-only root build that runs on Gradle 9.1 and no longer relies on legacy `allprojects/subprojects` dependency injection.

- [ ] **Step 1: Add final repository boundary guard**

Guard active build/source files against Quarkus/CDI/Jandex/SmallRye references and obsolete plugin/catalog IDs.

- [ ] **Step 2: Simplify root build**

Remove inherited legacy test/repository/plugin configuration now owned by Durex conventions.

- [ ] **Step 3: Upgrade root wrapper to Gradle 9.1**

Use standard wrapper generation and verify wrapper metadata.

- [ ] **Step 4: Delete unused legacy Gradle fragments/catalogs**

Only delete files with zero active references after Tasks 1-3.

- [ ] **Step 5: Update README**

Describe Durex as Spring Boot-first, document public module/feature/schema plugin namespaces, and remove Quarkus language.

- [ ] **Step 6: Full verification**

Run build-bootstrap fixtures, build-logic fixtures, Spring Music, Spring Messaging, Spring Native reference, shared-utils/rest-assured retained workflows, configuration cache, and final boundary guard.

- [ ] **Step 7: Commit**

Commit message: `build: modernize Durex root platform`.
