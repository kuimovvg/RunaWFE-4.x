package ru.runa.bpm.ui.common.command;

import org.eclipse.gef.commands.Command;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.common.model.Variable;

public class ProcessDefinitionRemoveVariablesCommand extends Command {

    private Variable variable;

    private ProcessDefinition definition;

    @Override
    public void execute() {
        definition.removeVariable(variable);
    }

    @Override
    public void undo() {
        definition.addVariable(variable);
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public void setProcessDefinition(ProcessDefinition definition) {
        this.definition = definition;
    }

}
