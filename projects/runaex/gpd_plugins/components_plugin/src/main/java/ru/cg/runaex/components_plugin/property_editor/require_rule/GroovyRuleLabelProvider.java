package ru.cg.runaex.components_plugin.property_editor.require_rule;

import org.eclipse.jface.viewers.LabelProvider;

import ru.cg.runaex.components.bean.component.part.GroovyRuleComponentPart;

public class GroovyRuleLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        GroovyRuleComponentPart groovyRule = (GroovyRuleComponentPart) element;
        String groovyScript = groovyRule.getGroovyScript();
        return groovyScript != null ? groovyScript : "";
    }

}
