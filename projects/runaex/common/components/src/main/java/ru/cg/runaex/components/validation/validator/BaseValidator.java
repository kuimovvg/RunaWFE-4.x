package ru.cg.runaex.components.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

/**
 * @author urmancheev
 */
public abstract class BaseValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {
  private static final Pattern APPEND_PARAMETER_PATTERN = Pattern.compile("\\}$");

  protected void addParametrizedConstraintViolation(String parameter, ConstraintValidatorContext context) {
    String messageTemplate = context.getDefaultConstraintMessageTemplate();
    boolean parameterIsEmpty = parameter.isEmpty();

    context.disableDefaultConstraintViolation();

    if (!parameterIsEmpty) {
      messageTemplate = APPEND_PARAMETER_PATTERN.matcher(messageTemplate).replaceAll("." + parameter + "}");
    }

    context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
  }
}
