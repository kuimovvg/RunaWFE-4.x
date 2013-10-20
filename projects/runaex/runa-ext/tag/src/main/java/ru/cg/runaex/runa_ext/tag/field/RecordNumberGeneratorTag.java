package ru.cg.runaex.runa_ext.tag.field;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Input;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.field.RecordNumberGenerator;
import ru.cg.runaex.runa_ext.tag.BaseFreemarkerTag;
import ru.cg.runaex.database.context.DatabaseSpringContext;

/**
 * @author korablev
 */
public class RecordNumberGeneratorTag extends BaseFreemarkerTag<RecordNumberGenerator> {
  private static final long serialVersionUID = 626161819341914946L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.RECORD_NUMBER_GENERATOR;
  }

  @Override
  protected String executeToHtml(RecordNumberGenerator component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    String schema = component.getSchema();

    initObjectInfo(schema, component.getTable());
    Input input;
    if (component.showGenerationProcess()) {
      input = new Input(Input.TEXT, component.getField());
      input.setClass("runaex textfield speech");
    }
    else {
      input = new Input(Input.HIDDEN, component.getField());
      input.setClass("runaex save-hidden-input");
    }
    input.setID("record_number_" + System.nanoTime());
    input.setReadOnly(true);
    input.addAttribute("data-schema", schema);
    input.addAttribute("data-table", component.getTable());
    input.addAttribute("pattern", getFormattedPattern(component.getPattern()));
    input.addAttribute("generate-automatically", !component.showGenerationProcess());
    input.setValue(getValue(component));

    return getHtmlWithValidation(new WrapComponent(null, input));
  }

  private String getValue(RecordNumberGenerator component) {
    if (component.showGenerationProcess())
      return formatValue(component.getPattern(), getNextValue(component.getSchema(), component.getFormattedSequenceName()));
    else {
      return component.getFormattedSequenceName();
    }
  }

  private Long getNextValue(String schemaName, String sequenceName) {
    return DatabaseSpringContext.getComponentDbServices().getBaseDaoService().getSequenceNextValue(getProcessDefinitionId(), schemaName, sequenceName);
  }

  private String formatValue(String pattern, Long value) {
    return String.format(getFormattedPattern(pattern), value);
  }

  private String getFormattedPattern(String pattern) {
    return pattern.replaceAll("%", "%d");
  }
}