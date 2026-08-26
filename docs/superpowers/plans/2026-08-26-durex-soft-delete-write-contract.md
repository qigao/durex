# Durex Soft-Delete Write Contract Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make normal Music/Playlist updates and deletes ignore rows already marked deleted.

**Architecture:** Keep the existing service error semantics and enforce the invariant atomically in repository SQL predicates with jOOQ. Extend the existing H2 repository integration fixture to inspect hidden deleted rows directly.

**Tech Stack:** Java 25, jOOQ, Spring Boot/H2 integration tests, Gradle/SimpleDSL.

**Spec:** `docs/superpowers/specs/2026-08-26-durex-soft-delete-write-contract-design.md`

### Task 1: RED repeated-delete contract

- [ ] Extend `RepositoryWiringTest` to operate on the existing deleted Music/Playlist fixtures.
- [ ] Capture deleted-row `DELETE_TIME`, call normal repository delete, require affected count `0`, and require the timestamp unchanged.
- [ ] Run Spring Music Migration; current delete queries must fail this contract.

### Task 2: RED deleted-row update contract

- [ ] Attempt single-row update of the deleted Music/Playlist ids with changed titles.
- [ ] Require affected count `0`.
- [ ] Query H2 directly and require the stored title unchanged.
- [ ] Verify RED against current record `update()` behavior.

### Task 3: GREEN conditional writes

- [ ] Add `NOT_DELETED` to all normal soft-delete predicates.
- [ ] Replace single update with an explicit conditional jOOQ update preserving the current mapped/touched fields.
- [ ] Build batch updates from the same conditional query shape so per-row counts remain meaningful.
- [ ] Apply the same invariant to Music and Playlist repositories.

### Task 4: Regression and review

- [ ] Run Spring Music Migration, SimpleDSL Integration, Module Discovery, and Legacy Runtime Boundary.
- [ ] Confirm no public Maven API/signature change.
- [ ] Compare diff to `master`; only Music/Playlist repository behavior/tests/docs may change.
- [ ] Update PR with RED/GREEN evidence and close #192 on merge.
