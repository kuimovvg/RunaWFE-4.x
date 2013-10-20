package ru.cg.runaex.components.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ru.cg.runaex.components.validation.annotation.AssertBoolean;

/**
 * @author urmancheev
 */
public class AssertBooleanValidator implements ConstraintValidator<AssertBoolean, String> {

  @Override
  public void initialize(AssertBoolean constraintAnnotation) {
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null)
      return true;

    return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
  }
}
