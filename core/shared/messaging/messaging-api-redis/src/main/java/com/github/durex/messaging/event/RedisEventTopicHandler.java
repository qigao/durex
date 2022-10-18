package com.github.durex.messaging.event;

import com.github.durex.messaging.api.enums.CodecEnum;
import com.github.durex.messaging.redis.RedisCodec;
import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.client.codec.Codec;
import reactor.core.publisher.Mono;

public class RedisEventTopicHandler<T> {
  private final RTopicReactive topicReactive;

  public RedisEventTopicHandler(RedissonClient client, String topic, CodecEnum codecEnum) {
    Codec redisCodec = RedisCodec.getCodec(codecEnum);
    topicReactive = client.reactive().getTopic(topic, redisCodec);
  }

  public Mono<Integer> listen(Class<T> tClass, MessageListener<T> messageListener) {
    return topicReactive.addListener(tClass, messageListener);
  }
}
