package com.github.durex.messaging.spring.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.durex.messaging.annotation.Outgoing;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.annotation.RedisListener;

@SpringBootTest(
    classes = RedisPubSubIntegrationTest.TestApplication.class,
    properties = {
      "spring.data.redis.host=127.0.0.1",
      "spring.data.redis.port=6379"
    })
class RedisPubSubIntegrationTest {

  @Autowired PubSubPipeline pipeline;

  @Test
  void outgoingPublishesReturnValueToSpringRedisListener() throws Exception {
    var expected = pipeline.normalize(new RawEvent("event-1", "  hello spring redis  "));

    var received = pipeline.awaitReceived(5, TimeUnit.SECONDS);
    assertNotNull(received);
    assertEquals(expected, received);
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import(PubSubPipeline.class)
  static class TestApplication {}

  public static class PubSubPipeline {
    private final BlockingQueue<NormalizedEvent> received = new LinkedBlockingQueue<>();

    @RedisListener(topic = "events.normalized", consumes = "application/json")
    public void receive(NormalizedEvent event) {
      received.add(event);
    }

    @Outgoing("events.normalized")
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
