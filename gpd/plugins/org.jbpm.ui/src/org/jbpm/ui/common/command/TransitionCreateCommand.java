package ru.runa.bpm.ui.common.command;

import org.eclipse.gef.commands.Command;
import ru.runa.bpm.ui.JpdlVersionRegistry;
import ru.runa.bpm.ui.common.model.Node;
import ru.runa.bpm.ui.common.model.Transition;

public class TransitionCreateCommand extends Command {
    private Node source;

    private Node target;

    private Transition transition;

    @Override
    public boolean canExecute() {
        if (source == null || target == null) {
            return false;
        }
        return true;
    }

    private void createTransition() {
        String jpdlVersion = source.getProcessDefinition().getJpdlVersion();
        transition = JpdlVersionRegistry.getElementTypeDefinition(jpdlVersion, "transition").createElement();
        transition.setParent(source);
        transition.setName(source.getNextTransitionName());
        transition.setTarget(target);
    }

    @Override
    public void execute() {
        if (transition == null) {
            createTransition();
        }
        source.addLeavingTransition(transition);
    }

    @Override
    public void undo() {
        source.removeLeavingTransition(transition);
    }

    public void setSource(Node newSource) {
        source = newSource;
    }

    public Node getSource() {
        return source;
    }

    public void setTarget(Node newTarget) {
        target = newTarget;
    }
}
