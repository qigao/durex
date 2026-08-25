# Durex Messaging

Durex messaging is Spring Boot-native.

- Use Spring Data Redis `@RedisListener` directly for ordinary Redis Pub/Sub receive handlers.
- Use Durex `@Outgoing` when a method's non-null return value should be forwarded to a Redis Pub/Sub destination.
- Use `@RedisStreamListener` for explicit Redis Stream consumer-group subscriptions.
- Use `@RedisStreamOutgoing` when a method's non-null return value should be appended to a Redis Stream.
- The build enables the independent SimpleDSL `messaging`, `redis`, and `aop` capabilities through `simpledsl { ... }`.

The runtime adapter lives in `core/shared/messaging/spring-redis` and is discovered through Spring Boot auto-configuration. It does not use CDI, Quarkus, Redisson, annotation processors, or generated listener source.

## Redis Stream payloads

Redis Stream outgoing and listener paths share a `RedisMessageCodec`. The default `JacksonRedisMessageCodec` decodes against the listener method's declared generic `Type`, so parameterized payloads such as `List<Event>` retain their element type. Applications can provide their own `RedisMessageCodec` bean to replace the default codec.

## Redis Stream listener failures

For manual-ack listeners (`autoAck = false`), the default `RedisStreamListenerFailureHandler` returns `KEEP_PENDING`: the failed record is left pending and the listener failure is propagated. An application can provide its own handler and return `ACKNOWLEDGE` after handling the failure, for example after publishing the failed record to an application-owned dead-letter stream. Durex does not schedule retries or create a DLQ automatically.

Redis Stream consumer groups are infrastructure. Applications or deployment tooling must create them explicitly before stream listeners start.
