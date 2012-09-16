package ru.runa.bpm.ui.jpdl3.part.graph;

import org.eclipse.ui.IActionFilter;
import ru.runa.bpm.ui.common.part.graph.LabeledNodeGraphicalEditPart;
import ru.runa.bpm.ui.jpdl3.model.ReceiveMessageNode;

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
        if ("ru.runa.bpm.ui.timerExists".equals(name)) {
            return value.equals(String.valueOf(getModel().timerExist()));
        }
        return false;
    }

}
