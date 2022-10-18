package com.github.durex.messaging.api.annotation;

import static com.github.durex.messaging.api.enums.RemoteServiceEnum.SYNC;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import com.github.durex.messaging.api.enums.RemoteServiceEnum;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Inherited
@Target({METHOD})
@Retention(SOURCE)
public @interface RemoteServiceApi {
  String value() default "";

  RemoteServiceEnum serviceType() default SYNC;
}
