# Durex Spring Boot Platform v3 Design

## Goal

Make Spring Boot the single application runtime for Durex, remove the remaining Quarkus/CDI runtime, and expose a small Durex platform surface that composes Spring Boot for HTTP, messaging, transactions, observability, configuration, and auto-configuration while keeping build-time schema and module discovery independent from the runtime framework.

## Context

Durex has already moved its build platform toward Spring-first conventions and introduced a public plugin surface under `durex.*`. PR #132 establishes the first runtime boundary needed by this design: a framework-neutral `shared-common`, a Spring AOP adapter for Durex-specific annotations, and `durex.feature.aop`.

The repository still contains legacy Quarkus infrastructure and runtime code, including Quarkus Gradle catalogs/scripts, CDI/JAX-RS/MicroProfile annotations, Quarkus-specific reactive modules, Quarkus Redis/messaging glue, and code generation templates such as `QuarkusDaemon.vm`. Maintaining both runtimes now increases complexity without providing product value.

This design supersedes the earlier requirement to preserve Quarkus until all replacements exist. The replacement runtime is now explicitly Spring Boot.

## Primary decision

Durex adopts a **thin semantic platform over Spring Boot**.

Durex does not implement its own application runtime, HTTP stack, dependency injection container, transaction engine, event loop, Redis listener container, observation system, or configuration engine. Those responsibilities belong to Spring Boot and the Spring ecosystem.

Durex owns:

- Gradle build-platform conventions and dependency management;
- module discovery and project composition;
- build-time schema/code generation;
- a small number of Durex-specific semantic annotations where they add real domain value;
- reusable Spring Boot auto-configuration modules that connect those semantics to Spring runtime facilities;
- opinionated capability composition and diagnostics.

The architecture is:

```text
Durex Build Platform
│
├── Settings / Module Discovery
├── Dependency / Capability Model
├── Schema / Codegen
│   ├── jOOQ
│   ├── JSON Schema
│   └── future OpenAPI / Protobuf
│
├── Neutral Contracts
│   ├── DTO / Error Model
│   ├── Durex semantic annotations
│   └── messaging contracts
│
└── Spring Boot Runtime
    ├── HTTP Server
    ├── HTTP Client
    ├── AOP
    ├── Transactions
    ├── Messaging / Redis
    ├── Configuration
    ├── Observability
    └── AutoConfiguration
```

## Runtime policy

Spring Boot is the only supported application runtime.

The following runtime technologies are removed from Durex application modules:

- Quarkus;
- CDI / `javax.enterprise.*`;
- `javax.inject.*`;
- `javax.interceptor.*`;
- `javax.transaction.*`;
- JAX-RS runtime annotations;
- MicroProfile runtime annotations;
- Quarkus-specific Redis and messaging integration;
- Quarkus-specific source/code generation.

Jakarta APIs remain only where they are actual standards consumed by Spring:

- `jakarta.persistence.*` for JPA;
- `jakarta.validation.*` for Bean Validation.

No `core/shared/jakarta` namespace remains part of the target architecture.

## Public Gradle capability surface

The target public plugin surface is:

```text
Platform
├── durex.settings
└── durex.module

Module Types
├── durex.java-library
├── durex.spring-library
└── durex.spring-service

Schema
├── durex.schema.jooq
└── durex.schema.json

Runtime Features
├── durex.feature.web
├── durex.feature.http-client
├── durex.feature.aop
├── durex.feature.transaction
├── durex.feature.messaging
├── durex.feature.redis
├── durex.feature.observability
├── durex.feature.jpa
├── durex.feature.jdbc
├── durex.feature.jooq
├── durex.feature.native
└── durex.feature.lombok
```

`durex.spring-service` means only "Spring Boot executable application". It must not implicitly mean web + validation + observability + every other runtime dependency. Runtime features remain orthogonal capabilities.

Existing persistence capabilities remain valid. The new HTTP, messaging, transaction, and observability capabilities follow the same capability-engine model.

## Module discovery

Gradle module discovery remains a Durex build-time concern and must not be replaced by Spring component scanning.

The model is:

```text
Gradle time
    Durex ProjectDiscovery
        ↓
    settings.include(...)
        ↓
    projectDir / buildFileName

Runtime
    Spring Boot bean / auto-configuration discovery
```

Durex keeps both automatic and manual discovery, with **automatic discovery as the default** and manifest entries as overrides.

Normal repository layout:

```text
core/
├── music/
├── user/
└── payment/
```

A conventional module with a recognized build file is included automatically. `gradle/modules.toml` is used only when the logical Gradle path, physical path, or build file cannot be inferred canonically.

The discovery rules must stay deterministic, diagnostics-friendly, configuration-cache compatible, and independent of Spring runtime behavior.

## HTTP platform

### Server contracts

Spring MVC is the default blocking HTTP server runtime.

Durex does not introduce wrapper annotations such as `@DurexController` or `@DurexGet`. Application code uses Spring-native annotations where server-specific behavior is required.

For reusable API contracts, Spring HTTP Service Interfaces are preferred:

```java
@HttpExchange("/v1/music")
public interface MusicApi {
    @GetExchange("/{id}")
    RespData<Music> get(@PathVariable String id);

    @PostExchange
    RespData<Integer> create(@RequestBody Music music);
}
```

A server implementation may implement the contract:

```java
@RestController
class MusicController implements MusicApi {
    ...
}
```

This keeps endpoint shape in one interface while allowing Spring to own routing, conversion, validation, error handling, and observation.

### HTTP clients

`durex.feature.http-client` provides Spring's declarative HTTP client support. It does not add a custom Durex HTTP client protocol.

The target model is:

```text
@HttpExchange interface
        ↓
Spring HTTP service proxy
        ↓
RestClient (default blocking backend)
        or
WebClient (reactive backend when explicitly required)
```

Client configuration belongs in Spring Boot `@ConfigurationProperties` and auto-configuration, including base URLs, timeouts, codecs, authentication hooks, and observation.

Durex may provide naming/configuration conventions, but not a separate runtime transport implementation.

## Annotation and AOP model

Only annotations with Durex-specific semantics are retained.

Examples:

```text
@NullChecker
@ValueChecker
```

These annotations remain framework-neutral declaration metadata. Spring AOP is their runtime backend.

Durex must not duplicate Spring annotations for generic runtime concepts:

```text
Use Spring directly:
@Transactional
@Cacheable
@Async
@Scheduled
@Observed
@PreAuthorize
@RestController
@GetMapping
@ConfigurationProperties
```

A Durex annotation is justified only when its semantic contract is not already expressed by a Spring or Jakarta standard annotation.

The #132 pattern is the reference model:

```text
Durex semantic annotation
        ↓
Spring Aspect / interceptor adapter
        ↓
Spring proxy runtime
```

## Auto-configuration model

Spring Boot auto-configuration is the standard integration mechanism for all reusable Durex runtime features.

A Durex runtime library should expose behavior through:

```java
@AutoConfiguration
@ConditionalOnClass(...)
@EnableConfigurationProperties(...)
class DurexXxxAutoConfiguration {
}
```

Beans supplied by Durex should normally use `@ConditionalOnMissingBean` so applications can override defaults.

Auto-configuration classes are registered through Spring Boot's auto-configuration imports metadata.

Applying a Durex feature should therefore follow this flow:

```text
durex.feature.messaging
        ↓
Gradle dependency added
        ↓
Spring Boot sees auto-configuration
        ↓
Messaging runtime beans appear
```

Application build files should not need to repeat `@Configuration` boilerplate for infrastructure that belongs to the feature module.

## Transaction model

Spring transaction management replaces `javax.transaction.Transactional` and Quarkus transaction integration.

`durex.feature.transaction` activates the dependencies required for Spring-managed transactions. Business code uses Spring's `@Transactional` where transaction semantics are required.

Durex does not create a custom transaction annotation or transaction manager abstraction.

Spring modules must not require `javax.transaction`, CDI, or `javax.inject` merely to compile business services or repositories.

Service and repository construction should use one of two Spring-native forms:

1. constructor-injected Spring components where component scanning is appropriate; or
2. explicit `@Bean` factory methods in auto-configuration/application configuration when the class should remain framework-neutral.

Framework-neutral classes are preferred for reusable domain/service/repository code where practical.

## Messaging platform

### Objective

Replace the existing Redis messaging generator/template system with a runtime registration model built on Spring Boot and Spring Data Redis.

The current concepts worth preserving are semantic concepts, not the existing code generator:

- incoming message;
- outgoing message;
- topic/channel;
- processor;
- forwarder;
- remote invocation only if a concrete use case still requires it.

### Neutral messaging contract

The target neutral model is intentionally small:

```text
Message<T>
MessageId
Topic
Incoming
Outgoing
```

Durex-specific annotations are allowed only if they materially simplify handler/forwarder declaration.

Example:

```java
@Incoming("music.created")
void handle(MusicCreated event) {
}
```

Processor/forwarder form:

```java
@Incoming("raw.music")
@Outgoing("normalized.music")
Music normalize(MusicRaw raw) {
    ...
}
```

### Runtime implementation

No Java source generation is required for ordinary listeners or forwarders.

The Spring adapter performs:

```text
receive
  ↓
decode / convert
  ↓
resolve handler metadata
  ↓
invoke Spring bean method
  ↓
optional result
  ↓
encode / publish to outgoing destination
```

For Redis, the adapter uses Spring Data Redis listener/container infrastructure and Spring Boot configuration.

The implementation must support runtime registration from bean metadata rather than generating Quarkus runners/listeners from Velocity templates.

### Delivery semantics

The first implementation supports explicit at-most-once/pub-sub semantics and Redis Stream semantics as separate adapters. It must not pretend they have identical delivery guarantees.

Retries, dead-letter handling, acknowledgment, consumer groups, ordering, and idempotency are adapter-level policies and are not hidden behind one ambiguous generic abstraction.

## Schema and code generation

Schema remains build-time and framework-neutral.

The namespace is:

```text
durex.schema.jooq
durex.schema.json
```

Future additions may include:

```text
durex.schema.openapi
durex.schema.protobuf
```

Schema plugins may generate Java source, metadata, or descriptors, but they do not depend on Spring Boot runtime types unless the generated artifact is explicitly a Spring-facing contract.

The intended flow is:

```text
schema source
    ↓
Durex Gradle schema plugin
    ↓
generated DTO / jOOQ model / contract metadata
    ↓
Java compilation
    ↓
Spring runtime consumes generated output
```

JSON Schema code generation should move under `durex.schema.json` rather than remain a module-local use of the raw jsonschema2pojo plugin.

## Observability

`durex.feature.observability` composes Spring Boot Actuator and Micrometer Observation.

Durex does not create its own metrics/tracing API.

HTTP server, HTTP client, messaging, database, and custom Durex AOP adapters should participate in Spring/Micrometer observation when the feature is active.

## Error handling and configuration

Shared error models remain neutral Java types.

HTTP exception mapping belongs to a Spring Web adapter (`@ControllerAdvice` / `@ExceptionHandler`) rather than the neutral common module.

Runtime configuration uses type-safe Spring Boot `@ConfigurationProperties`. Durex should prefer grouped properties for each feature over ad-hoc environment lookups.

## Quarkus removal scope

The target state contains no supported Quarkus runtime or build integration.

Removal includes, after Spring replacements are present in the same migration series:

- Quarkus Gradle version catalogs and dependency scripts;
- Quarkus Gradle plugin usage;
- Quarkus GitHub Actions workflow;
- Quarkus/JAX-RS/MicroProfile controllers and configuration;
- `music-reactive` when it exists only as a Quarkus runtime variant;
- `core/shared/jakarta/*` runtime modules;
- Quarkus-specific Redis resources;
- `QuarkusDaemon` and other Quarkus-specific messaging templates/generators;
- legacy compile-only CDI/inject/interceptor/transaction dependencies used only by Quarkus;
- obsolete tests and examples whose only purpose is validating Quarkus.

Historical documentation may remain when clearly historical, but active README/build docs must describe Spring Boot as the only runtime.

## Root build modernization

The old restriction preventing full root Gradle modernization existed because legacy Quarkus modules required the older root build path.

Once Quarkus build/runtime modules are removed, the repository root may converge on the same modern Gradle/JDK baseline used by the Durex Build Platform.

This modernization must happen only after the legacy root build no longer has Quarkus-specific consumers.

## Migration phases

### Phase A — Runtime foundation

Deliverables:

- merge/consume #132 neutral common and Spring AOP foundation;
- remove CDI / `javax.inject` / `javax.transaction` from Spring build graphs;
- introduce `durex.feature.transaction` and auto-configuration conventions;
- convert remaining Spring service/repository wiring to Spring-native construction;
- establish a generic pattern for Durex Spring Boot auto-configuration modules;
- keep tests proving Durex semantic AOP behavior.

Exit condition: the Spring application graph compiles and runs without Quarkus/CDI runtime dependencies.

### Phase B — HTTP platform

Deliverables:

- introduce `durex.feature.web` and `durex.feature.http-client`;
- move Music HTTP API to reusable Spring HTTP Service Interfaces where appropriate;
- verify server implementation and generated/proxied client behavior;
- add type-safe client configuration and observation hooks;
- move exception mapping into a Spring Web adapter.

Exit condition: one HTTP contract is exercised end-to-end as both server contract and client proxy without Durex custom HTTP transport code.

### Phase C — Messaging platform

Deliverables:

- define the neutral messaging contract;
- implement Spring/Redis listener registration;
- implement processor/forwarder semantics;
- preserve Redis pub/sub and Redis Stream delivery semantics explicitly;
- migrate one real messaging path;
- delete the replaced Velocity/source-generator runtime glue.

Exit condition: a message can be received, converted, processed, optionally forwarded, and observed through Spring runtime without generated Quarkus code.

### Phase D — Schema, discovery, and final legacy removal

Deliverables:

- introduce `durex.schema.json`;
- migrate JSON Schema generation to the Durex schema namespace;
- simplify module discovery to auto-first/manual-override behavior while preserving deterministic diagnostics;
- delete remaining Quarkus modules/catalogs/scripts/workflows;
- remove `core/shared/jakarta` as an active module family;
- modernize the root Gradle baseline after the legacy runtime is gone;
- update active documentation.

Exit condition: repository CI contains no Quarkus job, active build graph contains no Quarkus dependency/plugin, and Spring Boot is the sole application runtime.

## Testing strategy

Each phase must have its own regression gate rather than relying on one final migration test.

Required coverage:

```text
Build Platform
- public capability smoke tests
- dependency diagnostics
- module discovery tests
- configuration-cache tests

Runtime Foundation
- Spring context startup
- AOP semantic tests
- transaction integration tests
- no-CDI dependency guard

HTTP
- MockMvc/server tests
- declarative HTTP client tests
- serialization/validation/error mapping

Messaging
- handler registration
- conversion
- forwarding
- Redis pub/sub integration
- Redis Stream consumer-group integration
- failure/retry policy tests where enabled

Schema
- deterministic generated sources
- compile integration
- configuration-cache compatibility
```

CI should add negative guards that fail if active Spring build descriptors reintroduce Quarkus, `javax.enterprise`, `javax.inject`, `javax.interceptor`, or `javax.transaction` dependencies.

## Non-goals

This design does not:

- build a Durex DI container;
- build a Durex HTTP transport;
- build a Durex transaction manager;
- build a Durex observability stack;
- preserve Quarkus compatibility;
- provide one abstraction that hides the semantic differences between Redis pub/sub and Streams;
- replace standard Spring annotations merely for naming consistency;
- move Gradle module discovery into the Spring runtime.

## Success criteria

The migration is complete when:

1. Spring Boot is the only application runtime in active code and CI.
2. `durex.spring-service` is a small executable-app convention, with runtime functionality activated by independent features.
3. HTTP server/client behavior is based on Spring HTTP facilities.
4. messaging listeners/processors/forwarders are registered at runtime through Spring rather than generated as Quarkus source.
5. Durex-specific annotations are neutral metadata with Spring adapters.
6. schema generation is under `durex.schema.*` and remains build-time/framework-neutral.
7. module discovery remains automatic-first with explicit override support.
8. reusable runtime integrations are delivered through Spring Boot auto-configuration.
9. active builds contain no Quarkus/CDI runtime dependency or workflow.
10. the root build can move to the modern Gradle/JDK baseline without legacy Quarkus constraints.
