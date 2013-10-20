package ru.cg.runaex.components.validation.annotation;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

import ru.cg.runaex.components.validation.validator.MaxLengthValidator;

/**
 * @author urmancheev
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxLengthValidator.class)
@Documented
public @interface MaxLength {

  int value();

  String message() default "{ru.cg.runaex.constraints.maxLength}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
