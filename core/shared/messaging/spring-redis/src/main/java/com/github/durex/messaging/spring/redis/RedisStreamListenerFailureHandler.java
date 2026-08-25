package com.github.durex.messaging.spring.redis;

/** Strategy that decides whether a failed Redis Stream entry remains pending or is acknowledged. */
@FunctionalInterface
public interface RedisStreamListenerFailureHandler {
  /**
   * Handles a listener failure and chooses the entry's acknowledgement disposition.
   *
   * @param failure stream, record, listener, and exception context
   * @return disposition to apply after the handler returns
   */
  RedisStreamFailureDisposition onFailure(RedisStreamListenerFailure failure);

  /**
   * Returns the default failure policy, which leaves failed entries pending.
   *
   * @return handler that always returns {@link RedisStreamFailureDisposition#KEEP_PENDING}
   */
  static RedisStreamListenerFailureHandler keepPending() {
    return failure -> RedisStreamFailureDisposition.KEEP_PENDING;
  }
}
