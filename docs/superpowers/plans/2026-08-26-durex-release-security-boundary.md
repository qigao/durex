# Durex Release Security Boundary Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Separate unprivileged release verification from tag-only privileged publication without weakening the existing exact-SHA release proof.

**Architecture:** Keep the existing release mechanics but split trust domains into two workflows. A standalone shell contract verifies the split structurally; the verification workflow remains secret-free/read-only, while the tag-only publication workflow owns release secrets and write permission.

**Tech Stack:** GitHub Actions, Bash, Gradle 9.1, Java 25, Maven Central Publisher Portal.

**Spec:** `docs/superpowers/specs/2026-08-26-durex-release-security-boundary-design.md`

## Global Constraints

- Public artifact set remains exactly `shared-common`, `shared-spring-http`, `messaging-api`, `messaging-spring-redis`.
- Release version remains stable `X.Y.Z`; network publication requires tag `vX.Y.Z` at exact `GITHUB_SHA`.
- PR/manual verification must not receive release secrets or `contents: write`.
- Privileged release third-party actions must use immutable commit SHAs.
- Do not change runtime/public API behavior in this plan.

---

### Task 1: Executable release-security contract

**Files:**
- Create: `scripts/verify-release-security-boundary.sh`
- Create: `.github/workflows/durex-release-security.yml`

**Interfaces:**
- Consumes: `.github/workflows/durex-release.yml`
- Produces: a deterministic shell gate that validates final workflow privilege boundaries

- [ ] **Step 1: Add the security contract before the workflow split**

The script must fail if `.github/workflows/durex-release-verify.yml` is missing; reject PR/manual triggers in the privileged workflow; reject write permission/release-secret names in the verification workflow; and reject mutable `@vN` refs in either release workflow.

- [ ] **Step 2: Run the contract in a read-only PR workflow**

The workflow uses pinned `actions/checkout@11d5960a326750d5838078e36cf38b85af677262` and runs the shell script.

- [ ] **Step 3: Verify RED**

Open the PR with only the contract present. Expected result: `Durex Release Security Boundary` fails because the verification workflow does not yet exist and the current release workflow still accepts `pull_request`/`workflow_dispatch`.

### Task 2: Split verification from publication

**Files:**
- Create: `.github/workflows/durex-release-verify.yml`
- Modify: `.github/workflows/durex-release.yml`
- Modify: `docs/releasing.md`

**Interfaces:**
- Produces: `Durex Release Verification` for PR/manual dry-runs and tag-only `Durex Release` for publication

- [ ] **Step 1: Create the read-only verification workflow**

Copy only the credentials-free release proof: version validation, Apache-2.0 check, isolated unsigned staging, exact API baseline comparison, Maven-only consumer, unsigned Central bundle inspection, tested-SHA summary.

- [ ] **Step 2: Restrict the publication workflow to tags**

Remove `pull_request` and `workflow_dispatch`. Resolve version only from `vX.Y.Z`, require tag SHA equality and non-empty signing/Central credentials, then preserve signed staging, exact API verification, Central validation/publish polling, and GitHub Release creation.

- [ ] **Step 3: Pin release actions**

Use the immutable SHAs recorded in the design for checkout, setup-java, and setup-gradle in both release workflows.

- [ ] **Step 4: Update release documentation**

Document `Durex Release Verification` as the PR/manual dry-run and `Durex Release` as tag-only publication. Remove wording that implies manual network publication.

- [ ] **Step 5: Verify GREEN**

Expected: security-boundary workflow passes; release verification successfully stages the four artifacts, compares exact signatures, compiles the Maven-only consumer, and assembles the unsigned bundle.

### Task 3: Review and close

**Files:** none beyond Task 2

- [ ] **Step 1: Compare branch to `master`**

Confirm the diff contains only workflow/security documentation and the contract script.

- [ ] **Step 2: Review CI**

Require all triggered checks green. Do not merge while an Important/Critical review finding remains.

- [ ] **Step 3: Update PR with RED/GREEN evidence and close #188 on merge**
