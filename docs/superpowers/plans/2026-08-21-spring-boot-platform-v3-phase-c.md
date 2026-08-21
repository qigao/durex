# Durex Spring Boot Platform v3 Phase C Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the Quarkus/CDI/Velocity Redis messaging implementation with Spring Boot runtime registration for Pub/Sub receive, return-value forwarding, and Redis Stream consumer groups without generated Java source.

**Architecture:** Spring Data Redis owns Pub/Sub listener discovery through `@RedisListener` and `RedisMessageListenerContainer`; Durex does not duplicate that annotation. Durex adds only semantics Spring Data Redis does not provide directly: `@Outgoing` for Pub/Sub return-value forwarding, `@RedisStreamListener` for declarative Redis Stream consumer registration, and `@RedisStreamOutgoing` for Stream forwarding. The Spring Redis adapter is delivered through Boot auto-configuration and uses Spring Data Redis containers at runtime; the old annotation processor, CDI interceptor, Redisson runtime wrappers, and Velocity templates are deleted after the replacement integration tests pass.

**Tech Stack:** Gradle 9.1 Durex build platform, Java 25, Spring Boot 4.1, Spring Framework 7 AOP/Messaging, Spring Data Redis 4.1, Lettuce, Redis 7 service container in GitHub Actions, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-08-21-durex-spring-boot-platform-v3-design.md`

## Global Constraints

- Spring Boot is the only supported application runtime.
- Pub/Sub receive uses Spring Data Redis `@RedisListener`; Durex must not introduce a duplicate `@Incoming` annotation for ordinary Pub/Sub.
- Redis Pub/Sub and Redis Streams remain separate delivery models and must not be selected implicitly from an empty/non-empty group name.
- Durex must not generate listener/runner/executor Java source for messaging.
- Durex must not depend on CDI, `javax.inject`, `javax.interceptor`, Quarkus, Jandex, Velocity, or Redisson in the new Spring messaging graph.
- `durex.feature.messaging` is a generic Spring messaging capability; Redis transport remains the independent `durex.feature.redis` capability.
- Durex-specific annotations contain only semantic metadata and no Spring/CDI meta-annotations.
- `@Outgoing` publishes a non-null successful return value to Redis Pub/Sub; a `null` result is not published.
- `@RedisStreamOutgoing` publishes a non-null successful return value to a Redis Stream as a `payload` field.
- `@RedisStreamListener` explicitly declares `stream`, `group`, `consumer`, and acknowledgement behavior. Consumer groups are infrastructure and are not silently created by the runtime adapter.
- Remote-service/RPC generation is not carried forward in Phase C. `RemoteService`, `RemoteServiceApi`, and `QuarkusDaemon` are deletion targets.
- Phase C does not implement Kafka, RabbitMQ, a generic broker SPI, retry/DLQ policy, or exactly-once semantics.

---

## Target Module Structure

```text
build-logic/
├── src/main/groovy/durex.feature.messaging.gradle
├── src/main/groovy/com/github/durex/gradle/capability/BuiltinCapabilities.groovy
└── tests/messaging-feature-smoke/

core/shared/messaging/
├── api/
│   ├── build.spring.gradle
│   └── src/main/java/com/github/durex/messaging/annotation/
│       ├── Outgoing.java
│       ├── RedisStreamListener.java
│       └── RedisStreamOutgoing.java
└── spring-redis/
    ├── build.spring.gradle
    ├── src/main/java/com/github/durex/messaging/spring/redis/
    │   ├── DurexRedisMessagingAutoConfiguration.java
    │   ├── RedisOutgoingAspect.java
    │   ├── RedisStreamOutgoingAspect.java
    │   ├── RedisStreamListenerRegistrar.java
    │   └── RedisMessagingCodec.java
    ├── src/main/resources/META-INF/spring/
    │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
    └── src/test/java/com/github/durex/messaging/spring/redis/
        └── RedisMessagingIntegrationTest.java

migration/spring-messaging/
├── settings.gradle
└── modules.toml

.github/workflows/spring-messaging.yml
```

The existing `core/shared/messaging/messaging-api-redis` and `messaging-api-redis-example` trees are transitional legacy code only. They are deleted in Task 5 after runtime-equivalent receive/forward tests are green.

---

### Task 1: Add the generic messaging capability

**Files:**
- Modify: `gradle/dependencies/spring.toml`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/capability/BuiltinCapabilities.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy`
- Create: `build-logic/src/main/groovy/durex.feature.messaging.gradle`
- Create: `build-logic/tests/messaging-feature-smoke/settings.gradle`
- Create: `build-logic/tests/messaging-feature-smoke/build.gradle`
- Modify: `build-logic/tests/verify-plugin-namespaces.sh`
- Modify: `.github/workflows/durex-plugin-api.yml`

**Interfaces:**
- Produces capability id `messaging`, plugin id `durex.feature.messaging`, and DSL method `durex.messaging()`.
- Redis remains an independent capability and is not implicitly enabled by `messaging()`.

- [ ] **Step 1: Write the failing capability fixture**

Create `build-logic/tests/messaging-feature-smoke/build.gradle`:

```gradle
plugins { id 'durex.spring-library' }

durex { messaging() }

import com.github.durex.gradle.model.DurexModuleModel
assert extensions.getByType(DurexModuleModel).capabilities.get().contains('messaging')

tasks.register('verifyMessagingCapability')
```

Use the same settings bootstrap as `transaction-feature-smoke`.

- [ ] **Step 2: Verify RED**

Run:

```bash
gradle -p build-logic/tests/messaging-feature-smoke verifyMessagingCapability --stacktrace
```

Expected: FAIL because `messaging()` and `durex.feature.messaging` do not exist.

- [ ] **Step 3: Add the managed Spring Messaging dependency**

Add:

```toml
[libraries.spring-messaging]
module = "org.springframework:spring-messaging"
platform = "spring"
```

- [ ] **Step 4: Register the capability**

Add to `BuiltinCapabilities.groovy`:

```groovy
static final CapabilitySpec MESSAGING = CapabilitySpec.builder('messaging')
        .allow(ModuleKind.SPRING_LIBRARY, ModuleKind.SPRING_SERVICE)
        .dependency('implementation', 'spring-messaging')
        .build()
```

Register `MESSAGING` alongside the existing built-ins.

- [ ] **Step 5: Add plugin and DSL shortcut**

Create `durex.feature.messaging.gradle` using the same `DurexCapabilitySupport.registerAndEnable` pattern as transaction/web.

Add to `DurexExtension`:

```groovy
void messaging() {
    project.pluginManager.apply('durex.feature.messaging')
}
```

- [ ] **Step 6: Extend public namespace and CI smoke coverage**

Require `durex.feature.messaging.gradle` in `verify-plugin-namespaces.sh` and add:

```yaml
- name: Verify messaging feature capability
  run: gradle -p build-logic/tests/messaging-feature-smoke verifyMessagingCapability dependencies --configuration runtimeClasspath --stacktrace
```

- [ ] **Step 7: Verify GREEN and commit**

Run the fixture and namespace contract. Commit:

```bash
git commit -m "feat: add Spring messaging capability"
```

---

### Task 2: Replace the legacy messaging annotations with a small runtime API

**Files:**
- Create: `core/shared/messaging/api/build.spring.gradle`
- Create: `core/shared/messaging/api/src/main/java/com/github/durex/messaging/annotation/Outgoing.java`
- Create: `core/shared/messaging/api/src/main/java/com/github/durex/messaging/annotation/RedisStreamListener.java`
- Create: `core/shared/messaging/api/src/main/java/com/github/durex/messaging/annotation/RedisStreamOutgoing.java`
- Create: `core/shared/messaging/api/src/test/java/com/github/durex/messaging/annotation/MessagingAnnotationContractTest.java`
- Create: `migration/spring-messaging/settings.gradle`
- Create: `migration/spring-messaging/modules.toml`

**Interfaces:**
- `@Outgoing(String value)` means publish a successful non-null method result to one Redis Pub/Sub destination when a Spring Redis adapter is active.
- `@RedisStreamOutgoing(String value)` means append a successful non-null method result to one Redis Stream.
- `@RedisStreamListener(stream, group, consumer, autoAck)` declares a Redis Stream consumer subscription.

- [ ] **Step 1: Write annotation contract tests first**

Test that all three annotations have `RetentionPolicy.RUNTIME`, target methods only, and do not carry any annotation whose package begins with `javax.`, `jakarta.enterprise`, or `org.springframework`.

Example assertion:

```java
assertEquals(RetentionPolicy.RUNTIME,
    Outgoing.class.getAnnotation(Retention.class).value());
assertTrue(Arrays.stream(Outgoing.class.getAnnotations())
    .map(a -> a.annotationType().getName())
    .noneMatch(name -> name.startsWith("javax.") || name.startsWith("jakarta.enterprise")));
```

- [ ] **Step 2: Implement the minimal annotations**

`Outgoing.java`:

```java
@Retention(RUNTIME)
@Target(METHOD)
public @interface Outgoing {
  String value();
}
```

`RedisStreamOutgoing.java`:

```java
@Retention(RUNTIME)
@Target(METHOD)
public @interface RedisStreamOutgoing {
  String value();
}
```

`RedisStreamListener.java`:

```java
@Retention(RUNTIME)
@Target(METHOD)
public @interface RedisStreamListener {
  String stream();
  String group();
  String consumer();
  boolean autoAck() default false;
}
```

Annotation string attributes may contain Spring property placeholders; the runtime adapter resolves them through `Environment.resolvePlaceholders`.

- [ ] **Step 3: Keep the API module runtime-neutral**

`build.spring.gradle`:

```gradle
plugins { id 'durex.java-library' }
```

No Spring, Redis, CDI, Jackson, Reactor, Redisson, or Velocity dependency is allowed.

- [ ] **Step 4: Add a dedicated Spring messaging migration build**

`settings.gradle` mirrors `migration/spring-music` and points `durexSettings.modulesManifest` to its local `modules.toml`.

Initial `modules.toml` includes:

```toml
[discovery]
mode = "manual"

[[module]]
name = "messaging-api"
path = "core/shared/messaging/api"
build-file = "build.spring.gradle"

[[module]]
name = "messaging-spring-redis"
path = "core/shared/messaging/spring-redis"
build-file = "build.spring.gradle"
```

The adapter module is introduced in Task 3, so the migration graph is intentionally RED until that task lands.

- [ ] **Step 5: Verify annotation API and commit**

Run the API tests directly once the migration graph contains only existing modules or compile the API module from a focused fixture. Commit:

```bash
git commit -m "refactor: define runtime messaging annotations"
```

---

### Task 3: Implement Pub/Sub receive and return-value forwarding with Spring Redis

**Files:**
- Create: `core/shared/messaging/spring-redis/build.spring.gradle`
- Create: `core/shared/messaging/spring-redis/src/main/java/com/github/durex/messaging/spring/redis/RedisMessagingCodec.java`
- Create: `core/shared/messaging/spring-redis/src/main/java/com/github/durex/messaging/spring/redis/RedisOutgoingAspect.java`
- Create: `core/shared/messaging/spring-redis/src/main/java/com/github/durex/messaging/spring/redis/DurexRedisMessagingAutoConfiguration.java`
- Create: `core/shared/messaging/spring-redis/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Create: `core/shared/messaging/spring-redis/src/test/java/com/github/durex/messaging/spring/redis/RedisPubSubIntegrationTest.java`

**Interfaces:**
- Consumes `@Outgoing` from `messaging-api`.
- Receivers use Spring Data Redis `@RedisListener` directly.
- Uses one Durex-owned `RedisSerializer<Object>` bean only for messaging payload encoding; it does not replace the application's default RedisTemplate serializer.

- [ ] **Step 1: Write a real Redis Pub/Sub RED integration test**

The test application contains:

```java
@Component
class PubSubPipeline {
  private final BlockingQueue<NormalizedEvent> received = new LinkedBlockingQueue<>();

  @RedisListener(topic = "events.normalized", consumes = "application/json")
  void receive(NormalizedEvent event) {
    received.add(event);
  }

  @Outgoing("events.normalized")
  NormalizedEvent normalize(RawEvent event) {
    return new NormalizedEvent(event.id(), event.value().trim());
  }
}
```

The test calls `pipeline.normalize(...)`, waits for `received.poll(5, SECONDS)`, and asserts the POJO was delivered through Redis. Before the adapter exists the test fails because the return value is never published.

- [ ] **Step 2: Create the Spring Redis adapter module**

`build.spring.gradle`:

```gradle
plugins { id 'durex.spring-library' }

durex {
    messaging()
    redis()
    aop()
}

dependencies {
    api project(':messaging-api')
}
```

- [ ] **Step 3: Add one safe JSON codec shared by outgoing and listener conversion**

`RedisMessagingCodec` wraps a Spring Data `RedisSerializer<Object>` built with `GenericJacksonJsonRedisSerializer.builder().build()` **without enabling default typing**.

Public methods:

```java
byte[] encode(Object value);
<T> T decode(byte[] bytes, Class<T> targetType);
String encodeString(Object value);
<T> T decodeString(String value, Class<T> targetType);
```

`encodeString`/`decodeString` use UTF-8 over the JSON bytes.

- [ ] **Step 4: Configure Spring's native `@RedisListener` converter**

In auto-configuration, expose a `RedisListenerConfigurer` that adds a `SerializerMessageConverter` using the same serializer with `application/json` support. Do not create a second listener annotation processor.

The configuration shape is:

```java
@Bean
RedisListenerConfigurer durexRedisListenerConfigurer(RedisMessagingCodec codec) {
  return new RedisListenerConfigurer() {
    @Override
    public void configureMessageConverters(RedisMessageConverters.Builder builder) {
      builder.addCustomConverter(
          new SerializerMessageConverter(codec.serializer(), MimeTypeUtils.APPLICATION_JSON));
    }
  };
}
```

If the exact builder method name differs in Spring Data Redis 4.1, use the current API equivalent discovered from its Javadoc; do not fall back to custom endpoint invocation.

- [ ] **Step 5: Implement `@Outgoing` as Spring AOP**

`RedisOutgoingAspect`:

```java
@Aspect
public class RedisOutgoingAspect {
  private final RedisConnectionFactory connectionFactory;
  private final RedisMessagingCodec codec;

  @Around("@annotation(outgoing)")
  public Object publishResult(ProceedingJoinPoint joinPoint, Outgoing outgoing) throws Throwable {
    Object result = joinPoint.proceed();
    if (result != null) {
      String topic = environment.resolvePlaceholders(outgoing.value());
      try (RedisConnection connection = connectionFactory.getConnection()) {
        connection.publish(RedisSerializer.string().serialize(topic), codec.encode(result));
      }
    }
    return result;
  }
}
```

Do not require Reactor/`Mono`; synchronous and ordinary Java return values are the baseline semantic. Reactive return values are deferred until there is a concrete use case and tested semantics.

- [ ] **Step 6: Auto-configure only when Redis is present**

Use:

```java
@AutoConfiguration
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnBean(RedisConnectionFactory.class)
```

Register codec/aspect/configurer with `@ConditionalOnMissingBean` where application override is reasonable.

- [ ] **Step 7: Verify GREEN against Redis 7 and commit**

With Redis on localhost:6379:

```bash
gradle -p migration/spring-messaging :messaging-spring-redis:test --tests '*RedisPubSubIntegrationTest' --stacktrace
```

Expected: Spring `@RedisListener` receives a typed POJO published by the Durex `@Outgoing` aspect; no generated source exists.

Commit:

```bash
git commit -m "feat: add Spring Redis pubsub messaging adapter"
```

---

### Task 4: Add explicit Redis Stream listener and forwarder semantics

**Files:**
- Create: `core/shared/messaging/spring-redis/src/main/java/com/github/durex/messaging/spring/redis/RedisStreamListenerRegistrar.java`
- Create: `core/shared/messaging/spring-redis/src/main/java/com/github/durex/messaging/spring/redis/RedisStreamOutgoingAspect.java`
- Modify: `core/shared/messaging/spring-redis/src/main/java/com/github/durex/messaging/spring/redis/DurexRedisMessagingAutoConfiguration.java`
- Create: `core/shared/messaging/spring-redis/src/test/java/com/github/durex/messaging/spring/redis/RedisStreamIntegrationTest.java`

**Interfaces:**
- `@RedisStreamListener` registers a consumer-group subscription with `StreamMessageListenerContainer`.
- Stream entries use one stable field named `payload`, containing UTF-8 JSON.
- `autoAck=false` means acknowledge only after the handler returns successfully; `autoAck=true` delegates acknowledgement to `receiveAutoAck`.

- [ ] **Step 1: Write the stream RED integration test**

Pre-create stream and group in test setup. Register a Spring bean:

```java
@Component
class StreamPipeline {
  final BlockingQueue<NormalizedEvent> received = new LinkedBlockingQueue<>();

  @RedisStreamListener(
      stream = "events.stream.raw",
      group = "normalizers",
      consumer = "test-consumer")
  @RedisStreamOutgoing("events.stream.normalized")
  NormalizedEvent normalize(RawEvent event) {
    var result = new NormalizedEvent(event.id(), event.value().trim());
    received.add(result);
    return result;
  }
}
```

Append a raw event to `events.stream.raw`, wait for handler invocation, then read `events.stream.normalized` and verify its `payload` decodes to the expected POJO. Before registrar/aspect implementation the handler is never invoked.

- [ ] **Step 2: Auto-configure `StreamMessageListenerContainer<String, MapRecord<String,String,String>>`**

Use `StreamMessageListenerContainerOptions.builder()` with string serializers and a bounded poll timeout (for example 1 second) and rely on the container's `SmartLifecycle` auto-start support.

- [ ] **Step 3: Discover annotated Spring beans at runtime**

`RedisStreamListenerRegistrar` implements `SmartInitializingSingleton` and scans Spring-managed beans for `@RedisStreamListener` methods.

For every annotated method:
- require exactly one payload parameter in Phase C;
- require a public invocable method;
- resolve placeholders in `stream`, `group`, and `consumer`;
- register `Consumer.from(group, consumer)` with `StreamOffset.create(stream, ReadOffset.lastConsumed())`;
- decode the record's `payload` field to the declared method parameter type;
- invoke the **Spring bean/proxy**, not a separately constructed target;
- if `autoAck=false`, acknowledge only after successful invocation.

Invalid signatures fail context startup with a clear `IllegalStateException` containing bean, method, and expected signature.

- [ ] **Step 4: Implement Stream outgoing forwarding separately**

`RedisStreamOutgoingAspect` proceeds with the method and, on a non-null result, appends:

```text
stream = annotation value
field  = payload
value  = codec.encodeString(result)
```

Use `StringRedisTemplate.opsForStream().add(...)`; Redis generates the record id. Do not create a custom sequence/id generator.

- [ ] **Step 5: Verify manual and auto acknowledgement semantics**

Integration assertions:
- successful `autoAck=false` processing removes the record from the group's pending list after Durex acknowledges it;
- a handler throwing an exception leaves the record pending;
- `autoAck=true` uses Spring Data Redis `receiveAutoAck` semantics.

- [ ] **Step 6: Verify processor + forwarder composition and commit**

Run:

```bash
gradle -p migration/spring-messaging :messaging-spring-redis:test --tests '*RedisStreamIntegrationTest' --stacktrace
```

Expected: receive -> convert -> invoke Spring bean -> forward result is exercised without generated source.

Commit:

```bash
git commit -m "feat: add Redis Stream messaging runtime"
```

---

### Task 5: Delete the generated Quarkus/Redisson messaging runtime

**Files:**
- Delete: `core/shared/messaging/messaging-api-redis/**`
- Delete: `core/shared/messaging/messaging-api-redis-example/**`
- Modify any active settings/build descriptors that reference those modules.
- Modify active messaging documentation to describe the Spring runtime API.

**Interfaces:**
- Removes `InComing`, legacy `OutGoing`, `Topic`, `QuarkusDaemon`, `RemoteService`, `RemoteServiceApi`, Redisson event wrappers, annotation processors, remote API generators, and all Velocity templates.

- [ ] **Step 1: Add a negative legacy boundary check before deletion**

Create `migration/spring-messaging/verify-messaging-boundary.sh` that fails if the active Spring messaging tree contains any of:

```text
javax.enterprise
javax.inject
javax.interceptor
io.quarkus
org.redisson
org.apache.velocity
RedisCodeGenerator
QuarkusDaemon
@Retention(SOURCE) on listener annotations
```

It should also fail if any `.vm` file exists below the new `core/shared/messaging/api` or `spring-redis` modules.

- [ ] **Step 2: Delete the legacy modules atomically**

Delete both legacy directories after Tasks 3–4 are green. Do not preserve aliases for `InComing` or the old CDI `OutGoing`; this repository is still in breaking design-stage migration.

- [ ] **Step 3: Confirm no active references remain**

Search active build/source files and docs. Historical superseded design docs may mention the old system when clearly historical; executable build descriptors and current docs may not.

- [ ] **Step 4: Verify the new graph without legacy code**

Run:

```bash
bash migration/spring-messaging/verify-messaging-boundary.sh
gradle -p migration/spring-messaging projects durexProjects --stacktrace
gradle -p migration/spring-messaging :messaging-api:test :messaging-spring-redis:test --configuration-cache --stacktrace
```

Expected: only the new messaging modules participate; no generated-source or CDI/Quarkus messaging dependency remains.

- [ ] **Step 5: Commit**

```bash
git commit -m "refactor: remove generated Quarkus messaging runtime"
```

---

### Task 6: Add a dedicated real-Redis CI gate and mark Phase C complete

**Files:**
- Create: `.github/workflows/spring-messaging.yml`
- Modify: `.github/workflows/durex-build-platform.yml`
- Modify: `docs/superpowers/specs/2026-08-21-durex-spring-boot-platform-v3-design.md`
- Modify: `docs/superpowers/specs/2026-08-21-durex-public-plugin-api-design.md`

**Interfaces:**
- Produces a repeatable Redis 7 integration gate for Pub/Sub and Streams.

- [ ] **Step 1: Add Redis service to GitHub Actions**

Create job:

```yaml
services:
  redis:
    image: redis:7-alpine
    ports:
      - 6379:6379
    options: >-
      --health-cmd "redis-cli ping"
      --health-interval 2s
      --health-timeout 2s
      --health-retries 15
```

Set up Java 25 and Gradle 9.1 as in the existing Spring workflows.

- [ ] **Step 2: Run capability, boundary, Pub/Sub, and Stream tests**

CI commands:

```bash
gradle -p build-logic/tests/messaging-feature-smoke verifyMessagingCapability --stacktrace
bash migration/spring-messaging/verify-messaging-boundary.sh
gradle -p migration/spring-messaging :messaging-api:test :messaging-spring-redis:test --configuration-cache --stacktrace
```

Run the final Gradle test command twice and assert the second run reports `Reusing configuration cache.` when the runtime tests are cache-compatible.

- [ ] **Step 3: Keep build-platform regression coverage aware of the new module**

Add a focused compile/test check for `messaging-api` and `messaging-spring-redis` to `durex-build-platform.yml`; do not add Redis integration there because the dedicated messaging workflow owns the Redis service.

- [ ] **Step 4: Update public API/runtime documentation**

Document:

```text
Pub/Sub receive    -> Spring @RedisListener
Pub/Sub forward    -> Durex @Outgoing
Stream receive     -> Durex @RedisStreamListener
Stream forward     -> Durex @RedisStreamOutgoing
```

Also add `durex.feature.messaging` to the public feature list and explicitly state that `messaging()` and `redis()` are independent capabilities composed by the Spring Redis adapter.

- [ ] **Step 5: Verify full PR gates**

Required successful workflows for the stacked Phase C PR:

```text
Durex Plugin API
Durex Build Platform
Spring Music Migration
Spring Messaging
Spring Native Reference
```

- [ ] **Step 6: Commit the Phase C checkpoint**

```bash
git commit -m "ci: verify Spring Redis messaging platform"
```

---

## Self-Review Notes

- **Spring reuse:** Pub/Sub receive is entirely Spring Data Redis `@RedisListener`; no duplicate Durex incoming annotation or listener processor is created.
- **Clear semantics:** Redis Pub/Sub and Streams have distinct APIs and acknowledgement tests; no `group.isEmpty()` transport inference survives.
- **No generated runtime:** annotation processing and Velocity source generation are removed rather than ported.
- **Thin Durex layer:** Durex adds return-value forwarding and declarative Stream registration only where Spring lacks direct annotation semantics.
- **Runtime registration:** Stream listeners use `StreamMessageListenerContainer`, which supports runtime subscriptions and owns polling/thread lifecycle.
- **Serialization:** one non-default-typing JSON codec is shared by forwarding and listener conversion; application default RedisTemplate configuration is not overwritten.
- **No hidden topology:** consumer groups are not automatically created; infrastructure remains explicit.
- **No premature RPC rewrite:** legacy Redis RemoteService generation is removed/deferred rather than rebuilt under Spring.
- **Real integration evidence:** Redis 7 CI verifies typed Pub/Sub receive, processing/forwarding, Stream group receive, forward, and acknowledgement behavior.
