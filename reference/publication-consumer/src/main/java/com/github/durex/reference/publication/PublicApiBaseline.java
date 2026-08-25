package com.github.durex.reference.publication;

import com.github.durex.messaging.annotation.Outgoing;
import com.github.durex.messaging.annotation.RedisStreamListener;
import com.github.durex.messaging.annotation.RedisStreamOutgoing;
import com.github.durex.messaging.spring.redis.RedisMessageCodec;
import com.github.durex.messaging.spring.redis.RedisStreamFailureDisposition;
import com.github.durex.messaging.spring.redis.RedisStreamListenerFailureHandler;
import com.github.durex.shared.model.RespData;
import com.github.durex.shared.spring.http.DurexHttpExceptionHandler;

/**
 * Compile-only compatibility fixture for the public Durex API promised by the initial Maven surface.
 * It intentionally depends only on the staged Maven artifacts, never on Durex project sources.
 */
public final class PublicApiBaseline {
  private final DurexHttpExceptionHandler exceptionHandler = new DurexHttpExceptionHandler();

  public RespData<String> response(String value) {
    return RespData.of(value, null);
  }

  public DurexHttpExceptionHandler exceptionHandler() {
    return exceptionHandler;
  }

  public RedisMessageCodec codec(RedisMessageCodec codec) {
    return codec;
  }

  public RedisStreamListenerFailureHandler failureHandler() {
    return RedisStreamListenerFailureHandler.keepPending();
  }

  public RedisStreamFailureDisposition keepPending() {
    return RedisStreamFailureDisposition.KEEP_PENDING;
  }

  @Outgoing("public.api.pubsub")
  public String outgoing(String value) {
    return value;
  }

  @RedisStreamOutgoing("public.api.stream")
  public String streamOutgoing(String value) {
    return value;
  }

  @RedisStreamListener(
      stream = "public.api.stream",
      group = "public-api",
      consumer = "baseline",
      autoAck = false)
  public void streamListener(String value) {}
}
