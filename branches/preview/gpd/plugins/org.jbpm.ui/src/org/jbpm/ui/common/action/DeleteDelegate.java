package ru.runa.bpm.ui.common.action;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IAction;
import ru.runa.bpm.ui.common.command.ActionDeleteCommand;
import ru.runa.bpm.ui.common.command.NodeDeleteCommand;
import ru.runa.bpm.ui.common.command.ProcessDefinitionRemoveSwimlaneCommand;
import ru.runa.bpm.ui.common.command.TransitionDeleteCommand;
import ru.runa.bpm.ui.common.model.Action;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.Node;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.common.model.Swimlane;
import ru.runa.bpm.ui.common.model.Transition;

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
