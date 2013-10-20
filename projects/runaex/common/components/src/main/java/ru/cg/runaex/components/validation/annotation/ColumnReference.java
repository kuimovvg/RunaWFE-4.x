package ru.cg.runaex.components.validation.annotation;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

import ru.cg.runaex.components.validation.validator.ColumnReferenceValidator;

/**
 * @author urmancheev
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ColumnReferenceValidator.class)
@Documented
public @interface ColumnReference {

  String message() default "{ru.cg.runaex.constraints.columnReference}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
