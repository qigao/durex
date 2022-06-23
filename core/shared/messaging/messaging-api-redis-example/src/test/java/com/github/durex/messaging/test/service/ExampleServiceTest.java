package com.github.durex.messaging.test.service;

import static com.github.durex.messaging.api.model.CodecEnum.MSGPACK;

import com.github.durex.messaging.redis.RedisCodec;
import com.github.durex.messaging.test.models.DemoEvent;
import com.github.durex.messaging.test.models.TestEvent;
import com.github.durex.messaging.test.topics.TopicNameConstants;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import reactor.test.StepVerifier;

@QuarkusTest
class ExampleServiceTest {
  @Inject RedissonClient redissonClient;
  @Inject ExampleService service;

  @Test
  void testExampleTopic() {
    var event = DemoEvent.builder().name("John").age("40").build();
    redissonClient
        .reactive()
        .getTopic(TopicNameConstants.DEMO_TOPIC, RedisCodec.getCodec(MSGPACK))
        .publish(event)
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testSendTestEvent() {
    var event = TestEvent.builder().name("Duck").age("35").build();
    service.sendTestEvent(event).as(StepVerifier::create).expectNext(event).verifyComplete();
  }
}
