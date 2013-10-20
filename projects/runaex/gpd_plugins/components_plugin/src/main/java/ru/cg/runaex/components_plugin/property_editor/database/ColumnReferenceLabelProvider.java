package ru.cg.runaex.components_plugin.property_editor.database;

import org.eclipse.jface.viewers.LabelProvider;

import ru.cg.runaex.components.bean.component.part.ColumnReference;

public class ColumnReferenceLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        ColumnReference ref = (ColumnReference) element;
        return ref.toString();
    }

}