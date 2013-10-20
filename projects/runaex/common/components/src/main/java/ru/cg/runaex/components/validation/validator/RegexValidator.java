package ru.cg.runaex.components.validation.validator;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ru.cg.runaex.components.validation.annotation.Regex;

/**
 * @author urmancheev
 */
public class RegexValidator implements ConstraintValidator<Regex, String> {

  @Override
  public void initialize(Regex constraintAnnotation) {
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null)
      return true;

    boolean isValid = true;
    try {
      Pattern.compile(value);
    }
    catch (PatternSyntaxException ex) {
      isValid = false;
    }
    return isValid;
  }
}
