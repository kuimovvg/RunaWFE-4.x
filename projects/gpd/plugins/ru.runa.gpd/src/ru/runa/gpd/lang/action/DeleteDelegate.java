package ru.runa.gpd.lang.action;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IAction;

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

public class DeleteDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        Command command = createDeleteCommand((GraphElement) selectedPart.getModel());
        executeCommand(command);
    }

    private Command createDeleteCommand(GraphElement element) {
        if (element instanceof Node) {
            NodeDeleteCommand result = new NodeDeleteCommand();
            result.setNode((Node) element);
            return result;
        } else if (element instanceof Transition) {
            TransitionDeleteCommand result = new TransitionDeleteCommand();
            result.setTransition((Transition) element);
            return result;
        } else if (element instanceof Swimlane) {
            ProcessDefinitionRemoveSwimlaneCommand result = new ProcessDefinitionRemoveSwimlaneCommand();
            result.setSwimlane((Swimlane) element);
            result.setProcessDefinition((ProcessDefinition) selectedPart.getParent().getModel());
            return result;
        } else if (element instanceof Action) {
            ActionDeleteCommand command = new ActionDeleteCommand();
            command.setAction((Action) element);
            return command;
        } else {
            throw new IllegalArgumentException("Unknown element " + element);
        }
    }
}
