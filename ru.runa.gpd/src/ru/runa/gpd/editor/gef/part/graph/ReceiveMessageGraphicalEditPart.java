package ru.runa.gpd.editor.gef.part.graph;

import org.eclipse.ui.IActionFilter;

import ru.runa.gpd.lang.model.ReceiveMessageNode;

public class ReceiveMessageGraphicalEditPart extends LabeledNodeGraphicalEditPart implements IActionFilter {
    @Override
    public ReceiveMessageNode getModel() {
        return (ReceiveMessageNode) super.getModel();
    }

    @Override
    protected String getTooltipMessage() {
        return getModel().getName();
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if ("timerExists".equals(name)) {
            return value.equals(String.valueOf(getModel().timerExist()));
        }
        return false;
    }
}
