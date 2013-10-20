package ru.cg.runaex.components_plugin.property_editor.database;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class ColumnReferencePropertyDescriptor extends PropertyDescriptor {

    public ColumnReferencePropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
        this.setLabelProvider(new ColumnReferenceLabelProvider());
    }

    public CellEditor createPropertyEditor(Composite parent) {
        return new ColumnReferencePropertyCellEditor(parent);
    }

}
