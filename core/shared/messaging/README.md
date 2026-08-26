# Durex Messaging

Durex messaging is Spring Boot-native.

- Use Spring Data Redis `@RedisListener` directly for ordinary Redis Pub/Sub receive handlers.
- Use Durex `@Outgoing` when a method's non-null return value should be forwarded to a Redis Pub/Sub destination.
- Use `@RedisStreamListener` for explicit Redis Stream consumer-group subscriptions.
- Use `@RedisStreamOutgoing` when a method's non-null return value should be appended to a Redis Stream.
- The build enables the independent SimpleDSL `messaging`, `redis`, and `aop` capabilities through `simpledsl { ... }`.

The runtime adapter lives in `core/shared/messaging/spring-redis` and is discovered through Spring Boot auto-configuration. It does not use CDI, Quarkus, Redisson, annotation processors, or generated listener source.

## Outgoing transaction semantics

`@Outgoing` and `@RedisStreamOutgoing` publish only after the annotated method returns successfully. Outside a Spring transaction, the non-null return value is published immediately after that return.

When the intercepted call runs inside an active Spring transaction with synchronization enabled, Durex defers the external Redis side effect until Spring reports a successful commit. A rolled-back transaction therefore does not publish the deferred Pub/Sub message or Stream record. Stream payload encoding still happens synchronously before commit so codec failures remain part of the annotated invocation rather than surfacing later from an `afterCommit` callback.

This is transaction-aware publication, not an exactly-once or transactional-outbox guarantee: a failure after the database commit while sending to Redis can still leave committed state without a message. Applications needing durable cross-system delivery should use an outbox or another stronger delivery mechanism.

Durex follows normal Spring AOP proxy rules. Self-invocation of an annotated method does not pass through the Spring proxy and therefore does not trigger the outgoing aspect. Invoke the annotated method through its Spring-managed proxy (for example, across bean boundaries) when outgoing behavior is required.

## Redis Stream payloads

Redis Stream outgoing and listener paths share a `RedisMessageCodec`. The default `JacksonRedisMessageCodec` decodes against the listener method's declared generic `Type`, so parameterized payloads such as `List<Event>` retain their element type. Applications can provide their own `RedisMessageCodec` bean to replace the default codec.

## Redis Stream listener failures

For manual-ack listeners (`autoAck = false`), the default `RedisStreamListenerFailureHandler` returns `KEEP_PENDING`: the failed record is left pending and the listener failure is propagated. An application can provide its own handler and return `ACKNOWLEDGE` after handling the failure, for example after publishing the failed record to an application-owned dead-letter stream. Durex does not schedule retries or create a DLQ automatically.

Redis Stream consumer groups are infrastructure. Applications or deployment tooling must create them explicitly before stream listeners start.
