package ru.cg.runaex.components.validation.annotation;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;

/**
 * @author urmancheev
 */
@NotNull(message = "{ru.cg.runaex.constraints.notNullSchema}")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface NotNullSchema {

  String message() default "{ru.cg.runaex.constraints.notNullSchema}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
