package ru.runa.bpm.ui.common.part.tree;

import java.util.List;

import ru.runa.bpm.ui.common.model.Action;
import ru.runa.bpm.ui.common.model.Transition;

public class TransitionTreeEditPart extends ElementTreeEditPart {

    public Transition getTransition() {
        return (Transition) getModel();
    }

    @Override
    protected List<Action> getModelChildren() {
        return getTransition().getChildren(Action.class);
    }

}
