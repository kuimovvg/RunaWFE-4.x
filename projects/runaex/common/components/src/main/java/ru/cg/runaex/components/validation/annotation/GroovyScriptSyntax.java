package ru.cg.runaex.components.validation.annotation;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

import ru.cg.runaex.components.validation.validator.GroovyScriptValidator;

/**
 * @author Kochetkov
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GroovyScriptValidator.class)
@Documented
public @interface GroovyScriptSyntax {
  String message() default "{ru.cg.runaex.constraints.groovySyntax}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
