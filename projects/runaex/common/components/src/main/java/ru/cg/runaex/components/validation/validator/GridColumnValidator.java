package ru.cg.runaex.components.validation.validator;

import javax.validation.ConstraintValidatorContext;

import ru.cg.runaex.components.bean.component.part.GridColumn;
import ru.cg.runaex.components.validation.annotation.GridColumnValidation;

/**
 * @author urmancheev
 */
public class GridColumnValidator extends BaseValidator<GridColumnValidation, GridColumn> {

  @Override
  public void initialize(GridColumnValidation constraintAnnotation) {
  }

  @Override
  public boolean isValid(GridColumn value, ConstraintValidatorContext context) {
    if (value == null)
      return true;

    return value.getTermCount() < 5;
  }
}
