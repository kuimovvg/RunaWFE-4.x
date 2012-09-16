package ru.runa.bpm.ui.jpdl3.model;

import java.util.List;

import ru.runa.bpm.ui.common.model.Node;
import ru.runa.bpm.ui.common.model.Transition;

public class StartState extends ru.runa.bpm.ui.common.model.StartState {

    @Override
    protected boolean allowLeavingTransition(Node target, List<Transition> transitions) {
        /* allow duplicated transitions to attach different action handlers
        for (Transition transition : transitions) {
            if (transition.getTarget().equals(target)) {
                return false;
            }
        }
        */
        return true;
    }

    @Override
    protected void validate() {
        super.validate();
        /*if (!hasForm() && getLeavingTransitions().size() > 1) {
            addError("startState3.noFormAndMultipleOutTransitions");
        }*/
    }
}
