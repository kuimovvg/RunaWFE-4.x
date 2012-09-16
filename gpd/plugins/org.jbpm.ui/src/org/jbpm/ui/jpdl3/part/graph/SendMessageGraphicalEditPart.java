package ru.runa.bpm.ui.jpdl3.part.graph;

import ru.runa.bpm.ui.common.part.graph.LabeledNodeGraphicalEditPart;
import ru.runa.bpm.ui.jpdl3.model.SendMessageNode;

public class SendMessageGraphicalEditPart extends LabeledNodeGraphicalEditPart {

    @Override
    public SendMessageNode getModel() {
        return (SendMessageNode) super.getModel();
    }

    @Override
    protected String getTooltipMessage() {
        return getModel().getName();
    }

}
