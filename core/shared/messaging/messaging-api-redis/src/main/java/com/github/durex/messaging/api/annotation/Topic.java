package com.github.durex.messaging.api.annotation;

import static java.lang.annotation.ElementType.TYPE;

import com.github.durex.messaging.api.model.CodecEnum;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Inherited
@Target({TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Topic {
  String value() default "";

  CodecEnum codec() default CodecEnum.JSON;

  String subscriber() default "";
}
