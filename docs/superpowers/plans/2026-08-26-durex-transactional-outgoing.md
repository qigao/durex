# Durex Transactional Outgoing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Redis Pub/Sub and Stream outgoing annotations publish only after successful Spring transaction commit while preserving immediate non-transactional behavior.

**Architecture:** Add one package-private transaction-synchronization helper used by both existing outgoing aspects. Keep the public annotation/API surface unchanged and prove the semantics with the real Redis integration suite.

**Tech Stack:** Java 25, Spring transaction synchronization, Spring AOP, Spring Data Redis, Redis 7, JUnit 5, Gradle/SimpleDSL.

**Spec:** `docs/superpowers/specs/2026-08-26-durex-transactional-outgoing-design.md`

## Global Constraints

- Do not add a broker abstraction, retry engine, DLQ, or outbox.
- Do not add public API types or annotation members.
- The annotated method still executes synchronously and returns normally before publication handling.
- Encoding failure for Stream output remains synchronous.
- Normal Spring AOP self-invocation limitations are documented, not bypassed.

---

### Task 1: RED Pub/Sub transaction contract

**Files:**
- Modify: `core/shared/messaging/spring-redis/build.spring.gradle`
- Modify: `core/shared/messaging/spring-redis/src/test/java/com/github/durex/messaging/spring/redis/RedisPubSubIntegrationTest.java`

- [ ] Add Spring transaction support to the test classpath only.
- [ ] Add a minimal test `PlatformTransactionManager` and `TransactionTemplate` bean.
- [ ] Add distinct commit/rollback Pub/Sub destinations and listener queues.
- [ ] Require no message to be visible while the transaction is active.
- [ ] Require successful commit to publish afterward.
- [ ] Require rollback to publish nothing.
- [ ] Run `:messaging-spring-redis:test`; current immediate-send aspect must fail the before-commit/rollback contract.

### Task 2: RED Stream transaction contract

**Files:**
- Modify: `core/shared/messaging/spring-redis/src/test/java/com/github/durex/messaging/spring/redis/RedisStreamIntegrationTest.java`

- [ ] Create dedicated transactional streams/groups during test infrastructure initialization.
- [ ] Add commit/rollback Stream outgoing methods and listener queues.
- [ ] Require no record/listener delivery before commit.
- [ ] Require commit delivery after commit.
- [ ] Require rollback to produce no record/delivery.
- [ ] Verify RED against the current immediate `XADD` aspect.

### Task 3: GREEN internal transaction synchronization

**Files:**
- Create: `core/shared/messaging/spring-redis/src/main/java/com/github/durex/messaging/spring/redis/TransactionalPublication.java`
- Modify: `core/shared/messaging/spring-redis/src/main/java/com/github/durex/messaging/spring/redis/RedisOutgoingAspect.java`
- Modify: `core/shared/messaging/spring-redis/src/main/java/com/github/durex/messaging/spring/redis/RedisStreamOutgoingAspect.java`
- Modify: `core/shared/messaging/spring-redis/build.spring.gradle`

- [ ] Promote Spring transaction support from test-only to implementation dependency.
- [ ] Implement `publishNowOrAfterCommit(Runnable)` using `TransactionSynchronizationManager`.
- [ ] Route Pub/Sub `convertAndSend` through the helper.
- [ ] Encode Stream result synchronously, then route only `XADD` through the helper.
- [ ] Keep helper package-private and outside public API manifest.
- [ ] Run Redis integration tests; all transaction and existing non-transaction tests must pass.

### Task 4: Documentation and native regression

**Files:**
- Modify: `core/shared/messaging/README.md`

- [ ] Document immediate vs after-commit behavior.
- [ ] Document rollback suppression.
- [ ] Document normal Spring proxy/self-invocation semantics.
- [ ] Run Spring Messaging Platform and Spring Native Reference workflows.
- [ ] Confirm public JVM signature baseline is unchanged.

### Task 5: Review

- [ ] Compare branch to `master`; only messaging implementation/tests/build/docs/spec/plan may change.
- [ ] Require all triggered CI green.
- [ ] Update PR with RED/GREEN evidence and close #191 on merge.
