package org.jbpm.ui.jpdl3.model;

import java.util.List;

import org.jbpm.ui.common.model.Node;
import org.jbpm.ui.common.model.Transition;

public class StartState extends org.jbpm.ui.common.model.StartState {

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
