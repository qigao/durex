package com.github.durex.messaging.event;

import com.github.durex.messaging.api.model.CodecEnum;
import com.github.durex.messaging.redis.RedisCodec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RStreamReactive;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.client.codec.Codec;
import reactor.core.publisher.Mono;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisStreamTopicPublisher {
  private Codec redisCodec;
  private RedissonClient client;
  private String topic;

  public RedisStreamTopicPublisher(RedissonClient client, String topic, CodecEnum codecEnum) {
    this.redisCodec = RedisCodec.getCodec(codecEnum);
    this.client = client;
    this.topic = topic;
  }

  public <T> Mono<StreamMessageId> publish(String key, T event) {
    final RStreamReactive<String, T> stream = client.reactive().getStream(topic, redisCodec);
    final StreamAddArgs<String, T> streamArgs = StreamAddArgs.entry(key, event);
    return stream.add(streamArgs);
  }
}
