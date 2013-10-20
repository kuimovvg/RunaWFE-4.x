package ru.cg.runaex.components_plugin.property_editor.database;

import org.eclipse.jface.viewers.LabelProvider;

import ru.cg.runaex.components.bean.component.part.TableReference;

public class TableReferenceLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        TableReference ref = (TableReference) element;
        return ref.toString();
    }

}