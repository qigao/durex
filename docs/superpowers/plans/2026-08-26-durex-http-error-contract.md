# Durex 0.1 HTTP Error Contract Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace accidental mutable HTTP DTO API with a deliberate immutable record contract, stabilize one error occurrence per exception, and classify client errors correctly before `v0.1.0`.

**Architecture:** `shared-common` owns immutable transport-neutral error/envelope records and stable `ApiException` state. `shared-spring-http` owns only HTTP status mapping. The real Music Spring integration remains the serialization/deserialization proof, while staged-Maven signatures are deliberately regenerated for the final pre-release contract.

**Tech Stack:** Java 25 records, Spring Boot 4.1, Spring MVC, JUnit 5, Gradle 9.1, staged Maven API verification.

**Spec:** `docs/superpowers/specs/2026-08-26-durex-http-error-contract-design.md`

## Global Constraints

- Keep public Maven artifacts unchanged.
- Preserve `RespData.of(result, error)`.
- Do not expose implementation caller/class/method data in HTTP JSON.
- `ENTITY_NOT_FOUND` remains 404; `EMPTY_PARAM` and `VALUE_ERROR` become 400; unspecified/server failures remain 500.
- The final `0.1-signatures.txt` must be generated from staged jars and exactly represent the final pre-release API.

---

### Task 1: RED common-model contract

**Files:**
- Create: `core/shared/common/src/test/java/com/github/durex/shared/exceptions/ApiExceptionContractTest.java`
- Create: `core/shared/common/src/test/java/com/github/durex/shared/model/HttpModelSurfaceTest.java`

**Interfaces:**
- Produces: failing tests for stable error occurrence and immutable record API

- [ ] **Step 1: Test stable exception identity**

Construct `new ApiException("invalid", ErrorCode.VALUE_ERROR)`, call `getErrorResponse()` twice, and require `assertSame(first, second)`.

- [ ] **Step 2: Test the final surface by reflection**

Require `ErrorResponse.class.isRecord()` and `RespData.class.isRecord()`. Require `ErrorResponse` to have no `getCaller`, `setCaller`, or `withCaller` method and `RespData` to have no `builder`, `setResult`, or `setError` method.

- [ ] **Step 3: Verify RED**

Run `:shared-common:test`. Expected failures: current exceptions create a fresh response; both DTOs are ordinary Lombok classes and expose the legacy methods.

### Task 2: RED HTTP semantics

**Files:**
- Create: `core/shared/spring/http/src/test/java/com/github/durex/shared/spring/http/DurexHttpExceptionHandlerTest.java`
- Modify: `core/music/src/springTest/java/com/github/durex/music/spring/MusicHttpContractIntegrationTest.java`

**Interfaces:**
- Consumes: current handler and real Spring server
- Produces: status and serialized-boundary expectations

- [ ] **Step 1: Add handler status tests**

Call `handleApiException` with `ENTITY_NOT_FOUND`, `EMPTY_PARAM`, `VALUE_ERROR`, and `UNKNOWN_ERROR`; require 404, 400, 400, and 500 respectively.

- [ ] **Step 2: Add real JSON boundary assertion**

The existing missing-music random-port test must additionally require that the response body does not contain `"caller"`.

- [ ] **Step 3: Verify RED**

Run the shared HTTP test plus `:music:test`. Expected failures: client validation codes are 500 and missing-music JSON includes caller metadata.

### Task 3: GREEN immutable common model

**Files:**
- Modify: `core/shared/common/src/main/java/com/github/durex/shared/exceptions/ApiException.java`
- Modify: `core/shared/common/src/main/java/com/github/durex/shared/exceptions/model/ErrorResponse.java`
- Modify: `core/shared/common/src/main/java/com/github/durex/shared/model/RespData.java`

**Interfaces:**
- Produces: `ErrorResponse(UUID,String,ErrorCode,LocalDateTime)` record and `RespData<T>(T,ErrorResponse)` record with static `of`

- [ ] **Step 1: Replace `ErrorResponse` with the documented record**

Remove Lombok annotations and caller state entirely. Keep `Serializable` and document all record components.

- [ ] **Step 2: Replace `RespData` with the documented record**

Remove Lombok builder/getter/setter surface and preserve only the canonical record API plus `of(...)`.

- [ ] **Step 3: Stabilize `ApiException`**

Create the structured `ErrorResponse` once in the constructor with one UUID and timestamp; return the stored value from `getErrorResponse()`; remove stack walking and duplicate message/caller fields.

- [ ] **Step 4: Verify common-model GREEN**

Run `:shared-common:test` and strict Javadoc for the public common API.

### Task 4: GREEN HTTP mapping and consumers

**Files:**
- Modify: `core/shared/spring/http/src/main/java/com/github/durex/shared/spring/http/DurexHttpExceptionHandler.java`
- Modify: `core/music/src/springTest/java/com/github/durex/music/spring/MusicHttpContractIntegrationTest.java`
- Modify any repository source/tests that call removed bean getters on `RespData`/`ErrorResponse`
- Modify: `reference/publication-consumer/src/main/java/com/github/durex/reference/publication/PublicApiBaseline.java`
- Modify: `docs/public-artifacts.md`

**Interfaces:**
- Consumes: record accessors `errorCode()`, `result()`, `error()`

- [ ] **Step 1: Implement explicit status mapping**

Use a null-safe switch: 404 for entity-not-found, 400 for empty/value errors, 500 otherwise.

- [ ] **Step 2: Migrate record accessor usage**

Replace old generated getters only where the repository actually uses them; do not add compatibility aliases.

- [ ] **Step 3: Update public docs/consumer examples**

Describe immutable record DTOs and keep `RespData.of(...)` as the construction example.

- [ ] **Step 4: Verify HTTP GREEN**

Run `:shared-spring-http:test`, Music HTTP/client tests, and the Maven-only consumer build.

### Task 5: Deliberate 0.1 baseline rewrite

**Files:**
- Modify: `gradle/public-api/0.1-signatures.txt`

**Interfaces:**
- Consumes: staged Maven jars from the final implementation
- Produces: exact pre-release 0.1 JVM baseline

- [ ] **Step 1: Stage the four Maven artifacts**

Use the publication workflow/Gradle staging repository with `0.1.0-SNAPSHOT`.

- [ ] **Step 2: Generate signatures from staged jars**

Run `scripts/public-api-signatures.sh generate ...` using `gradle/public-api/0.1-surface.txt`.

- [ ] **Step 3: Review the generated diff**

Confirm the old setters/withers/builder/caller signatures disappear and only the intended record/canonical factory API replaces them.

- [ ] **Step 4: Commit the exact generated baseline**

Do not hand-edit unrelated signature lines.

### Task 6: Final verification and review

- [ ] **Step 1: Run publication surface and release dry-run verification**

Require strict Javadoc, staged artifact boundary, exact API baseline, Maven-only consumer, Spring Music, and native reference checks green.

- [ ] **Step 2: Review branch diff against `master`**

No Redis/messaging or unrelated Music behavior changes are allowed.

- [ ] **Step 3: Update PR with RED/GREEN evidence and close #190 on merge**
