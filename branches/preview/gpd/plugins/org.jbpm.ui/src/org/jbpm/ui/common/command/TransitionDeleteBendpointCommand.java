package ru.runa.bpm.ui.common.command;

public class TransitionDeleteBendpointCommand extends TransitionAbstractBendpointCommand {

    @Override
    public void execute() {
        transition.removeBendpoint(index);
    }

    @Override
    public void undo() {
        transition.addBendpoint(index, bendpoint);
    }

}
