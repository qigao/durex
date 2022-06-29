package com.github.durex.messaging.test.service;

import static com.github.durex.messaging.api.model.CodecEnum.JSON;

import com.github.durex.messaging.event.RedisStreamTopicPublisher;
import com.github.durex.messaging.test.models.TestEvent;
import com.github.durex.messaging.test.topics.TopicNameConstants;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import reactor.core.publisher.Mono;

@Slf4j
@Singleton
public class StreamService {
  public static final String TEST_TOPIC = TopicNameConstants.TEST_TOPIC;
  @Inject RedissonClient redisson;

  private RedisStreamTopicPublisher  publisher;

  public void init() {
    publisher = new RedisStreamTopicPublisher(redisson, TEST_TOPIC, JSON);
  }

  public Mono<StreamMessageId> pubMessage(TestEvent testEvent) {
    return publisher.publish("1", testEvent);
  }
}
