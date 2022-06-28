package com.github.durex.messaging.test.service;

import static com.github.durex.messaging.api.model.CodecEnum.MSGPACK;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.messaging.redis.RedisCodec;
import com.github.durex.messaging.test.models.DemoEvent;
import com.github.durex.messaging.test.models.TestEvent;
import com.github.durex.messaging.test.topics.TopicNameConstants;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Map;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamReadGroupArgs;
import reactor.test.StepVerifier;

@QuarkusTest
class ExampleServiceTest {
  @Inject RedissonClient redisson;
  @Inject ExampleService service;

  @Test
  void testExampleTopic() {
    var event = DemoEvent.builder().name("John").age("40").build();
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
    var event = TestEvent.builder().name("Duck").age("35").build();
    service.sendTestEvent(event).as(StepVerifier::create).expectNext(event).verifyComplete();
  }

  @Test
  void testRemoveConsumer() {
    RStream<String, String> stream = redisson.getStream("test");

    StreamMessageId sm = stream.add(StreamAddArgs.entry("0", "0"));

    stream.createGroup("testGroup");

    StreamMessageId id1 = stream.add(StreamAddArgs.entry("1", "1"));
    StreamMessageId id2 = stream.add(StreamAddArgs.entry("2", "2"));

    Map<StreamMessageId, Map<String, String>> group =
        stream.readGroup("testGroup", "consumer1", StreamReadGroupArgs.neverDelivered());
    long amount = stream.ack("testGroup", id1, id2);
    assertEquals(2, group.size());
  }
}
