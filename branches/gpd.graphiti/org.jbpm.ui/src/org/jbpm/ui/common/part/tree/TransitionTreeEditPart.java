package org.jbpm.ui.common.part.tree;

import java.util.List;

import org.jbpm.ui.common.model.Action;
import org.jbpm.ui.common.model.Transition;

public class TransitionTreeEditPart extends ElementTreeEditPart {

    public Transition getTransition() {
        return (Transition) getModel();
    }

    @Override
    protected List<Action> getModelChildren() {
        return getTransition().getChildren(Action.class);
    }

}
