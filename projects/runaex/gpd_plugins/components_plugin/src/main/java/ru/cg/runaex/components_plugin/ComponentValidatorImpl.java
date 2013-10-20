package ru.cg.runaex.components_plugin;

import ru.cg.runaex.validation.ErrorMessage;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.formeditor.ftl.validation.FreemarkerTag;
import ru.runa.gpd.formeditor.ftl.validation.FreemarkerTagValidator;
import ru.runa.gpd.formeditor.ftl.validation.ValidationMessage;

import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.Component;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.util.ComponentUtil;
import ru.cg.runaex.validation.component.ComponentValidatorBaseImpl;

import java.util.*;

/**
 * @author urmancheev
 */
public class ComponentValidatorImpl extends ComponentValidatorBaseImpl implements FreemarkerTagValidator {
  private ResourceBundle componentsResourceBundle;

  public ComponentValidatorImpl() {
    componentsResourceBundle = ComponentsPluginActivator.getResourceBundle();

    setValidator(ComponentsPluginActivator.getValidator());
  }

  @Override
  public List<ValidationMessage> validateTag(FreemarkerTag freemarkerTag, Map<String, Variable> variables) {
    ComponentType componentType = ComponentUtil.getComponentType(freemarkerTag.getName());

    String[] parameters = new String[freemarkerTag.getParameters().size()];
    freemarkerTag.getParameters().toArray(parameters);

    Variable defaultSchemaVariable = variables.get(WfeRunaVariables.DEFAULT_SCHEMA_VARIABLE_NAME);
    String defaultSchema = defaultSchemaVariable != null ? defaultSchemaVariable.getDefaultValue() : null;

    Component component = ComponentUtil.createComponent(componentType);
    component.init(freemarkerTag.getName(), componentType, parameters);
    component.setDefaultSchema(defaultSchema);
    Map<String, String> parameter = new HashMap<String, String>();
    parameter.put("element", freemarkerTag.getName());
    Collection<ErrorMessage> problems = validate(component, parameter);
    return convertProblems(problems);
  }

  private List<ValidationMessage> convertProblems(Collection<ErrorMessage> problems) {
    List<ValidationMessage> converted = new ArrayList<ValidationMessage>(problems.size());

    for (ErrorMessage problem : problems) {
      converted.add(ValidationMessage.error(problem.toString()));
    }

    return converted;
  }

  @Override
  protected String getValidationMessageByCode(String componentCode) {
    return getComponentMessageByCode(componentCode);
  }

  @Override
  protected String getComponentMessageByCode(String code) {
    return componentsResourceBundle.getString(code);
  }
}
