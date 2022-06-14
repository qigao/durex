package com.github.durex.redisson.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@InterceptorBinding
public @interface Cached {
  @Nonbinding
  Class<?> clazz() default Object.class;

  @Nonbinding
  String topic() default "";

  @Nonbinding
  String message() default "";

  @Nonbinding
  String subscription() default "";

  @Nonbinding
  String key() default "";

  @Nonbinding
  String value() default "";
}
