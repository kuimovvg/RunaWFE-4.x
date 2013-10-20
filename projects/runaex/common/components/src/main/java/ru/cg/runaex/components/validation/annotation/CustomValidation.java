package ru.cg.runaex.components.validation.annotation;

import ru.cg.runaex.components.validation.validator.CustomValidationValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author urmancheev
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomValidationValidator.class)
@Documented
public @interface CustomValidation {

  String message() default "{ru.cg.runaex.constraints}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
