package ru.cg.runaex.components.validation.annotation;

import ru.cg.runaex.components.validation.validator.GridColumnValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author urmancheev
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {GridColumnValidator.class})
@Documented
public @interface GridColumnValidation {

  String message() default "{ru.cg.runaex.constraints.gridColumn}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
