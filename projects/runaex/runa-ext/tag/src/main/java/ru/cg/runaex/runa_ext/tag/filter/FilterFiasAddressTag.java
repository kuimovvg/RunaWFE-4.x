package ru.cg.runaex.runa_ext.tag.filter;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Input;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.filter.FilterFiasAddress;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.runa_ext.tag.BaseFiasTag;

/**
 * @author urmancheev
 */
public class FilterFiasAddressTag extends BaseFiasTag<FilterFiasAddress> {
  private static final long serialVersionUID = -4669227433321278088L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.FILTER_FIAS_ADDRESS;
  }

  @Override
  protected String getGuidColumn(FilterFiasAddress component) {
    String field;
    if (component.getUsageActual()) {
      field = component.getField();
    }
    else {
      field = component.getFullField();
    }
    return field + Data.EQ_POSTFIX;
  }

  @Override
  protected String getFieldId() {
    return "filter_fias_address_" + System.nanoTime();
  }

  @Override
  protected String getDateFieldId() {
    return "filter_fias_address_date_" + System.nanoTime();
  }

  @Override
  protected String getStringValueColumn(FilterFiasAddress component) {
    return component.getField() + "_str";
  }

  @Override
  protected String getDateField(FilterFiasAddress component) {
    return component.getField() + "_date";
  }


  @Override
  protected SelectedValue getSelectedValue(FilterFiasAddress component) throws TemplateModelException {
    return new SelectedValue(null, null, null, null);
  }

  @Override
  protected void setAdditionalHiddenInputParameters(Input hiddenInput, FilterFiasAddress component) {
    hiddenInput.addAttribute("data-tableId", component.getTableId());
  }

  @Override
  protected boolean shouldSaveAllFields(FilterFiasAddress component) {
    return false;
  }
}
