package com.github.durex.messaging.api.model;

import com.github.durex.messaging.api.enums.CodecEnum;
import reactor.core.publisher.Mono;

public interface EventTopic<T> {
  String getTopicName();

  CodecEnum getPayloadCodec();

  Mono<Long> publish(T payload);
}
