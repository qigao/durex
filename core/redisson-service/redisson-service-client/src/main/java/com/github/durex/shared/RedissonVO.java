package com.github.durex.shared;

import reactor.core.publisher.Mono;

public interface RedissonVO<T, C> {
  String getKey();

  Class<T> getPayloadType();

  Mono<Void> save(T value);
}
