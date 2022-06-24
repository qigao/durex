package com.github.durex.shared.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.github.durex.shared.exceptions.ApiException;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

@Inherited
@InterceptorBinding
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface ValueChecker {
  @Nonbinding
  Class<?> type() default Object.class;

  @Nonbinding
  String value() default "";

  @Nonbinding
  String message() default "Unqualified Return Value";

  @Nonbinding
  Class<? extends RuntimeException> exception() default ApiException.class;
}
