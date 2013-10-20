package ru.cg.runaex.runa_ext.tag.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Label;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.field.RadioButtonGroup;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.runa_ext.tag.utils.DefaultValueUtil;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.runa_ext.tag.BaseEditableFreemarkerTag;
import ru.cg.runaex.runa_ext.tag.bean.DefaultValue;

/**
 * @author Sabirov
 */
public class RadioButtonGroupTag extends BaseEditableFreemarkerTag<RadioButtonGroup> {
  private static final long serialVersionUID = 5346445981601736352L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.RADIO_BUTTON_GROUP;
  }

  @Override
  protected String executeToHtml(RadioButtonGroup component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("schema - " + component.getSchema());
    logger.debug("table - " + component.getTable());
    logger.debug("field - " + component.getField());
    logger.debug("references - " + component.getColumnReference());
    logger.debug("defaultValue - " + component.getDefaultValue());

    String schema = component.getSchema();
    String sortOrder = component.getSortOrder();
    if (sortOrder == null) {
      sortOrder = "asc"; //todo use constant
    }

    initObjectInfo(schema, component.getTable());

    String radioSchema = component.getColumnReference().getSchema();
    String radioTable = component.getColumnReference().getTable();
    String radioField = component.getColumnReference().getColumn();
    String radioId = radioTable + "_id"; //todo use postfix constant

    /**
     * Selected id in base table
     */
    Long selectedRowId = getSelectedRowId();
    DefaultValue defaultValue = getDefaultValue(selectedRowId, component, schema);

    TransportDataSet transportDataSet = getDataFromDB(radioSchema, radioTable, Arrays.asList(radioId, radioField), radioField, sortOrder);

    List<WrapComponent> list = new ArrayList<WrapComponent>();
    for (TransportData transportData : transportDataSet.getSortSets()) {
      Data dataId = transportData.getData(radioId);
      Data dataValue = transportData.getData(radioField);
      logger.debug("dataId.getValue() - " + dataId.getValue());
      logger.debug("dataId.getValueClass() - " + dataId.getValueClass());
      logger.debug("dataId.getField() - " + dataId.getField());
      Long id = (Long) dataId.getValue();
      String name = (String) dataValue.getValue();

      Input input = new Input();
      input.setClass("runaex radio check-error-above");
      input.setType(Input.radio);
      input.setName(component.getField());
      input.addAttribute("data-schema", schema);
      input.addAttribute("data-table", component.getTable());
      input.addAttribute("column-reference", component.getColumnReference());

      if (id != null) {
        input.setValue(String.valueOf(id));
      }
      if (defaultValue.getId() != null && defaultValue.getId().equals(id) || selectedRowId == null && name.equals(defaultValue.getValue())) { //todo code reuse
        input.setChecked(true);
      }
      Label label = new Label();
      label.setClass("runaex radio-label");
      label.setTagText(" " + dataValue.getValue() + " ");

      if (!isEditable) {
        input.addAttribute("disabled", "");
      }

      list.add(new WrapComponent(label, input));
    }

    StringBuilder htmlBuilder = new StringBuilder();
    appendComponentCssReference(RadioButtonGroupTag, htmlBuilder);

    String validationHtml = getHtmlWithValidation(list);
    htmlBuilder.append(validationHtml);

    return htmlBuilder.toString();
  }

  @Override
  protected StringBuilder getHtmlWithValidationDiv(List<WrapComponent> list) {
    StringBuilder sb = new StringBuilder("<div class=\"control-group\">");
    sb.append("<div class=\"controls inline\">");
    sb.append("<fieldset>");
    for (WrapComponent wrapComponent : list) {
      sb.append(wrapComponent.getElem1().toString());
      if (wrapComponent.getElem2() != null)
        sb.append(wrapComponent.getElem2().toString());
      if (wrapComponent.getLabel() != null)
        sb.append(wrapComponent.getLabel().toString());
    }
    sb.append("</fieldset>");
    sb.append("</div>");
    sb.append("</div>");
    return sb;
  }

  private DefaultValue getDefaultValue(Long selectedRowId, RadioButtonGroup component, String schema) throws TemplateModelException {
    //todo code reuse
    Long selectId = null;
    Object defaultValue = DefaultValueUtil.getDefaultValue(component.getDefaultValue(), variableProvider);
    String defaultValueStr = String.valueOf(defaultValue);

    if (selectedRowId != null) {
      selectId = getValue(selectedRowId, schema, component.getTable(), component.getField(), null, Long.class, null);
    }
    else if (defaultValue instanceof ColumnReference) {
      ColumnReference defaultValueReference = (ColumnReference) defaultValue;
      String defaultValueReferenceSchema = defaultValueReference.getSchema();
      String defaultValueReferenceTable = defaultValueReference.getTable();
      String defaultValueReferenceColumn = defaultValueReference.getColumn();

      String referenceStr = component.getColumnReference().toString();

      ObjectInfo refObjectInfo = getObjectInfo(defaultValueReferenceSchema, defaultValueReferenceTable);
      if (refObjectInfo != null) {
        defaultValueStr = getValue(refObjectInfo.getId(), defaultValueReferenceSchema, defaultValueReferenceTable,
            defaultValueReferenceColumn, referenceStr, String.class, "");
      }
    }

    return new DefaultValue(selectId, defaultValueStr);
  }
}
