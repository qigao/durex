package com.github.durex.messaging.spring.redis;

@FunctionalInterface
public interface RedisStreamListenerFailureHandler {
  RedisStreamFailureDisposition onFailure(RedisStreamListenerFailure failure);

  static RedisStreamListenerFailureHandler keepPending() {
    return failure -> RedisStreamFailureDisposition.KEEP_PENDING;
  }
}
