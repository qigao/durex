package com.github.durex.messaging.event;

import com.github.durex.messaging.api.model.CodecEnum;
import com.github.durex.messaging.api.model.EventTopic;
import com.github.durex.messaging.redis.RedisCodec;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.redisson.api.RedissonClient;
import reactor.core.publisher.Mono;

@Setter
@Getter
public class RedisEventTopicPublisher<T> implements EventTopic<T> {
  @Inject RedissonClient redissonClient;
  private String topicName;
  private CodecEnum payloadCodec;

  public Mono<Long> publish(T payload) {
    return redissonClient
        .reactive()
        .getTopic(topicName, RedisCodec.getCodec(payloadCodec))
        .publish(payload);
  }
}
