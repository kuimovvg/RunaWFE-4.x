package org.jbpm.ui.jpdl3.part.graph;

import org.jbpm.ui.common.part.graph.LabeledNodeGraphicalEditPart;
import org.jbpm.ui.jpdl3.model.SendMessageNode;

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
