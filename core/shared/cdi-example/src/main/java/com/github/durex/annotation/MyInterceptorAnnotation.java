package com.github.durex.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

@InterceptorBinding
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
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
  @Nonbinding
  Class<? extends BaseData>  types() default BaseData.class;
}
