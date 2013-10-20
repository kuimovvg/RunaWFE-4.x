package ru.cg.runaex.components.validation.annotation;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

import ru.cg.runaex.components.validation.validator.AssertBooleanValidator;

/**
 * @author urmancheev
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AssertBooleanValidator.class)
@Documented
public @interface AssertBoolean {

  String message() default "{ru.cg.runaex.constraints.assertBoolean}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
