package com.github.durex.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

@Inherited
@InterceptorBinding
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface MyInterceptorAnnotation {

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
