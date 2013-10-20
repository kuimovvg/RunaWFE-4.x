package ru.cg.runaex.components.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ru.cg.runaex.components.validation.annotation.DefaultValue;

/**
 * @author Kochetkov
 */
public class DefaultValueValidator implements ConstraintValidator<DefaultValue, ru.cg.runaex.components.bean.component.part.DefaultValue> {

  @Override
  public void initialize(DefaultValue constraintAnnotation) {
  }

  @Override
  public boolean isValid(ru.cg.runaex.components.bean.component.part.DefaultValue value, ConstraintValidatorContext context) {
//    if (value == null)
//      return true;
//
//    return DefaultValueValidationHelper.isValid(value);
    return true;
  }
}
