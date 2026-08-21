package com.github.durex.shared.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.github.durex.shared.exceptions.ApiException;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Inherited
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface ValueChecker {
  Class<?> type() default Object.class;

  String value() default "";

  String message() default "Unqualified Return Value";

  Class<? extends RuntimeException> exception() default ApiException.class;
}
