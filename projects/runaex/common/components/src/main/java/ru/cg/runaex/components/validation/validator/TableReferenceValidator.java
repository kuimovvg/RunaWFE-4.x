package ru.cg.runaex.components.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ru.cg.runaex.components.validation.annotation.TableReference;

/**
 * @author urmancheev
 */
public class TableReferenceValidator implements ConstraintValidator<TableReference, ru.cg.runaex.components.bean.component.part.TableReference> {

  @Override
  public void initialize(TableReference constraintAnnotation) {
  }

  @Override
  public boolean isValid(ru.cg.runaex.components.bean.component.part.TableReference value, ConstraintValidatorContext context) {
    if (value == null)
      return true;

    return value.getTermCount() <= 2;
  }
}
