package ru.cg.runaex.components.validation.annotation;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

import ru.cg.runaex.components.validation.validator.EditableTreeGridColumnValidator;

/**
 * @author Kochetkov
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {EditableTreeGridColumnValidator.class})
@Documented
public @interface EditableTreeGridColumnValidation {

  String message() default "{ru.cg.runaex.constraints.editableTreeGridColumn}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
