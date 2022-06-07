package com.github.durex.shared.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

@InterceptorBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Documented
public @interface Cached {
  @Nonbinding
  String key() default "";

  @Nonbinding
  Class<?> clazz() default Object.class;
}
