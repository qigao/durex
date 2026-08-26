package com.github.durex.messaging.spring.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.durex.messaging.annotation.Outgoing;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.annotation.RedisListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(
    classes = RedisPubSubIntegrationTest.TestApplication.class,
    properties = {
      "spring.data.redis.host=127.0.0.1",
      "spring.data.redis.port=6379"
    })
class RedisPubSubIntegrationTest {

  @Autowired PubSubPipeline pipeline;
  @Autowired TransactionTemplate transactionTemplate;

  @Test
  void outgoingPublishesReturnValueToSpringRedisListener() throws Exception {
    var expected = pipeline.normalize(new RawEvent("event-1", "  hello spring redis  "));

    var received = pipeline.awaitReceived(5, TimeUnit.SECONDS);
    assertNotNull(received);
    assertEquals(expected, received);
  }

  @Test
  void transactionalOutgoingPublishesOnlyAfterCommit() throws Exception {
    var expected = new AtomicReference<NormalizedEvent>();

    transactionTemplate.executeWithoutResult(
        status -> {
          expected.set(pipeline.normalizeCommitted(new RawEvent("event-commit", "  committed  ")));
          try {
            assertNull(pipeline.awaitCommitted(500, TimeUnit.MILLISECONDS));
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
          }
        });

    var received = pipeline.awaitCommitted(5, TimeUnit.SECONDS);
    assertNotNull(received);
    assertEquals(expected.get(), received);
  }

  @Test
  void transactionalOutgoingDoesNotPublishAfterRollback() throws Exception {
    transactionTemplate.executeWithoutResult(
        status -> {
          pipeline.normalizeRolledBack(new RawEvent("event-rollback", "  rolled back  "));
          status.setRollbackOnly();
        });

    assertNull(pipeline.awaitRolledBack(2, TimeUnit.SECONDS));
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import(PubSubPipeline.class)
  static class TestApplication {
    @Bean
    PlatformTransactionManager transactionManager() {
      return new TestTransactionManager();
    }

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
      return new TransactionTemplate(transactionManager);
    }
  }

  static final class TestTransactionManager extends AbstractPlatformTransactionManager {
    @Override
    protected Object doGetTransaction() {
      return new Object();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {}

    @Override
    protected void doCommit(DefaultTransactionStatus status) {}

    @Override
    protected void doRollback(DefaultTransactionStatus status) {}
  }

  public static class PubSubPipeline {
    private final BlockingQueue<NormalizedEvent> received = new LinkedBlockingQueue<>();
    private final BlockingQueue<NormalizedEvent> committed = new LinkedBlockingQueue<>();
    private final BlockingQueue<NormalizedEvent> rolledBack = new LinkedBlockingQueue<>();

    @RedisListener(topic = "events.normalized", consumes = "application/json")
    public void receive(NormalizedEvent event) {
      received.add(event);
    }

    @RedisListener(topic = "events.transaction.commit", consumes = "application/json")
    public void receiveCommitted(NormalizedEvent event) {
      committed.add(event);
    }

    @RedisListener(topic = "events.transaction.rollback", consumes = "application/json")
    public void receiveRolledBack(NormalizedEvent event) {
      rolledBack.add(event);
    }

    @Outgoing("events.normalized")
    public NormalizedEvent normalize(RawEvent event) {
      return normalized(event);
    }

    @Outgoing("events.transaction.commit")
    public NormalizedEvent normalizeCommitted(RawEvent event) {
      return normalized(event);
    }

    @Outgoing("events.transaction.rollback")
    public NormalizedEvent normalizeRolledBack(RawEvent event) {
      return normalized(event);
    }

    public NormalizedEvent awaitReceived(long timeout, TimeUnit unit) throws InterruptedException {
      return received.poll(timeout, unit);
    }

    public NormalizedEvent awaitCommitted(long timeout, TimeUnit unit) throws InterruptedException {
      return committed.poll(timeout, unit);
    }

    public NormalizedEvent awaitRolledBack(long timeout, TimeUnit unit) throws InterruptedException {
      return rolledBack.poll(timeout, unit);
    }

    private static NormalizedEvent normalized(RawEvent event) {
      return new NormalizedEvent(event.id(), event.value().trim());
    }
  }

  record RawEvent(String id, String value) {}

  record NormalizedEvent(String id, String value) {}
}
