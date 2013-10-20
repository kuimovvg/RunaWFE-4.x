package ru.cg.runaex.components.validation.validator;

import javax.validation.ConstraintValidatorContext;

import ru.cg.runaex.components.bean.component.part.GridColumn;
import ru.cg.runaex.components.validation.annotation.EditableTreeGridColumnValidation;

/**
 * @author Kochetkov
 */
public class EditableTreeGridColumnValidator extends BaseValidator<EditableTreeGridColumnValidation, GridColumn> {

  @Override
  public void initialize(EditableTreeGridColumnValidation constraintAnnotation) {
  }

  @Override
  public boolean isValid(GridColumn value, ConstraintValidatorContext context) {
    if (value == null)
      return true;

    return value.getTermCount() < 6;
  }
}
