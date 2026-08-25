package com.github.durex.messaging.spring.redis;

import java.lang.reflect.Method;
import org.springframework.data.redis.connection.stream.MapRecord;

/**
 * Context supplied to a {@link RedisStreamListenerFailureHandler} after a stream listener fails.
 *
 * @param stream Redis stream key being consumed
 * @param group consumer group processing the entry
 * @param record original Redis Stream record
 * @param method listener method that failed
 * @param cause exception raised while decoding or invoking the listener
 */
public record RedisStreamListenerFailure(
    String stream,
    String group,
    MapRecord<String, String, String> record,
    Method method,
    Throwable cause) {}
