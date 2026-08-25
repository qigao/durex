package com.github.durex.messaging.spring.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RedisStreamListenerFailureHandlerTest {

  @Test
  void defaultHandlerKeepsFailedMessagePending() {
    var handler = RedisStreamListenerFailureHandler.keepPending();

    assertEquals(RedisStreamFailureDisposition.KEEP_PENDING, handler.onFailure(null));
  }

  @Test
  void customHandlerCanAcknowledgeFailedMessage() {
    RedisStreamListenerFailureHandler handler =
        failure -> RedisStreamFailureDisposition.ACKNOWLEDGE;

    assertEquals(RedisStreamFailureDisposition.ACKNOWLEDGE, handler.onFailure(null));
  }
}
