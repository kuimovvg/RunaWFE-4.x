package ru.runa.gpd.lang.model;

import java.util.List;

public class StartState extends FormNode {
    @Override
    protected boolean allowArrivingTransition(Node source, List<Transition> transitions) {
        return false;
    }

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
