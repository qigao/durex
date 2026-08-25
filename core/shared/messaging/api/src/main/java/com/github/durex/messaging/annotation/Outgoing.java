package com.github.durex.messaging.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method whose successful return value is published to a Redis Pub/Sub channel.
 *
 * <p>The Spring Redis runtime adapter performs the publication after the annotated method returns.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Outgoing {
  /**
   * Redis Pub/Sub channel to which the method result is published.
   *
   * @return channel name
   */
  String value();
}
