package ru.cg.runaex.components.validation.annotation;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

import ru.cg.runaex.components.validation.validator.DefaultValueValidator;

/**
 * @author Kochetkov
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DefaultValueValidator.class)
@Documented
public @interface DefaultValue {

  String message() default "{ru.cg.runaex.constraints.defaultValue}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
