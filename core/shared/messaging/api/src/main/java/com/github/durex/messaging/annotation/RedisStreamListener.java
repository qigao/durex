package com.github.durex.messaging.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Registers a method as a Redis Streams consumer-group listener.
 *
 * <p>The Spring Redis runtime adapter decodes each stream payload to the listener method's declared
 * parameter type and applies the configured acknowledgement semantics.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface RedisStreamListener {
  /**
   * Redis stream key to consume.
   *
   * @return stream key
   */
  String stream();

  /**
   * Consumer group used for stream delivery.
   *
   * @return consumer-group name
   */
  String group();

  /**
   * Consumer name within the configured group.
   *
   * @return consumer name
   */
  String consumer();

  /**
   * Whether the listener container should automatically acknowledge delivered messages.
   *
   * @return {@code true} for automatic acknowledgement; {@code false} for Durex-managed handling
   */
  boolean autoAck() default false;
}
