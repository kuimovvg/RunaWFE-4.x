package ru.cg.runaex.components.validation.validator;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidatorContext;

import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;

/**
 * @author urmancheev
 */
public class DatabaseStructureElementValidator extends BaseValidator<DatabaseStructureElement, String> {
  private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("[а-яА-Яa-zA-Z0-9\\-_ ]+");

  @Override
  public void initialize(DatabaseStructureElement constraintAnnotation) {
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null)
      return true;

    return ALLOWED_CHARACTERS.matcher(value).matches();
  }
}
