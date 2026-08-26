# Durex Soft-Delete Write Contract Design

## Goal

Make the Music reference implementation treat a soft-deleted row as absent for normal repository updates and deletes, matching the read-side contract already enforced by `find*` methods.

## Contract

For `MUSIC` and `PLAYLIST` rows with `DELETED_FLAG = 1`:

- normal single-row update returns `0` and must not modify the row;
- batch update reports `0` for that element and must not modify the row;
- delete-by-id/title/wildcard/list returns no affected row for already-deleted data;
- repeated delete does not refresh `DELETE_TIME`;
- service methods keep their existing behavior: a zero-row single delete/update becomes the existing structured `DELETE_ERROR` / `UPDATE_ERROR`.

Active rows keep the current behavior.

## Repository implementation

All soft-delete-aware write queries include the same `NOT_DELETED` condition used by reads. Delete queries simply add the condition. Update paths use explicit jOOQ update queries so `NOT_DELETED` is part of the SQL predicate instead of relying on a pre-read check that would introduce a race.

Batch update is built from the same conditional update query shape and executed as a jOOQ batch so each returned count preserves per-record semantics.

## Tests

The existing H2 Spring repository integration fixture already contains active and deleted Music/Playlist rows. Extend it to assert:

- repeated delete returns `0` and keeps the original `DELETE_TIME`;
- update of a deleted row returns `0` and leaves stored data unchanged;
- active-row behavior remains covered by existing tests.

Tests inspect the database directly through `DSLContext` for the deleted-row assertions because normal repository reads intentionally hide deleted rows.

## Non-goals

- no restore/undelete API;
- no physical delete;
- no optimistic-lock/versioning redesign;
- no change to the public Maven surface (Music remains repository-internal).

Closes #192 when merged.