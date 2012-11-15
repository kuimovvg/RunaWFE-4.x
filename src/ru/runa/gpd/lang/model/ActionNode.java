package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.lang.NodeRegistry;

public class ActionNode extends DescribableNode implements Active {
    @Override
    public void postCreate() {
        ActionImpl nodeAction = NodeRegistry.getNodeTypeDefinition("action").createElement();
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
