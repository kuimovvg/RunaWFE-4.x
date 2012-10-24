package org.jbpm.ui.common.command;

public class TransitionCreateBendpointCommand extends TransitionAbstractBendpointCommand {

    @Override
    public void execute() {
        transition.addBendpoint(index, bendpoint);
    }

    @Override
    public void undo() {
        transition.removeBendpoint(index);
    }

}
