package com.github.durex.messaging.spring.redis;

/** Determines how Durex handles a Redis Stream entry after listener processing fails. */
public enum RedisStreamFailureDisposition {
  /** Leave the failed entry pending for later inspection or recovery. */
  KEEP_PENDING,

  /** Acknowledge the failed entry because the configured failure handler considers it handled. */
  ACKNOWLEDGE
}
