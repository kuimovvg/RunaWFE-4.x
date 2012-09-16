package ru.runa.bpm.ui.common.command;

import org.eclipse.gef.commands.Command;
import ru.runa.bpm.ui.JpdlVersionRegistry;
import ru.runa.bpm.ui.common.model.Action;
import ru.runa.bpm.ui.common.model.Active;
import ru.runa.bpm.ui.common.model.GraphElement;

public class AddActionCommand extends Command {
    private Active target;
    private Action action;
    private int actionIndex = -1;

    @Override
    public void execute() {
        if (action == null) {
            action = createAction();
        }
        target.addAction(action , actionIndex);
    }

    @Override
    public void undo() {
        target.removeAction(action);
    }

    public void setTarget(Active newTarget) {
        target = newTarget;
    }

    public Action getAction() {
        return action;
    }

    public void setActionIndex(int actionIndex) {
        this.actionIndex = actionIndex;
    }

    private Action createAction() {
        String jpdlVersion = ((GraphElement) target).getProcessDefinition().getJpdlVersion();
        Action action = JpdlVersionRegistry.getElementTypeDefinition(jpdlVersion, "action").createElement();
        action.setDelegationClassName("");
        return action;
    }

}
