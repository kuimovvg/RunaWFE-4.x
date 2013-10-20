package ru.cg.runaex.components.validation.annotation;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

import ru.cg.runaex.components.validation.validator.DatabaseStructureElementValidator;

/**
 * @author urmancheev
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {DatabaseStructureElementValidator.class})
@Documented
public @interface DatabaseStructureElement {

  String message() default "{ru.cg.runaex.constraints.databaseStructureElement}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
