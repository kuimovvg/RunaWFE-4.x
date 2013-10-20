package ru.cg.runaex.components.validation.annotation;

import ru.cg.runaex.components.validation.validator.SortOrderValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author urmancheev
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {SortOrderValidator.class})
@Documented
public @interface SortOrder {

  String message() default "{ru.cg.runaex.constraints.sortOrder}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
