# Durex Messaging

Durex messaging is Spring Boot-native.

- Use Spring Data Redis `@RedisListener` directly for ordinary Redis Pub/Sub receive handlers.
- Use Durex `@Outgoing` when a method's non-null return value should be forwarded to a Redis Pub/Sub destination.
- Use `@RedisStreamListener` for explicit Redis Stream consumer-group subscriptions.
- Use `@RedisStreamOutgoing` when a method's non-null return value should be appended to a Redis Stream.
- `durex.feature.messaging` provides generic Spring messaging support; `durex.feature.redis` remains the independent Redis transport capability.

The runtime adapter lives in `core/shared/messaging/spring-redis` and is discovered through Spring Boot auto-configuration. It does not use CDI, Quarkus, Redisson, annotation processors, or generated listener source.

Redis Stream consumer groups are infrastructure. Applications or deployment tooling must create them explicitly before stream listeners start.
