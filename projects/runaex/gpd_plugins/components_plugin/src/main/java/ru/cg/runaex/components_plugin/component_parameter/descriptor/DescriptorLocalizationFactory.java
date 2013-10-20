package ru.cg.runaex.components_plugin.component_parameter.descriptor;

import ru.cg.runaex.components_plugin.Localization;
import ru.cg.runaex.components_plugin.LocalizationImpl;

/**
 * @author urmancheev
 */
public final class DescriptorLocalizationFactory {
  private static Localization gridColumnsDialogLocalization = new LocalizationImpl("ru.cg.runaex.components_plugin.component_parameter.descriptor.GridColumnsDialogLocalization");
  private static Localization selectColumnReferenceDialogLocalization = new LocalizationImpl("ru.cg.runaex.components_plugin.component_parameter.descriptor.SelectColumnReferenceDialogLocalization");
  private static Localization selectDefaultValueLocalization = new LocalizationImpl("ru.cg.runaex.components_plugin.component_parameter.descriptor.SelectDefaultValueDialogLocalization");
  private static Localization selectTableReferenceDialogLocalization = new LocalizationImpl("ru.cg.runaex.components_plugin.component_parameter.descriptor.SelectTableReferenceDialogLocalization");
  private static Localization requireRuleLocalization = new LocalizationImpl("ru.cg.runaex.components_plugin.component_parameter.descriptor.RequireRuleLocalization");

  public static Localization getGridColumnsDialogLocalization() {
    return gridColumnsDialogLocalization;
  }
  
  public static Localization getSelectColumnReferenceDialogLocalization() {
	    return selectColumnReferenceDialogLocalization;
	  }

  public static Localization getSelectDefaultValueDialogLocalization() {
    return selectDefaultValueLocalization;
  }
  
  public static Localization getSelectTableReferenceDialogLocalization() {
	    return selectTableReferenceDialogLocalization;
	  }
  
  public static Localization getRequireRuleLocalization() {
    return requireRuleLocalization;
  }
}
