package ru.runa.gpd.editor.gef.part.graph;

import ru.runa.gpd.lang.model.ReceiveMessageNode;

public class ReceiveMessageGraphicalEditPart extends LabeledNodeGraphicalEditPart {
    @Override
    public ReceiveMessageNode getModel() {
        return (ReceiveMessageNode) super.getModel();
    }

    @Override
    protected String getTooltipMessage() {
        return getModel().getName();
    }
}
