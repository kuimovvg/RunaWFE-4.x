package ru.cg.runaex.components.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ru.cg.runaex.components.validation.annotation.ColumnReference;
import ru.cg.runaex.components.validation.helper.ColumnReferenceValidationHelper;

/**
 * @author urmancheev
 */
public class ColumnReferenceValidator implements ConstraintValidator<ColumnReference, ru.cg.runaex.components.bean.component.part.ColumnReference> {

  @Override
  public void initialize(ColumnReference constraintAnnotation) {
  }

  @Override
  public boolean isValid(ru.cg.runaex.components.bean.component.part.ColumnReference value, ConstraintValidatorContext context) {
    if (value == null)
      return true;

    return ColumnReferenceValidationHelper.isValid(value);
  }
}
