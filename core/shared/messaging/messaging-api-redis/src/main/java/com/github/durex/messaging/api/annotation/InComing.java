package com.github.durex.messaging.api.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import com.github.durex.messaging.api.model.EventTopic;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Inherited
@Target({METHOD})
@Retention(SOURCE)
public @interface InComing {
  String value() default "";

  Class<? extends EventTopic> topic() default EventTopic.class;
}
