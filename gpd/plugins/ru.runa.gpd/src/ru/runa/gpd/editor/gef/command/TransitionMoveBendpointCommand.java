package ru.runa.gpd.editor.gef.command;

import ru.runa.gpd.lang.model.Bendpoint;

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
