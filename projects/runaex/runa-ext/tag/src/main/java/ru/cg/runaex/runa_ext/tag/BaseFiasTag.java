package ru.cg.runaex.runa_ext.tag;

import java.util.ArrayList;
import java.util.List;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;

import ru.cg.fias.search.core.client.bean.ObjectLevel;
import ru.cg.runaex.components.bean.component.FiasComponent;
import ru.cg.runaex.components.bean.component.part.FiasObjectLevel;

/**
 * @author urmancheev
 */
public abstract class BaseFiasTag<C extends FiasComponent> extends BaseEditableFreemarkerTag<C> {
  private static final long serialVersionUID = -9032877038406050762L;

  private static final String minSymbols = "3";
  private static final String queryDelay = "300";

  @Override
  protected String executeToHtml(C component) throws TemplateModelException {
    String schema = component.getSchema();
    String defaultFilter = component.getDefaultFilter() != null ? component.getDefaultFilter() : "";

    String guidColumn = getGuidColumn(component);
    String fieldId = getFieldId();
    String dateFieldId = getDateFieldId();

    String stringValueColumn = getStringValueColumn(component);
    String dateField = getDateField(component);

    int fiasMinLevel = convertMinObjectLevel(component.getMinObjectLevel());
    int fiasMaxLevel = convertMaxObjectLevel(component.getMaxObjectLevel());

    initObjectInfo(schema, component.getTable());
    SelectedValue selectedValues = getSelectedValue(component);

    String selectedDate = selectedValues.getSelectedDate();
    String selectedAddressGuId = selectedValues.getSelectedAddressGuId();
    String selectedValue = selectedValues.getSelectedValue();

    Input input = new Input(Input.TEXT);
    input.setID(fieldId);
    String inputName = component.getUsageActual() ? stringValueColumn + "_fias-name" : stringValueColumn;
    input.setName(inputName);
    if (!component.getUsageActual()) {
      if (shouldSaveAllFields(component)) {
        input.addAttribute("data-schema", schema);
        input.addAttribute("data-table", component.getTable());
      }
      input.addAttribute("readonly", "");
    }
    if (selectedValue != null && !selectedValue.isEmpty()) {
      input.setValue(selectedValue);
    }

    if (!component.getUsageActual()) {
      input.setClass("runaex ac-input fias-input fias-input-usageversion");
    }
    else {
      input.setClass("runaex ac-input fias-input");
    }
    Input inputHidden = new Input(Input.HIDDEN);
    inputHidden.setClass("runaex ac-hidden-input");
    inputHidden.setName(guidColumn);
    inputHidden.setID(guidColumn);
    inputHidden.addAttribute("data-schema", schema);
    inputHidden.addAttribute("data-table", component.getTable());
    setAdditionalHiddenInputParameters(inputHidden, component);

    if (selectedAddressGuId != null && !selectedAddressGuId.isEmpty()) {
      inputHidden.setValue(selectedAddressGuId);
    }

    StringBuilder html = new StringBuilder();
    if (!component.getUsageActual()) {
      Input inputDate = new Input(Input.TEXT, dateField);
      inputDate.setID(dateFieldId);
      inputDate.setReadOnly(true);
      inputDate.setClass("runaex sdtpt-datetimepicker fias-input-date");
      if (shouldSaveAllFields(component)) {
        inputDate.addAttribute("data-schema", schema);
        inputDate.addAttribute("data-table", component.getTable());
      }

      if (selectedDate != null) {
        inputDate.setValue(selectedDate);
      }

      IMG img = new IMG("resources/images/datetime/cal.gif");
      img.setClass("runaex sdtpt-datetimepicker");
      boolean isDateTime = false;
      img.setOnClick(new StringBuilder().append("javascript:NewCssCal('").append("resources/images/datetime/','").append(dateFieldId).append("','ddMMyyyy','dropdown',").append(String.valueOf(isDateTime)).append(",'24',true)").toString());

      if (!isEditable) {
        inputDate.addAttribute("disabled", "");
        img.addAttribute("disabled", "");
        input.addAttribute("disabled", "");
        inputHidden.addAttribute("disabled", "");
      }

      List<WrapComponent> wrapComponents = new ArrayList<WrapComponent>(2);
      wrapComponents.add(new WrapComponent(null, inputDate, img));
      wrapComponents.add(new WrapComponent(null, input, inputHidden));
      String validationHtml = getHtmlWithValidation(wrapComponents);
      html.append(validationHtml);
    }
    else {
      if (!isEditable) {
        input.addAttribute("disabled", "");
        inputHidden.addAttribute("disabled", "");
      }

      String validationHtml = getHtmlWithValidation(new WrapComponent(null, input, inputHidden));
      html.append(validationHtml);
    }

    appendComponentCssReference(FiasAddressTag, html);
    appendCssReference(JqueryUiAutoCompleteCss, html);
    appendComponentCssReference(DateTimePickerTag, html);
    appendComponentJsReference(DateTimePickerTag, html);

    String guildColumnWithGap = guidColumn.replaceAll("\\s+", "\\\\\\\\ ");
    setJsTemplateName(FiasAddressTag);
    addObjectToJs("fieldId", fieldId);
    addObjectToJs("dateFieldId", dateFieldId);
    addObjectToJs("guidColumn", guidColumn);
    addObjectToJs("guildColumnWithGap", guildColumnWithGap);
    addObjectToJs("fiasMinLevel", fiasMinLevel);
    addObjectToJs("fiasMaxLevel", fiasMaxLevel);
    addObjectToJs("defaultFilter", defaultFilter);
    addObjectToJs("usageActual", component.getUsageActual());
    addObjectToJs("minSymbols", minSymbols);
    addObjectToJs("queryDelay", queryDelay);
    return html.toString();
  }

  private int convertMinObjectLevel(FiasObjectLevel minObjectLevel) {
    int fiasMinLevel;
    switch (minObjectLevel) {
      case REGION:
        fiasMinLevel = ObjectLevel.getMinLevelByRegion().getLevel();
        break;
      case DISTRICT:
        fiasMinLevel = ObjectLevel.getMinLevelByDistrict().getLevel();
        break;
      case CITY:
        fiasMinLevel = ObjectLevel.getMinLevelByCity().getLevel();
        break;
      case STREET:
        fiasMinLevel = ObjectLevel.getMinLevelByStreet().getLevel();
        break;
      default:
        fiasMinLevel = -1;
    }
    return fiasMinLevel;
  }

  private int convertMaxObjectLevel(FiasObjectLevel maxObjectLevel) {
    int fiasMaxLevel;
    switch (maxObjectLevel) {
      case REGION:
        fiasMaxLevel = ObjectLevel.getMaxLevelByRegion().getLevel();
        break;
      case DISTRICT:
        fiasMaxLevel = ObjectLevel.getMaxLevelByDistrict().getLevel();
        break;
      case CITY:
        fiasMaxLevel = ObjectLevel.getMaxLevelByCity().getLevel();
        break;
      case STREET:
        fiasMaxLevel = ObjectLevel.getMaxLevelByStreet().getLevel();
        break;
      default:
        fiasMaxLevel = -1;
    }
    return fiasMaxLevel;
  }

  protected abstract String getGuidColumn(C component);

  protected abstract String getFieldId();

  protected abstract String getDateFieldId();

  protected abstract String getStringValueColumn(C component);

  protected abstract String getDateField(C component);

  protected abstract SelectedValue getSelectedValue(C component) throws TemplateModelException;

  protected void setAdditionalHiddenInputParameters(Input hiddenInput, C component) {
  }

  protected boolean shouldSaveAllFields(C component) {
    return !component.getUsageActual();
  }

  protected StringBuilder getHtmlWithValidationDiv(List<WrapComponent> list) {
    StringBuilder sb = new StringBuilder();
    for (WrapComponent wrapComponent : list) {
      sb.append("<div class=\"control-group fias-input-inline\"><div class=\"controls inline fias-input-inline\">").
          append(wrapComponent.getElem1().toString());
      if (wrapComponent.getElem2() != null)
        sb.append(wrapComponent.getElem2().toString());
      if (wrapComponent.getLabel() != null)
        sb.append(wrapComponent.getLabel().toString());
      sb.append("</div>");
      sb.append("</div>");
    }
    return sb;
  }

  protected static class SelectedValue {
    private Long selectedRowId;
    private String selectedAddressGuId;
    private String selectedValue;
    private String selectedDate;

    public SelectedValue(Long selectedRowId, String selectedAddressGuId, String selectedValue, String selectedDate) {
      this.selectedRowId = selectedRowId;
      this.selectedAddressGuId = selectedAddressGuId;
      this.selectedValue = selectedValue;
      this.selectedDate = selectedDate;
    }

    public Long getSelectedRowId() {
      return selectedRowId;
    }

    public String getSelectedAddressGuId() {
      return selectedAddressGuId;
    }

    public String getSelectedValue() {
      return selectedValue;
    }

    public String getSelectedDate() {
      return selectedDate;
    }
  }
}
