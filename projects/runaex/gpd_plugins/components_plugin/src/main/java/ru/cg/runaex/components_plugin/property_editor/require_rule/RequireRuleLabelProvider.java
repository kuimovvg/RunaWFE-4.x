package ru.cg.runaex.components_plugin.property_editor.require_rule;

import org.eclipse.jface.viewers.LabelProvider;

import ru.cg.runaex.components.bean.component.part.RequireRuleComponentPart;
import ru.cg.runaex.components_plugin.Localization;
import ru.cg.runaex.components_plugin.component_parameter.descriptor.DescriptorLocalizationFactory;

public class RequireRuleLabelProvider extends LabelProvider {

    private Localization localization = DescriptorLocalizationFactory.getRequireRuleLocalization();

    @Override
    public String getText(Object element) {
        RequireRuleComponentPart requireRule = (RequireRuleComponentPart) element;

        String groovyScript = requireRule.getGroovyScript();
        if (groovyScript != null) {
            return groovyScript;
        }

        return requireRule.isUnconditionallyRequired() ? localization.get("required.yes") : localization.get("required.no");
    }

}
