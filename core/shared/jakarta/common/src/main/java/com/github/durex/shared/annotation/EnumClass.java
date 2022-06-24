package com.github.durex.shared.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.github.durex.shared.validator.EnumClassValidator;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = EnumClassValidator.class)
public @interface EnumClass {
  Class<? extends Enum<?>> enumClass();

  String message() default "must be any of enum {enumClass}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
