# Durex Mandatory PR Gate Design

## Goal

Expose one always-present pull-request check, `Durex Gate`, that represents the result of every other Durex PR workflow actually triggered for the same exact head SHA.

This solves the branch-protection problem created by path-filtered workflows: a workflow omitted by its path filter produces no check and therefore must not itself be configured as a universal required status. The aggregate gate always exists.

## Aggregation model

`Durex Gate` runs on every `pull_request` with read-only permissions (`actions: read`, `contents: read`, `pull-requests: read`). It does not execute application tests again.

The gate queries GitHub Actions for `event=pull_request` runs whose `head_sha` equals `github.event.pull_request.head.sha`, excludes its own `Durex Gate` run, and observes the remaining workflow-run set.

It fails immediately if any discovered exact-head workflow completes with a conclusion other than `success`. It succeeds only when:

1. every discovered exact-head workflow is completed successfully; and
2. the discovered workflow-run identity set has remained unchanged for six consecutive five-second scans.

The stabilization window prevents the aggregate check from succeeding before a sibling workflow run has been registered by GitHub. Queue/runtime duration does not weaken this: once a sibling run exists, the gate continues polling until it completes.

A hard timeout fails closed rather than silently passing an indefinitely incomplete PR.

## Why aggregate actual runs instead of duplicating path logic

The individual workflow files remain the authoritative owners of their `paths` filters. `Durex Gate` intentionally does not maintain a second copy of those glob rules. If a workflow is triggered for the exact PR head, it must pass; if GitHub legitimately omits it because its path filter did not match, the gate does not invent a pending required check.

## Repository protection

After this workflow is merged, `master` should require the `Durex Gate` status and disallow force pushes. Human-review requirements remain optional for this solo-maintainer repository.

The currently connected GitHub API integration exposes ruleset/branch-protection reads but no write action, so repository settings cannot be mutated from this session. #189 remains open until that final GitHub setting is applied and verified.

## Non-goals

- no duplicate build/test matrix;
- no replacement of specialized workflow names or summaries;
- no change to runtime/public API;
- no assumption that absent path-filtered workflows are failures.
