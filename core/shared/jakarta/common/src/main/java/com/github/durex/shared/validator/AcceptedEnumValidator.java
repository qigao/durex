package com.github.durex.shared.validator;

import com.github.durex.shared.annotation.AcceptedEnum;
import com.github.durex.shared.exceptions.model.ErrorCode;
import java.util.Arrays;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AcceptedEnumValidator implements ConstraintValidator<AcceptedEnum, ErrorCode> {

  private ErrorCode[] subset;

  @Override
  public void initialize(AcceptedEnum constraint) {
    this.subset = constraint.anyOf();
  }

  @Override
  public boolean isValid(ErrorCode value, ConstraintValidatorContext context) {
    return value == null || Arrays.asList(subset).contains(value);
  }
}
