package ru.cg.runaex.components.validation.annotation;

import ru.cg.runaex.components.validation.validator.AssertNumberValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author urmancheev
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AssertNumberValidator.class)
@Documented
public @interface AssertNumber {

  String message() default "{ru.cg.runaex.constraints.assertNumber}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  boolean allowFractional() default true;

  boolean allowNegative() default false;
}
