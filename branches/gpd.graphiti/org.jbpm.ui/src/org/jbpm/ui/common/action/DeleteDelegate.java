package org.jbpm.ui.common.action;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IAction;
import org.jbpm.ui.common.command.ActionDeleteCommand;
import org.jbpm.ui.common.command.NodeDeleteCommand;
import org.jbpm.ui.common.command.ProcessDefinitionRemoveSwimlaneCommand;
import org.jbpm.ui.common.command.TransitionDeleteCommand;
import org.jbpm.ui.common.model.Action;
import org.jbpm.ui.common.model.GraphElement;
import org.jbpm.ui.common.model.Node;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.Swimlane;
import org.jbpm.ui.common.model.Transition;

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
