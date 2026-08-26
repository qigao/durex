# Durex 0.1 HTTP Error Contract Design

## Goal

Finalize the HTTP/error model before the first `0.1.0` release so the supported Maven API is intentionally small, immutable, and semantically correct.

## Problems in the current contract

- `ErrorResponse` uses Lombok `@Data/@With`, which exposes setters, withers, generated equality helpers, and constructors as frozen JVM API.
- `RespData` exposes a Lombok builder and mutable setters; the nested builder is not explicitly inventoried in the 0.1 public-surface manifest.
- `ApiException.getErrorResponse()` creates a new UUID and timestamp on every call, so one exception does not represent one stable error occurrence.
- `ErrorResponse.caller` sends an implementation class/method name to HTTP clients.
- `EMPTY_PARAM` and `VALUE_ERROR` are client errors but currently map to HTTP 500.

No GitHub Release exists yet, so this is a pre-first-release contract correction rather than a compatibility break after publication.

## Public DTO design

### `ErrorResponse`

Replace the mutable Lombok bean with an immutable Java record:

```java
public record ErrorResponse(
    UUID errorId,
    String message,
    ErrorCode errorCode,
    LocalDateTime timestamp) implements Serializable {}
```

The record deliberately has no `caller` component. Its canonical constructor, four accessors, and record equality/hash/string methods are part of the deliberate API surface.

### `RespData<T>`

Replace the mutable Lombok bean/builder with an immutable record:

```java
public record RespData<T>(T result, ErrorResponse error) {
  public static <T> RespData<T> of(T result, ErrorResponse error) {
    return new RespData<>(result, error);
  }
}
```

`of(...)` remains the preferred construction API used by Durex controllers and existing examples. Record serialization/deserialization is verified through the real Spring Boot HTTP client/server test.

## Stable exception occurrence

`ApiException` constructs its `ErrorResponse` once in the exception constructor and returns that same value from `getErrorResponse()`. The structured constructor stores the supplied `ErrorCode`; the message-only constructor uses no explicit code. No stack-walking caller metadata is captured.

## HTTP status semantics

`DurexHttpExceptionHandler` maps the existing `ErrorCode` values explicitly:

- `ENTITY_NOT_FOUND` -> 404 Not Found
- `EMPTY_PARAM` -> 400 Bad Request
- `VALUE_ERROR` -> 400 Bad Request
- `SAVE_ERROR`, `UPDATE_ERROR`, `DELETE_ERROR`, `OPERATION_FAILED`, `UNKNOWN_ERROR`, `NOTHING_FAILED`, or null -> 500 Internal Server Error

`NOTHING_FAILED` is retained because it is already an enum value, but throwing an `ApiException` with it is semantically inconsistent and therefore not treated as success.

## Public-surface handling

This change deliberately rewrites the pre-release 0.1 signature baseline for `ErrorResponse` and `RespData`. The staged-JAR baseline must contain the exact final record signatures and must not contain:

- `ErrorResponse.getCaller/setCaller/withCaller`
- mutable DTO setters/withers
- `RespData.builder()` or generated builder type signatures

`DurexHttpExceptionHandler` remains `scope=api` for 0.1 because the current publication/Javadoc contract expects each published module to have an explicit supported API and external users may invoke/replace the handler directly. Its auto-configuration remains `scope=runtime`.

## Testing

RED tests are added before production changes:

1. one `ApiException` must return the same structured error occurrence on repeated inspection;
2. `ErrorResponse` and `RespData` must be records and expose no legacy caller/builder/mutator methods;
3. real HTTP not-found JSON must not contain `caller`;
4. handler contract must map `EMPTY_PARAM` and `VALUE_ERROR` to 400 while preserving 404/500 cases.

GREEN then updates implementation, consumer usage, docs, and the staged-JAR signature baseline.

## Non-goals

- No redesign of Music business errors beyond HTTP status classification.
- No new validation framework.
- No change to Redis messaging API.
- No post-0.1 compatibility promise for the removed pre-release accidental signatures.

Closes #190 when merged.