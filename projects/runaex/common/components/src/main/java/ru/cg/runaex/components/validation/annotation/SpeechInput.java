package ru.cg.runaex.components.validation.annotation;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @author urmancheev
 */
@AssertBoolean
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface SpeechInput {

  String message() default "{ru.cg.runaex.constraints.speechInput}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
