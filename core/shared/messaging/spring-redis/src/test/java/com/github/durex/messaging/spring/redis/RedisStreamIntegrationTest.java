package com.github.durex.messaging.spring.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.durex.messaging.annotation.RedisStreamListener;
import com.github.durex.messaging.annotation.RedisStreamOutgoing;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(
    classes = RedisStreamIntegrationTest.TestApplication.class,
    properties = {
      "spring.data.redis.host=127.0.0.1",
      "spring.data.redis.port=6379"
    })
class RedisStreamIntegrationTest {
  private static final String COMMIT_STREAM = "events.transaction.commit.stream";
  private static final String ROLLBACK_STREAM = "events.transaction.rollback.stream";

  @Autowired StreamPipeline pipeline;
  @Autowired StringRedisTemplate redisTemplate;
  @Autowired TransactionTemplate transactionTemplate;

  @Test
  void streamOutgoingIsDeliveredThroughDeclaredConsumerGroup() throws Exception {
    var expected = pipeline.normalize(new RawEvent("event-2", "  stream value  "));

    var received = pipeline.awaitReceived(5, TimeUnit.SECONDS);
    assertNotNull(received);
    assertEquals(expected, received);
  }

  @Test
  void transactionalStreamOutgoingPublishesOnlyAfterCommit() throws Exception {
    var expected = new AtomicReference<NormalizedEvent>();

    transactionTemplate.executeWithoutResult(
        status -> {
          expected.set(
              pipeline.normalizeCommitted(new RawEvent("stream-commit", "  committed stream  ")));
          assertEquals(0L, redisTemplate.opsForStream().size(COMMIT_STREAM));
        });

    var received = pipeline.awaitCommitted(5, TimeUnit.SECONDS);
    assertNotNull(received);
    assertEquals(expected.get(), received);
    assertEquals(1L, redisTemplate.opsForStream().size(COMMIT_STREAM));
  }

  @Test
  void transactionalStreamOutgoingDoesNotPublishAfterRollback() throws Exception {
    transactionTemplate.executeWithoutResult(
        status -> {
          pipeline.normalizeRolledBack(new RawEvent("stream-rollback", "  rolled back stream  "));
          status.setRollbackOnly();
        });

    assertEquals(0L, redisTemplate.opsForStream().size(ROLLBACK_STREAM));
    assertNull(pipeline.awaitRolledBack(2, TimeUnit.SECONDS));
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import(StreamPipeline.class)
  static class TestApplication {
    @Bean
    SmartInitializingSingleton streamInfrastructure(StringRedisTemplate redisTemplate) {
      return () -> {
        redisTemplate
            .opsForStream()
            .createGroup("events.stream", ReadOffset.from("0-0"), "workers");
        redisTemplate
            .opsForStream()
            .createGroup(COMMIT_STREAM, ReadOffset.from("0-0"), "commit-workers");
        redisTemplate
            .opsForStream()
            .createGroup(ROLLBACK_STREAM, ReadOffset.from("0-0"), "rollback-workers");
      };
    }

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

  public static class StreamPipeline {
    private final BlockingQueue<NormalizedEvent> received = new LinkedBlockingQueue<>();
    private final BlockingQueue<NormalizedEvent> committed = new LinkedBlockingQueue<>();
    private final BlockingQueue<NormalizedEvent> rolledBack = new LinkedBlockingQueue<>();

    @RedisStreamListener(
        stream = "events.stream",
        group = "workers",
        consumer = "worker-1",
        autoAck = false)
    public void receive(NormalizedEvent event) {
      received.add(event);
    }

    @RedisStreamListener(
        stream = COMMIT_STREAM,
        group = "commit-workers",
        consumer = "commit-worker-1",
        autoAck = false)
    public void receiveCommitted(NormalizedEvent event) {
      committed.add(event);
    }

    @RedisStreamListener(
        stream = ROLLBACK_STREAM,
        group = "rollback-workers",
        consumer = "rollback-worker-1",
        autoAck = false)
    public void receiveRolledBack(NormalizedEvent event) {
      rolledBack.add(event);
    }

    @RedisStreamOutgoing("events.stream")
    public NormalizedEvent normalize(RawEvent event) {
      return normalized(event);
    }

    @RedisStreamOutgoing(COMMIT_STREAM)
    public NormalizedEvent normalizeCommitted(RawEvent event) {
      return normalized(event);
    }

    @RedisStreamOutgoing(ROLLBACK_STREAM)
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
