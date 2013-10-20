package ru.cg.runaex.validation.component;

import ru.cg.runaex.components.bean.component.IsComponent;
import ru.cg.runaex.components.validation.ColumnReferenceSequence;
import ru.cg.runaex.components.validation.NotNullSequence;
import ru.cg.runaex.validation.ErrorMessage;
import ru.cg.runaex.validation.MessagePart;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author urmancheev
 */
public abstract class ComponentValidatorBaseImpl implements ComponentValidator {
  private Validator validator;

  private static final Pattern PLACE_IN_ARRAY_PATTERN = Pattern.compile("\\[(.+?)\\]");

  public void setValidator(Validator validator) {
    this.validator = validator;
  }

  @Override
  public List<ErrorMessage> validate(IsComponent component, Map<String, String> parameters) {
    List<ErrorMessage> errorMessages = new LinkedList<ErrorMessage>();
    Set<ConstraintViolation<IsComponent>> constraintViolations = validator.validate(component);
    Set<ConstraintViolation<IsComponent>> constraintViolationsReference = validator.validate(component, ColumnReferenceSequence.class);
    constraintViolations.addAll(constraintViolationsReference);
    if (constraintViolationsReference.isEmpty()) {
      Set<ConstraintViolation<IsComponent>> constraintViolationsReference1 = validator.validate(component, NotNullSequence.class);
      constraintViolations.addAll(constraintViolationsReference1);
    }
    if (!constraintViolations.isEmpty()) {
      errorMessages.addAll(createErrorMessage(constraintViolations, parameters));
    }
    return errorMessages;
  }

  private List<ErrorMessage> createErrorMessage(Set<ConstraintViolation<IsComponent>> constraintViolations, Map<String, String> parameters) {
    List<ErrorMessage> errorMessages = new LinkedList<ErrorMessage>();
    List<MessagePart> messageParts = new LinkedList<MessagePart>();
    List<MessagePart> generalMessageParts = new LinkedList<MessagePart>();
    generalMessageParts.add(addErrorMessage(parameters.get("businessProcess"), getComponentMessageByCode("businessProcess"), false));
    generalMessageParts.add(addErrorMessage(parameters.get("nodeAction"),  getComponentMessageByCode("nodeAction"), false));
    generalMessageParts.add(addErrorMessage(getComponentMessageByCode("Method." + parameters.get("element")), getComponentMessageByCode("element"), false));
    for (ConstraintViolation<IsComponent> violation : constraintViolations) {
      messageParts.addAll(generalMessageParts);
      if (violation.getPropertyPath().toString().isEmpty()) {
        messageParts.add(addErrorMessage(violation.getMessage(), "", true));
      }
      else {
        messageParts.addAll(addFieldMessage(parameters.get("element"), violation.getPropertyPath().toString(), violation.getMessage()));
      }
      errorMessages.add(new ErrorMessage(messageParts));
      messageParts = new LinkedList<MessagePart>();
    }
    return errorMessages;
  }

  private MessagePart addErrorMessage(String message, String title, boolean isErrorMessage) {
    MessagePart messagePart = new MessagePart();
    messagePart.setMessage(message);
    messagePart.setTitle(title);
    messagePart.setErrorMessage(isErrorMessage);
    return messagePart;
  }

  private List<MessagePart> addFieldMessage(String componentName, String path, String error) {
    List<MessagePart> messageParts = new LinkedList<MessagePart>();
    String[] parts = path.split("\\.");
    for (int i = 0; i < parts.length; i++) {
      String part = parts[i].replaceAll("Str", "");
      if (i == 0)
        messageParts.add(addErrorMessage(getComponentMessageByCode("Param." + componentName + "." + part.replaceAll(PLACE_IN_ARRAY_PATTERN.pattern(), "")), getValidationMessageByCode("field"), false));
      else
        messageParts.add(addErrorMessage(getValidationMessageByCode(part), getValidationMessageByCode("fieldElement"), false));

    }
    messageParts.add(addErrorMessage(error, "", true));
    return messageParts;
  }

  protected abstract String getValidationMessageByCode(String componentCode);

  protected abstract String getComponentMessageByCode(String code);
}
