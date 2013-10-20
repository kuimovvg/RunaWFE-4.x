package ru.cg.runaex.components.validation.annotation;

import ru.cg.runaex.components.validation.validator.NavigationTreeMenuNodeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author urmancheev
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {NavigationTreeMenuNodeValidator.class})
@Documented
public @interface NavigationTreeMenuNode {

  String message() default "{ru.cg.runaex.constraints.navigationTreeMenuNode}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
