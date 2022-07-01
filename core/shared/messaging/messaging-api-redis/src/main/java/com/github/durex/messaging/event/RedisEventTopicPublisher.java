package com.github.durex.messaging.event;

import com.github.durex.messaging.api.model.CodecEnum;
import com.github.durex.messaging.redis.RedisCodec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import reactor.core.publisher.Mono;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisEventTopicPublisher<T> {
  private RTopicReactive topicReactive;

  public RedisEventTopicPublisher(RedissonClient client, String topic, CodecEnum codecEnum) {
    Codec redisCodec = RedisCodec.getCodec(codecEnum);
    topicReactive = client.reactive().getTopic(topic, redisCodec);
  }

  public Mono<Long> publish(T payload) {
    return topicReactive.publish(payload);
  }
}
