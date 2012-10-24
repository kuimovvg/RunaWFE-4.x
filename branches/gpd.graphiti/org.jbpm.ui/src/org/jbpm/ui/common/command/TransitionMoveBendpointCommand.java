package org.jbpm.ui.common.command;

import org.jbpm.ui.common.model.Bendpoint;

public class TransitionMoveBendpointCommand extends TransitionAbstractBendpointCommand {

    private Bendpoint oldBendpoint;

    @Override
    public void execute() {
        oldBendpoint = transition.getBendpoints().get(index);
        transition.setBendpoint(index, bendpoint);
    }

    @Override
    public void undo() {
        transition.setBendpoint(index, oldBendpoint);
    }

}
