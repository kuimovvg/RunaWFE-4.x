package ru.cg.runaex.components_plugin.property_editor.database;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class TableReferencePropertyDescriptor extends TextPropertyDescriptor {

    public TableReferencePropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
        this.setLabelProvider(new TableReferenceLabelProvider());
    }

    public CellEditor createPropertyEditor(Composite parent) {
        return new TableReferenceCellEditor(parent);
    }

}
