package ru.cg.runaex.components_plugin.property_editor.require_rule;

import org.eclipse.jface.viewers.LabelProvider;

import ru.cg.runaex.components.bean.component.part.DefaultValue;
import ru.cg.runaex.components_plugin.Localization;
import ru.cg.runaex.components_plugin.component_parameter.descriptor.DescriptorLocalizationFactory;

public class DefaultValueLabelProvider extends LabelProvider {
    private Localization localization = DescriptorLocalizationFactory.getSelectDefaultValueDialogLocalization();

    @Override
    public String getText(Object element) {
        DefaultValue defVal = (DefaultValue) element;
        String typeStr = "";

        switch (defVal.getType()) {
        case EXECUTE_GROOVY:
            typeStr = localization.get("dialog.defaultValueType.value.executeGroovy");
            break;
        case FROM_DB:
            typeStr = localization.get("dialog.defaultValueType.value.fromDb");
            break;
        case MANUAL:
            typeStr = localization.get("dialog.defaultValueType.value.manual");
            break;
        default:
            break;
        }
        String defValStr = "";

        if (!typeStr.isEmpty()) {
            defValStr = new StringBuilder(typeStr).append(": ").append(defVal.getValue()).toString();
        }
        return defValStr;
    }
}
