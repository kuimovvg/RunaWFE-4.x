package ru.runa.gpd.lang.action;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.editor.gef.command.ActionDeleteCommand;
import ru.runa.gpd.editor.gef.command.NodeDeleteCommand;
import ru.runa.gpd.editor.gef.command.ProcessDefinitionRemoveSwimlaneCommand;
import ru.runa.gpd.editor.gef.command.TransitionDeleteCommand;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Transition;

// unused now, GEF native command used instead
public class DeleteGraphElementDelegate extends BaseModelActionDelegate {
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (getSelection() instanceof ProcessDefinition) {
            action.setEnabled(false);
        }
    }

    @Override
    public void run(IAction action) {
        GraphElement element = getSelection();
        Command command;
        if (element instanceof Node) {
            command = new NodeDeleteCommand();
            ((NodeDeleteCommand) command).setNode((Node) element);
        } else if (element instanceof Transition) {
            command = new TransitionDeleteCommand();
            ((TransitionDeleteCommand) command).setTransition((Transition) element);
        } else if (element instanceof Swimlane) {
            command = new ProcessDefinitionRemoveSwimlaneCommand();
            ((ProcessDefinitionRemoveSwimlaneCommand) command).setSwimlane((Swimlane) element);
            ((ProcessDefinitionRemoveSwimlaneCommand) command).setProcessDefinition(element.getProcessDefinition());
        } else if (element instanceof Action) {
            command = new ActionDeleteCommand();
            ((ActionDeleteCommand) command).setAction((Action) element);
        } else {
            throw new IllegalArgumentException("Unknown element " + element);
        }
        executeCommand(command);
    }
}
