package com.github.durex.messaging.spring.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.durex.messaging.annotation.RedisStreamListener;
import com.github.durex.messaging.annotation.RedisStreamOutgoing;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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

@SpringBootTest(
    classes = RedisStreamIntegrationTest.TestApplication.class,
    properties = {
      "spring.data.redis.host=127.0.0.1",
      "spring.data.redis.port=6379"
    })
class RedisStreamIntegrationTest {

  @Autowired StreamPipeline pipeline;

  @Test
  void streamOutgoingIsDeliveredThroughDeclaredConsumerGroup() throws Exception {
    var expected = pipeline.normalize(new RawEvent("event-2", "  stream value  "));

    var received = pipeline.awaitReceived(5, TimeUnit.SECONDS);
    assertNotNull(received);
    assertEquals(expected, received);
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import(StreamPipeline.class)
  static class TestApplication {
    @Bean
    SmartInitializingSingleton streamInfrastructure(StringRedisTemplate redisTemplate) {
      return () ->
          redisTemplate
              .opsForStream()
              .createGroup("events.stream", ReadOffset.from("0-0"), "workers");
    }
  }

  public static class StreamPipeline {
    private final BlockingQueue<NormalizedEvent> received = new LinkedBlockingQueue<>();

    @RedisStreamListener(
        stream = "events.stream",
        group = "workers",
        consumer = "worker-1",
        autoAck = false)
    public void receive(NormalizedEvent event) {
      received.add(event);
    }

    @RedisStreamOutgoing("events.stream")
    public NormalizedEvent normalize(RawEvent event) {
      return new NormalizedEvent(event.id(), event.value().trim());
    }

    public NormalizedEvent awaitReceived(long timeout, TimeUnit unit) throws InterruptedException {
      return received.poll(timeout, unit);
    }
  }

  record RawEvent(String id, String value) {}

  record NormalizedEvent(String id, String value) {}
}
