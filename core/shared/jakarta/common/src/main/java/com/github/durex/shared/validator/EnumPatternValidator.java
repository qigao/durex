package com.github.durex.shared.validator;

import com.github.durex.shared.annotation.EnumPattern;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EnumPatternValidator implements ConstraintValidator<EnumPattern, Enum<?>> {
  private Pattern pattern;

  @Override
  public void initialize(EnumPattern annotation) {
    try {
      pattern = Pattern.compile(annotation.regexp());
    } catch (PatternSyntaxException e) {
      throw new IllegalArgumentException("Given regex is invalid", e);
    }
  }

  @Override
  public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    Matcher m = pattern.matcher(value.name());
    return m.matches();
  }
}
