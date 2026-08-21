# Durex Public Plugin API Design

## Goal

Define one small, intentional public Gradle plugin surface and move bootstrap/composition details behind a `durex.internal.*` namespace. The build platform is Spring-first, while schema/code-generation plugins remain framework-neutral.

## Public plugin surface

The supported user-facing plugin IDs are:

- Platform: `durex.settings`, `durex.module`
- Module types: `durex.java-library`, `durex.spring-library`, `durex.spring-service`
- Schema: `durex.schema.jooq`
- Features: `durex.feature.aop`, `durex.feature.transaction`, `durex.feature.web`, `durex.feature.http-client`, `durex.feature.jdbc`, `durex.feature.jooq`, `durex.feature.jpa`, `durex.feature.redis`, `durex.feature.native`, `durex.feature.lombok`

Public plugins are the only IDs application/module build files should apply directly.

## Internal plugin surface

Implementation-only plugins use an explicit internal namespace:

- `durex.internal.build-logic-settings`
- `durex.internal.build-logic`
- `durex.internal.catalog`
- `durex.internal.java-base`
- `durex.internal.spring-base`
- `durex.internal.fixture`

Internal plugins may be composed by public plugins and by build-logic bootstrap code, but must not be recommended in user-facing diagnostics.

## Breaking rename policy

This is a design-stage cleanup, so no compatibility aliases are retained.

| Old ID | New ID |
| --- | --- |
| `durex.jooq-schema` | `durex.schema.jooq` |
| `durex.catalog` | `durex.internal.catalog` |
| `durex.java-base` | `durex.internal.java-base` |
| `durex.spring-base` | `durex.internal.spring-base` |
| `durex.build-logic-settings` | `durex.internal.build-logic-settings` |
| `durex.build-logic` | `durex.internal.build-logic` |
| `com.acme.durex.fixture` | `durex.internal.fixture` |

The old IDs must stop resolving. This prevents accidental long-term support commitments to implementation details.

## Composition rules

`durex.java-library` composes `durex.internal.java-base`; `durex.spring-library` and `durex.spring-service` compose `durex.internal.spring-base`; the Spring base composes the Java base. The internal Java base installs `durex.module`.

`durex.spring-service` defines an executable Spring Boot application only. It does not implicitly provide WebMVC, HTTP clients, observability, persistence, Redis, or transaction semantics. Those concerns are enabled through explicit `durex.feature.*` capabilities.

`durex.schema.jooq` remains independent from Spring module types. It installs only the internal dependency catalog needed to resolve jOOQ build-time dependencies and wires `jooqCodegen` into Java compilation.

`durex.feature.*` plugins are orthogonal capabilities layered on module types rather than combinatorial module plugins. Runtime capabilities compose Spring Boot/Spring Framework facilities; they do not reimplement those runtimes.

## Diagnostics

User-facing errors should recommend public recovery actions such as applying `durex.module`; they should not tell users to apply `durex.internal.catalog`. Bootstrap-only errors may describe the bootstrap phase without exposing an implementation plugin ID.

## Validation

The contract is verified through:

1. a namespace contract for public/internal plugin files and removed legacy IDs;
2. dedicated capability fixtures for AOP, transaction, web, and HTTP client features;
3. the jOOQ schema smoke fixture for `durex.schema.jooq`;
4. a negative fixture proving `durex.jooq-schema` no longer resolves;
5. full Durex Build Platform, Spring Music, and Spring Native regression gates.

## Non-goals

The plugin namespace contract does not define application runtime implementations. Spring runtime behavior is specified by the Spring Boot Platform v3 architecture. Historical design/plan documents are not rewritten; this document supersedes their plugin naming where they differ.
