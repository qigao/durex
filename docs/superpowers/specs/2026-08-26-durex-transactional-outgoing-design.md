# Durex Transactional Outgoing Design

## Goal

Define one precise Spring transaction boundary for Durex Redis outgoing annotations without introducing a broker transaction framework or outbox subsystem.

When an annotated method is invoked through the normal Spring AOP proxy:

- outside a Spring transaction, the successful non-null return value is published immediately after the method returns;
- inside an active Spring transaction with synchronization enabled, publication is deferred until `afterCommit`;
- rollback never publishes the deferred result.

The same rule applies to Redis Pub/Sub `@Outgoing` and Redis Streams `@RedisStreamOutgoing`.

## Problem

Today both outgoing aspects execute `joinPoint.proceed()` and immediately send the returned value. If the method is called inside a surrounding Spring transaction, that method return happens before the transaction manager commits. A later rollback can therefore leave an externally visible Redis message for database state that never committed.

## Design

### One internal synchronization helper

Add a package-private helper in `messaging-spring-redis`:

```java
final class TransactionalPublication {
  static void publishNowOrAfterCommit(Runnable publication) { ... }
}
```

The helper checks both:

- `TransactionSynchronizationManager.isActualTransactionActive()`; and
- `TransactionSynchronizationManager.isSynchronizationActive()`.

If both are true, it registers a `TransactionSynchronization` whose `afterCommit()` invokes the supplied publication. Otherwise it invokes the publication immediately.

The helper is implementation-only and is not added to `gradle/public-api/0.1-surface.txt`.

### Pub/Sub aspect

`RedisOutgoingAspect` still calls `joinPoint.proceed()` first. For a non-null result it resolves the destination and passes the existing `convertAndSend(destination, result)` operation to `TransactionalPublication`.

### Stream aspect

`RedisStreamOutgoingAspect` still calls `joinPoint.proceed()` first. For a non-null result it resolves the stream and serializes the result using the configured `RedisMessageCodec`, then defers only the Redis `XADD` operation through the helper.

Encoding occurs synchronously while the invocation/result context is still available; only the external side effect is delayed. Encoding failure therefore still fails the annotated invocation immediately rather than appearing after commit.

## Spring AOP boundary

Durex does not bypass normal Spring proxy semantics. An annotation on a self-invoked method is not intercepted merely because it is present; applications that require outgoing behavior must invoke the annotated method through its Spring proxy (or structure the call across beans). This is documented explicitly rather than hidden behind custom weaving.

## Dependency boundary

`messaging-spring-redis` takes an explicit implementation dependency on Spring transaction support because its runtime code directly references `TransactionSynchronizationManager` / `TransactionSynchronization`. This does not enable transaction management by itself and does not require applications to use transactions.

## Tests

The Redis integration suite uses a minimal Spring `PlatformTransactionManager`/`TransactionTemplate` to create real Spring synchronization lifecycle events while the existing Redis 7 service proves the external side effect.

For both Pub/Sub and Stream paths the tests establish:

1. non-transactional calls still publish;
2. an active transaction does not publish before commit;
3. successful commit publishes after commit;
4. rollback does not publish.

The tests use distinct destinations/streams for transactional cases so asynchronous delivery cannot be confused with messages from another test.

## Non-goals

- no exactly-once guarantee;
- no Redis transaction coupling to the database transaction;
- no retry scheduler or DLQ;
- no transactional outbox;
- no new public annotation attribute;
- no change to listener acknowledgement semantics.

An outbox may be added later for stronger durability requirements, but it is intentionally outside this issue.

Closes #191 when merged.