# Spring Runtime Interceptor Migration Design

## Goal

Move Durex method-interceptor behavior from Jakarta/CDI runtime semantics to Spring AOP without introducing a permanent dual-runtime abstraction layer.

## Boundary

The Spring migration uses three distinct layers:

```text
business code
    -> neutral annotations / error model
    -> Spring interceptor adapter
    -> Spring AOP runtime
```

The neutral API lives under `core/shared/common`. It contains the existing `NullChecker` and `ValueChecker` semantic annotations plus the error/response types needed by migrated application code. These annotations must not depend on CDI, Jakarta/Javax Interceptors, Spring, or AspectJ.

The Spring implementation lives under `core/shared/spring/interceptor`. It depends on the neutral common module, activates the Durex AOP capability, registers Spring `@Aspect` implementations, and exports them through Spring Boot auto-configuration.

The existing `core/shared/jakarta/common` module remains untouched for the legacy Quarkus graph. It is not a backend Durex intends to evolve; it is deleted when the Quarkus replacement is complete.

## Build capability

`durex.feature.aop` is a public Spring capability available to `SPRING_LIBRARY` and `SPRING_SERVICE` modules. The capability supplies the Spring Boot 4.1 AspectJ starter and participates in the normal Durex capability model and diagnostics.

The user-facing DSL is:

```gradle
durex {
    aop()
}
```

Spring Boot 4.1 renamed its official AOP starter to `spring-boot-starter-aspectj`; the Durex dependency alias remains semantic (`spring-aop`) so build files do not encode framework artifact naming.

## Interceptor semantics

### NullChecker

`@NullChecker` is an after-success result policy. The Spring aspect executes the target invocation and throws `ApiException("No Data Returned", OPERATION_FAILED)` when the result is null or structurally empty (empty character sequence, collection, map, optional, or array).

### ValueChecker

`@ValueChecker` is an around/result policy. Before invoking, it verifies that the annotation's declared `type` matches the Java method return type. After invoking, it compares the result's string value to the configured sentinel and throws the configured exception/message when they match.

These rules intentionally preserve the behavior of the existing Jakarta interceptors while changing only the runtime implementation.

## Spring registration

The interceptor module uses Spring Boot auto-configuration rather than application package component scanning. `DurexInterceptorAutoConfiguration` registers both aspects and backs off when an application supplies its own bean of the same aspect type.

Spring Boot's normal AOP auto-configuration is responsible for proxy creation. Business services do not import `ProceedingJoinPoint`, Spring AOP annotations, or Spring runtime types.

## Migration graph

The Spring Music module graph changes from:

```text
shared-common -> core/shared/jakarta/common
```

to:

```text
shared-common              -> core/shared/common
shared-spring-interceptor  -> core/shared/spring/interceptor
music                      -> both
```

The Quarkus root graph continues using `core/shared/jakarta/common` until retirement.

## Validation

The migration is accepted when:

1. the AOP capability fixture resolves `aop` as a Durex capability and brings the Spring AOP runtime onto `runtimeClasspath`;
2. the public-plugin namespace contract includes `durex.feature.aop`;
3. Spring Music starts with the interceptor auto-configuration;
4. `MusicSpringInterceptorTest` proves `@NullChecker` and `@ValueChecker` execute on Spring-managed `MusicService` calls;
5. existing Spring Music tests and build-platform tests remain green;
6. legacy Quarkus behavior remains unchanged.

## Next migration slice

This slice deliberately does not convert `javax.inject`, CDI scopes, or `javax.transaction.Transactional` in shared service/repository source. The next slice removes those runtime annotations from Spring business/persistence code, moves construction fully into Spring configuration, and replaces transaction semantics with Spring transaction management. Once all Spring replacements are complete, the Jakarta/Quarkus modules can be removed rather than maintained as a parallel runtime.
