package ru.cg.runaex.components.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ru.cg.runaex.components.validation.annotation.MaxLength;

/**
 * @author urmancheev
 */
public class MaxLengthValidator implements ConstraintValidator<MaxLength, String> {
  private int maxLength;

  @Override
  public void initialize(MaxLength constraintAnnotation) {
    this.maxLength = constraintAnnotation.value();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null)
      return true;
    return value.length() <= maxLength;
  }
}
