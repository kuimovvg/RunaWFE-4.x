package ru.runa.bpm.ui.common.command;

import org.eclipse.gef.commands.Command;
import ru.runa.bpm.ui.common.model.Node;
import ru.runa.bpm.ui.common.model.Transition;

public class TransitionDeleteCommand extends Command {
    private Transition transition;

    private Node source;

    public void setTransition(Transition transition) {
        this.transition = transition;
    }

    @Override
    public boolean canExecute() {
        return transition != null;
    }

    @Override
    public void execute() {
        source = transition.getSource();
        source.removeLeavingTransition(transition);
    }

    @Override
    public void undo() {
        source.addLeavingTransition(transition);
    }
}
