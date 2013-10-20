package ru.cg.runaex.components.validation.annotation;

import ru.cg.runaex.components.validation.validator.RegexValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author urmancheev
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {RegexValidator.class})
@Documented
public @interface Regex {

  String message() default "{ru.cg.runaex.constraints.regex}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
