package ru.cg.runaex.components.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ru.cg.runaex.components.validation.annotation.GroovyScriptSyntax;

/**
 * @author Kochetkov
 */
public class GroovyScriptValidator implements ConstraintValidator<GroovyScriptSyntax, String> {

  @Override
  public void initialize(GroovyScriptSyntax constraintAnnotation) {
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
//    if (value == null)
//      return true;
//
//    return GroovyScriptValidationHelper.isValid(value);
    return true;
  }
}
