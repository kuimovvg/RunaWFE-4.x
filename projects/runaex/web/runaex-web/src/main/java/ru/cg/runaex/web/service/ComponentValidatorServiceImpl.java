package ru.cg.runaex.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import ru.cg.runaex.validation.ErrorMessage;
import ru.cg.runaex.database.bean.FtlComponent;
import ru.cg.runaex.validation.component.ComponentValidatorBaseImpl;

import javax.validation.Validator;
import java.util.*;

/**
 * @author Bagautdinov
 */
@Service
public class ComponentValidatorServiceImpl extends ComponentValidatorBaseImpl implements ComponentValidatorService {

  @Autowired
  @Qualifier("validationMessageSource")
  private ResourceBundleMessageSource validationMessageSource;

  @Autowired
  @Qualifier("componentsMessages")
  private ResourceBundleMessageSource componentsMessages;

  @Override
  @Autowired
  @Qualifier("componentsValidator")
  public void setValidator(Validator validator) {
    super.setValidator(validator);
  }

  public List<ErrorMessage> validateComponents(Collection<FtlComponent> ftlComponents) {
    List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>();
    for (FtlComponent ftlComponent : ftlComponents) {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("businessProcess", ftlComponent.getProcessName());
      parameters.put("nodeAction", ftlComponent.getFormsAsString());
      parameters.put("element", ftlComponent.getComponent().getComponentName());
      errorMessages.addAll(validate(ftlComponent.getComponent(), parameters));
    }
    return errorMessages;
  }

  @Override
  protected String getValidationMessageByCode(String componentCode) {
    return validationMessageSource.getMessage(componentCode, null, Locale.ROOT);
  }

  @Override
  protected String getComponentMessageByCode(String code) {
    return componentsMessages.getMessage(code, null, Locale.ROOT);
  }
}
