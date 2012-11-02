package ru.runa.wfe.var.dao;

import ru.runa.wfe.execution.Process;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.Variable;

public class DAOVariableProvider extends AbstractVariableProvider {
    private final VariableDAO variableDAO;
    private final Process process;

    public DAOVariableProvider(VariableDAO variableDAO, Process process) {
        this.variableDAO = variableDAO;
        this.process = process;
    }

    @Override
    public Object get(String variableName) {
        Variable<?> variable = variableDAO.getVariable(process, variableName);
        if (variable != null) {
            return variable.getValue();
        }
        return null;
    }

}
