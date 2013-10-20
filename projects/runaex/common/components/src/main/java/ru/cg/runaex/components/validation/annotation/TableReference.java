package ru.cg.runaex.components.validation.annotation;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

import ru.cg.runaex.components.validation.validator.TableReferenceValidator;

/**
 * @author urmancheev
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TableReferenceValidator.class)
@Documented
public @interface TableReference {

  String message() default "{ru.cg.runaex.constraints.tableReference}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
