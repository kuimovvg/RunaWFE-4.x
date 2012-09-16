package ru.runa.bpm.ui.common.command;

import ru.runa.bpm.ui.common.model.Bendpoint;

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
