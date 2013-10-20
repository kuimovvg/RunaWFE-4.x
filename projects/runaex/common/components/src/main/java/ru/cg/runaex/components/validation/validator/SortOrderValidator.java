package ru.cg.runaex.components.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ru.cg.runaex.components.validation.annotation.SortOrder;

/**
 * @author urmancheev
 */
public class SortOrderValidator implements ConstraintValidator<SortOrder, String> {

  @Override
  public void initialize(SortOrder constraintAnnotation) {
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null)
      return true;

    return "asc".equalsIgnoreCase(value) || "desc".equalsIgnoreCase(value);
  }
}