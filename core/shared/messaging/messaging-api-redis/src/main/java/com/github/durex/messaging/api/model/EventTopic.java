package com.github.durex.messaging.api.model;

import reactor.core.publisher.Mono;

public interface EventTopic<T> {
  String getTopicName();

  CodecEnum getPayloadCodec();

  Mono<Long> publish(T payload);
}
