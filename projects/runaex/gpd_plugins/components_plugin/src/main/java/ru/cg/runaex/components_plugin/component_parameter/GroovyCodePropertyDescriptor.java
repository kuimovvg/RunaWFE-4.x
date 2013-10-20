package ru.cg.runaex.components_plugin.component_parameter;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class GroovyCodePropertyDescriptor extends PropertyDescriptor {

    public GroovyCodePropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
        this.setLabelProvider(new LabelProvider());
    }

    public CellEditor createPropertyEditor(Composite parent) {
        return new GroovyCodePropertyCellEditor(parent);
    }

}