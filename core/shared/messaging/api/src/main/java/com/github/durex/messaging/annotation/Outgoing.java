package com.github.durex.messaging.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method whose successful return value is published to a Redis Pub/Sub channel.
 *
 * <p>The Spring Redis runtime adapter publishes a non-null result after the annotated method
 * returns successfully. Outside a Spring transaction, publication is immediate. Inside an active
 * synchronized Spring transaction, publication is deferred until successful commit and is omitted
 * on rollback. Normal Spring AOP proxy rules apply, including the usual self-invocation limitation.
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
