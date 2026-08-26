package com.github.durex.messaging.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method whose successful return value is appended to a Redis Stream.
 *
 * <p>The Spring Redis runtime adapter encodes a non-null result after the annotated method returns
 * successfully. Outside a Spring transaction, the record is appended immediately. Inside an active
 * synchronized Spring transaction, the Redis append is deferred until successful commit and is
 * omitted on rollback. Normal Spring AOP proxy rules apply, including the usual self-invocation
 * limitation.
 */
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
