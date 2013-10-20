package ru.cg.runaex.components_plugin.property_editor.require_rule;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class GroovyRulePropertyDescriptor extends PropertyDescriptor {

    public GroovyRulePropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
        this.setLabelProvider(new GroovyRuleLabelProvider());
    }

    public CellEditor createPropertyEditor(Composite parent) {
        return new GroovyRulePropertyCellEditor(parent);
    }

}
