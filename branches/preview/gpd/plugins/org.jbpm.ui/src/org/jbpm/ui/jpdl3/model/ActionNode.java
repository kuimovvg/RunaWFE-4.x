package ru.runa.bpm.ui.jpdl3.model;

import java.util.ArrayList;
import java.util.List;

import ru.runa.bpm.ui.JpdlVersionRegistry;
import ru.runa.bpm.ui.common.model.Action;
import ru.runa.bpm.ui.common.model.Active;
import ru.runa.bpm.ui.common.model.DescribableNode;

public class ActionNode extends DescribableNode implements Active {

    @Override
    public void postCreate() {
        String jpdlVersion = getProcessDefinition().getJpdlVersion();
        ActionImpl nodeAction = JpdlVersionRegistry.getElementTypeDefinition(jpdlVersion, "action").createElement();
        addChild(nodeAction);
        nodeAction.setEventType(Event.NODE_ACTION);
    }
    
    @Override
    protected void validate() {
        super.validate();
        int nodeActionsCount = getNodeActions().size();
        if (nodeActionsCount == 0) {
            addError("nodeAction.required");
        }
        if (nodeActionsCount > 1) {
            addError("nodeAction.single", nodeActionsCount);
        }
    }
    
    public List<Action> getNodeActions() {
        List<Action> result = new ArrayList<Action>();
        for (ActionImpl action : getChildren(ActionImpl.class)) {
            if (Event.NODE_ACTION.equals(action.getEventType())) {
                result.add(action);
            }
        }
        return result;
    }
}
