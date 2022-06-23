package com.github.durex.messaging.event;

import com.github.durex.messaging.api.model.CodecEnum;
import com.github.durex.messaging.api.model.EventTopic;
import com.github.durex.messaging.redis.RedisCodec;
import lombok.Getter;
import lombok.Setter;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import reactor.core.publisher.Mono;

@Setter
@Getter
public class RedisEventTopicHandler<T> implements EventTopic<T> {
  private String topicName;
  private CodecEnum payloadCodec;

  @Override
  public Mono<Long> publish(T payload) {
    return null;
  }

  public Mono<Integer> listen(
      Class<T> tClass, MessageListener<T> messageListener, RedissonClient redissonClient) {
    return redissonClient
        .reactive()
        .getTopic(topicName, RedisCodec.getCodec(payloadCodec))
        .addListener(tClass, messageListener);
  }
}
