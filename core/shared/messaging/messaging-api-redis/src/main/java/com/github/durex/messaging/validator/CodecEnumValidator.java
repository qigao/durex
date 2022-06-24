package com.github.durex.messaging.validator;

import com.github.durex.messaging.api.model.CodecEnum;
import com.github.durex.messaging.api.validator.CodecEnumCheck;
import java.util.Arrays;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CodecEnumValidator implements ConstraintValidator<CodecEnumCheck, CodecEnum> {

  private CodecEnum[] subset;

  @Override
  public void initialize(CodecEnumCheck constraint) {
    this.subset = constraint.anyOf();
  }

  @Override
  public boolean isValid(CodecEnum value, ConstraintValidatorContext context) {
    return value == null || Arrays.asList(subset).contains(value);
  }
}
