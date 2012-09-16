package ru.runa.bpm.ui.common.model;

import java.util.List;

public class StartState extends FormNode {

    @Override
    protected boolean allowArrivingTransition(Node source, List<Transition> transitions) {
        return false;
    }

    @Override
    protected boolean allowLeavingTransition(Node target, List<Transition> transitions) {
        return super.allowLeavingTransition(target, transitions) && transitions.size() == 0;
    }

}
