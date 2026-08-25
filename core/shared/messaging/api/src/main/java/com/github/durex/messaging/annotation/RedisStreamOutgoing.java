package com.github.durex.messaging.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Marks a method whose successful return value is appended to a Redis Stream. */
@Retention(RUNTIME)
@Target(METHOD)
public @interface RedisStreamOutgoing {
  /**
   * Redis stream key to which the method result is appended.
   *
   * @return stream key
   */
  String value();
}
