package com.github.durex.messaging.test.service;

import static com.github.durex.messaging.api.enums.CodecEnum.MSGPACK;

import com.github.durex.messaging.redis.RedisCodec;
import com.github.durex.messaging.test.models.DemoEvent;
import com.github.durex.messaging.test.models.TestEvent;
import com.github.durex.messaging.test.topics.TopicNameConstants;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import reactor.test.StepVerifier;

@QuarkusTest
class ExampleServiceTest {
  @Inject RedissonClient redisson;
  @Inject ExampleService service;

  @Test
  void testExampleTopic() {
    var event = DemoEvent.builder().name(Faker.instance().name().name()).age(40).build();
    redisson
        .reactive()
        .getTopic(TopicNameConstants.DEMO_TOPIC, RedisCodec.getCodec(MSGPACK))
        .publish(event)
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testSendTestEvent() {
    var event = TestEvent.builder().age(35).name(Faker.instance().name().name()).build();
    service.sendTestEvent(event).as(StepVerifier::create).expectNext(event).verifyComplete();
  }
}
