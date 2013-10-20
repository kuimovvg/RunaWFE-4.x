package ru.cg.runaex.runa_ext.tag.field;

import java.util.List;

import freemarker.template.TemplateModelException;

import ru.cg.fias.search.core.server.bean.AddressSphinx;
import ru.cg.fias.search.core.server.sphinx.SphinxException;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.field.FiasAddress;
import ru.cg.runaex.database.context.DatabaseSpringContext;
import ru.cg.runaex.runa_ext.tag.BaseFiasTag;

/**
 * @author Абдулин Ильдар
 */
public class FiasAddressTag extends BaseFiasTag<FiasAddress> {
  private static final long serialVersionUID = -5021816421946491357L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.FIAS_ADDRESS;
  }

  @Override
  protected String getGuidColumn(FiasAddress component) {
    if (component.getUsageActual()) {
      return component.getField();
    }
    else {
      //Если используется не актуальные данные, то в главную колонку кладем не AO_ID объекта а сам адрес.
      return component.getFullField();
    }
  }

  @Override
  protected String getFieldId() {
    return "fias_address_" + System.nanoTime();
  }

  @Override
  protected String getDateFieldId() {
    return "fias_address_date_" + System.nanoTime();
  }

  @Override
  protected String getStringValueColumn(FiasAddress component) {
    if (component.getUsageActual()) {
      return component.getField();
    }
    else {
      //Если используется не актуальные данные, то в главную колонку кладем не AO_ID объекта а сам адрес.
      return component.getField();
    }
  }

  @Override
  protected String getDateField(FiasAddress component) {
    String dateField = component.getDateField();
    if (dateField == null) {
      dateField = component.getDateField() + "_date"; //todo use constant
    }
    return dateField;
  }

  @Override
  protected SelectedValue getSelectedValue(FiasAddress component) throws TemplateModelException {
    String schema = component.getSchema();
    String guidColumn = getGuidColumn(component);
    String stringValueColumn = getStringValueColumn(component);
    String dateField = getDateField(component);

    String selectedAddressGuId = null;
    String selectedValue = null;
    String date = null;

    Long selectedRowId = getSelectedRowId();
    if (selectedRowId != null) {
      selectedAddressGuId = getValue(selectedRowId, schema, component.getTable(), guidColumn, null, String.class, null);
      if (selectedAddressGuId != null) {
        //Если используется не исторический, то берем самый актуальный, в ином случае берем полный адрес с базы
        if (component.getUsageActual()) {
          selectedValue = getValue(selectedAddressGuId);
        }
        else {
          selectedValue = getValue(selectedRowId, schema, component.getTable(), stringValueColumn, null, String.class, null);
        }
      }
      if (!component.getUsageActual()) {
        date = getValue(selectedRowId, schema, component.getTable(), dateField, null, String.class, null);
      }
    }
    else {
      if (component.getDefaultValueReference() != null) {
        selectedAddressGuId = copyValue(component.getDefaultValueReference(), String.class, "");
        if (component.getUsageActual() && selectedAddressGuId != null) {
          selectedValue = getValue(selectedAddressGuId);
        }
        else {
          selectedValue = selectedAddressGuId;
        }
      }
    }

    return new SelectedValue(selectedRowId, selectedAddressGuId, selectedValue, date);
  }

  private static String getValue(String guId) throws TemplateModelException {
    List<AddressSphinx> foundList;
    try {
      foundList = DatabaseSpringContext.getSphinxStrAddressByGuidReader().search(guId, 1);
    }
    catch (SphinxException e) {
      logger.error(e.getMessage(), e);
      throw new TemplateModelException(e.toString(), e);
    }

    if (foundList == null || foundList.size() < 1) {
      return "";
    }

    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 6; i >= 1; i--) {
      if (foundList.get(0).get("s" + i) != null && !foundList.get(0).<String>get("s" + i).isEmpty()) {
        stringBuilder.append(foundList.get(0).get("s" + i));
        if (i != 1) {
          stringBuilder.append(", ");
        }
      }
    }

    return stringBuilder.toString();
  }
}
