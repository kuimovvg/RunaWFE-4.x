package ru.runa.gpd.editor.gef.part.graph;

import org.eclipse.ui.IActionFilter;

import ru.runa.gpd.lang.model.FormNode;

public class FormNodeEditPart extends SwimlaneNodeEditPart implements IActionFilter {
    @Override
    public FormNode getModel() {
        return (FormNode) super.getModel();
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if ("formExists".equals(name)) {
            return value.equals(String.valueOf(getModel().hasForm()));
        }
        return false;
    }
}
