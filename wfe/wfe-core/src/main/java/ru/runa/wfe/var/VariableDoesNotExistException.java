package ru.runa.wfe.var;

import ru.runa.wfe.WfException;

/**
 * Thrown when process variable does not defined in process.
 * 
 * @author Dofs
 * @since 4.0
 */
public class VariableDoesNotExistException extends WfException {
    private static final long serialVersionUID = 1L;

    public VariableDoesNotExistException(String variableName) {
        super(variableName);
    }

}
