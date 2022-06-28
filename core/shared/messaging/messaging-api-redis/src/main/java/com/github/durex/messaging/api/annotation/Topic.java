package com.github.durex.messaging.api.annotation;

import static com.github.durex.messaging.api.model.CodecEnum.JSON;
import static com.github.durex.messaging.api.model.CodecEnum.MSGPACK;
import static java.lang.annotation.ElementType.TYPE;

import com.github.durex.messaging.api.model.CodecEnum;
import com.github.durex.messaging.api.validator.CodecEnumCheck;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Inherited
@Target({TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Topic {
  String value() default "";

  @CodecEnumCheck(anyOf = {JSON, MSGPACK})
  CodecEnum codec() default JSON;

  String group() default "";

  String subscriber() default "";
}
