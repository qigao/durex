package com.github.durex.shared;

import reactor.core.publisher.Mono;

public interface RedissonValueObject<T,C> {
  String getKey();
  Class<T> getPayloadType();
  Mono<Void> save(T value);
}
