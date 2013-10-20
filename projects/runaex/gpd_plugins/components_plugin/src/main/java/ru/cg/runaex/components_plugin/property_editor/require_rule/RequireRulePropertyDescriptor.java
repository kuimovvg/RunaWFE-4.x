package ru.cg.runaex.components_plugin.property_editor.require_rule;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class RequireRulePropertyDescriptor extends PropertyDescriptor {

    public RequireRulePropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
        this.setLabelProvider(new RequireRuleLabelProvider());
    }

    public CellEditor createPropertyEditor(Composite parent) {
        return new RequireRulePropertyCellEditor(parent);
    }

}
