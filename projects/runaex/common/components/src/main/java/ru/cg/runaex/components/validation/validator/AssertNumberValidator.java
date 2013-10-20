package ru.cg.runaex.components.validation.validator;

import java.math.BigDecimal;
import javax.validation.ConstraintValidatorContext;

import ru.cg.runaex.components.validation.annotation.AssertNumber;

/**
 * @author urmancheev
 */
public class AssertNumberValidator extends BaseValidator<AssertNumber, String> {
  private boolean allowFractional;
  private boolean allowNegative;

  @Override
  public void initialize(AssertNumber constraintAnnotation) {
    allowFractional = constraintAnnotation.allowFractional();
    allowNegative = constraintAnnotation.allowNegative();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null)
      return true;

    boolean isValid = true;
    try {
      BigDecimal valueNum = new BigDecimal(value);
      if (!allowNegative && !(BigDecimal.ZERO.compareTo(valueNum) < 0)) {
        isValid = false;
        addParametrizedConstraintViolation("negative", context);
      }
      else if (!allowFractional) {
        isValid = valueNum.equals(valueNum.setScale(0));
        if (!isValid)
          addParametrizedConstraintViolation("int", context);
      }
    }
    catch (NumberFormatException ex) {
      isValid = false;
    }
    catch (ArithmeticException ex) {
      isValid = false;
      addParametrizedConstraintViolation("int", context);
    }

    return isValid;
  }
}
