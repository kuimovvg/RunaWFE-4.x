package org.jbpm.ui.common.command;

import org.eclipse.gef.commands.Command;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.Swimlane;

public class ProcessDefinitionRemoveSwimlaneCommand extends Command {
    private ProcessDefinition definition;

    private Swimlane swimlane;

    public void setProcessDefinition(ProcessDefinition definition) {
        this.definition = definition;
    }

    public void setSwimlane(Swimlane swimlane) {
        this.swimlane = swimlane;
    }

    @Override
    public void execute() {
        definition.removeSwimlane(swimlane);
    }

    @Override
    public void undo() {
        definition.addSwimlane(swimlane);
    }
}
