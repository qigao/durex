# Complete SimpleDSL Cutover Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the duplicated Durex Gradle platform implementation and make Durex a pure SimpleDSL consumer, while validating Durex against SimpleDSL source in GitHub Actions.

**Architecture:** Production `settings.gradle` continues to consume `io.github.qigao.simpledsl.settings` version `0.1.0` from the Gradle Plugin Portal. Durex owns only business code plus TOML manifests. CI checks out `qigao/simpledsl` into `.simpledsl` and uses Gradle composite build substitution (`--include-build .simpledsl`) so unpublished SimpleDSL source can compile and drive Durex builds without adding any production fallback.

**Tech Stack:** Gradle 9.1.0, Java 25, Groovy/Kotlin Gradle DSL, GitHub Actions, SimpleDSL.

**Spec:** Approved in-chat design from 2026-08-22: Durex no longer owns build-bootstrap/build-logic; production uses Plugin Portal, CI may compile SimpleDSL locally.

## Global Constraints

- Keep `io.github.qigao.simpledsl.settings` version `0.1.0` in production settings.
- Keep project descriptors on `io.github.qigao.simpledsl.build`; do not reintroduce feature/schema public plugin IDs.
- Delete Durex-owned `build-bootstrap/` and `build-logic/` completely.
- Preserve `gradle/dependencies/*.toml` and `gradle/modules.toml` as Durex-owned policy/configuration.
- Source integration must be CI-only; no production `includeBuild` fallback.
- CI source checkout targets `qigao/simpledsl` and compiles its bootstrap/build-logic before Durex verification.

---

### Task 1: Remove the duplicated Durex build platform

**Files:**
- Delete: `build-bootstrap/**`
- Delete: `build-logic/**`
- Delete: `.github/workflows/durex-build-platform.yml`
- Delete: `.github/workflows/durex-plugin-api.yml`
- Modify: `README.adoc`
- Modify: `core/shared/messaging/README.md`

- [ ] Remove the two top-level Gradle platform implementation trees.
- [ ] Remove CI workflows that only test the deleted implementation.
- [ ] Rewrite active documentation so SimpleDSL is the only build-platform owner.
- [ ] Verify no active production descriptor applies `durex.*` Gradle plugins.
- [ ] Commit as `refactor: remove local Durex build platform`.

### Task 2: Convert consumer CI to SimpleDSL source composite builds

**Files:**
- Create: `.github/workflows/simpledsl-source-integration.yml`
- Modify: `.github/workflows/durex-module-discovery.yml`
- Modify: `.github/workflows/spring-messaging.yml`
- Modify: `.github/workflows/spring-music.yml`
- Modify: `.github/workflows/spring-native-reference.yml`
- Modify: `.github/workflows/legacy-runtime-boundary.yml`

- [ ] Add a second checkout for `qigao/simpledsl` at `.simpledsl`.
- [ ] Build `:simpledsl-build-bootstrap:check` and `:simpledsl-build-logic:check` with Gradle 9.1.0 / Java 25.
- [ ] Run Durex consumer tasks with `--include-build "$GITHUB_WORKSPACE/.simpledsl"`.
- [ ] Remove path filters that reference deleted `build-bootstrap/**` or `build-logic/**`.
- [ ] Keep runtime boundary checks independent of the deleted platform implementation.
- [ ] Verify root graph, Spring music, messaging, and native reference workflows against SimpleDSL source.
- [ ] Commit as `ci: validate Durex against SimpleDSL source`.

### Task 3: Final repository contract

**Files:**
- Modify: `README.adoc` if CI reveals additional migration notes.

- [ ] Search the active repository for references to `build-bootstrap`, `build-logic`, `durex.settings`, `durex.module`, `durex.feature.*`, and `durex.schema.*`; historical design docs may remain, active build/CI/docs must not depend on them.
- [ ] Confirm `settings.gradle` has no `includeBuild` and still points to Durex TOML manifests.
- [ ] Confirm SimpleDSL source integration CI passes.
- [ ] Update PR #137 summary to describe the complete cutover.
