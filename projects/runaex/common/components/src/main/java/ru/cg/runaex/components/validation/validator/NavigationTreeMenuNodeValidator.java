package ru.cg.runaex.components.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ru.cg.runaex.components.bean.component.NavigationTreeMenu;
import ru.cg.runaex.components.validation.annotation.NavigationTreeMenuNode;

/**
 * @author urmancheev
 */
public class NavigationTreeMenuNodeValidator implements ConstraintValidator<NavigationTreeMenuNode, NavigationTreeMenu.Node> {

  @Override
  public void initialize(NavigationTreeMenuNode constraintAnnotation) {
  }

  @Override
  public boolean isValid(NavigationTreeMenu.Node value, ConstraintValidatorContext context) {
    if (value == null)
      return true;

    return value.getTermCount() < 4;
  }
}
