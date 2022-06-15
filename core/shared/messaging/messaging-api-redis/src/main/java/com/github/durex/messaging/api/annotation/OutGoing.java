package com.github.durex.messaging.api.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.github.durex.messaging.api.model.EventTopic;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

@Inherited
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@InterceptorBinding
public @interface OutGoing {
  @Nonbinding
  String value() default "";

  @Nonbinding
  Class<? extends EventTopic> topic() default EventTopic.class;
}
