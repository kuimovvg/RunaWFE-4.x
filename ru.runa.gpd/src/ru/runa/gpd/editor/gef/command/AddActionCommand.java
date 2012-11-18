package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.Active;
import ru.runa.gpd.lang.model.GraphElement;

public class AddActionCommand extends Command {
    private Active target;
    private Action action;
    private int actionIndex = -1;

    @Override
    public void execute() {
        action = NodeRegistry.getNodeTypeDefinition(Action.class).createElement((GraphElement) target);
        action.setDelegationClassName("");
        target.addAction(action, actionIndex);
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
}
