package org.jbpm.ui.jpdl3.part.graph;

import org.eclipse.ui.IActionFilter;
import org.jbpm.ui.common.part.graph.LabeledNodeGraphicalEditPart;
import org.jbpm.ui.jpdl3.model.ReceiveMessageNode;

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
        if ("org.jbpm.ui.timerExists".equals(name)) {
            return value.equals(String.valueOf(getModel().timerExist()));
        }
        return false;
    }

}
