package ru.cg.runaex.components.validation.validator;

import java.util.List;
import javax.validation.ConstraintValidatorContext;

import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import ru.cg.runaex.components.validation.annotation.CustomValidation;

/**
 * @author urmancheev
 */
public class CustomValidationValidator extends BaseValidator<CustomValidation, ComponentWithCustomValidation> {

  @Override
  public void initialize(CustomValidation CustomValidating) {
  }

  @Override
  public boolean isValid(ComponentWithCustomValidation value, ConstraintValidatorContext context) {
    if (value == null)
      return true;

    List<String> errorCodes = value.customValidate();
    boolean isValid = errorCodes.isEmpty();
    if (!isValid) {
      for (String errorCode : errorCodes)
        addParametrizedConstraintViolation(errorCode, context);
    }

    return isValid;
  }
}
