package com.github.durex.shared.validator;

import static com.github.durex.shared.exceptions.model.ErrorCode.NOTHING_FAILED;
import static com.github.durex.shared.exceptions.model.ErrorCode.SAVE_ERROR;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.shared.annotation.AcceptedEnum;
import com.github.durex.shared.annotation.EnumClass;
import com.github.durex.shared.annotation.EnumPattern;
import com.github.durex.shared.exceptions.model.ErrorCode;
import com.github.durex.shared.utils.EnumValue;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidatorTest {
  @Mock private AcceptedEnum acceptedEnum;
  @Mock private EnumClass enumClass;
  @Mock private ConstraintValidatorContext constraintValidatorContext;
  @Mock private EnumPattern enumPattern;

  @Test
  void testAcceptedErrorCodeChecker() {
    Mockito.when(acceptedEnum.anyOf()).thenReturn(new ErrorCode[] {NOTHING_FAILED});
    var validator = new AcceptedEnumValidator();
    validator.initialize(acceptedEnum);
    boolean result = validator.isValid(ErrorCode.NOTHING_FAILED, constraintValidatorContext);
    assertTrue(result);
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void testEnumClassValidator() {
    var validator = new EnumClassValidator();
    var enumValue = new EnumValue(ErrorCode.class);
    Mockito.when(enumClass.enumClass()).thenReturn(enumValue.enumClass());
    validator.initialize(enumClass);
    boolean result = validator.isValid("NOTHING_FAILED", constraintValidatorContext);
    assertTrue(result);
  }

  @Test
  void testEnumPatternValidator() {
    var validator = new EnumPatternValidator();
    Mockito.when(enumPattern.regexp()).thenReturn("NOTHING_FAILED|SAVE_ERROR");
    validator.initialize(enumPattern);
    boolean result = validator.isValid(SAVE_ERROR, constraintValidatorContext);
    assertTrue(result);
  }
}
