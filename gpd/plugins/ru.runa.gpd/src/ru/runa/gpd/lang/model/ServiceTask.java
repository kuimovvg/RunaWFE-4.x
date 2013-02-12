package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

public class ServiceTask extends Node implements Active {
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
