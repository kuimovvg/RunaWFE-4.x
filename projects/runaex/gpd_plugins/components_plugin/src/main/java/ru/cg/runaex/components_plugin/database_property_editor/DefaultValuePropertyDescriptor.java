package ru.cg.runaex.components_plugin.database_property_editor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.cg.runaex.components_plugin.property_editor.require_rule.DefaultValueLabelProvider;

public class DefaultValuePropertyDescriptor extends PropertyDescriptor {

    public DefaultValuePropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
        this.setLabelProvider(new DefaultValueLabelProvider());
    }

    public CellEditor createPropertyEditor(Composite parent) {
        return new DefaultValuePropertyCellEditor(parent);
    }

}
