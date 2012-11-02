package ru.runa.wfe.definition;

import ru.runa.wfe.ApplicationException;

/**
 * Thrown when trying to parse invalif process definition.
 * 
 * @author Dofs
 */
public class InvalidDefinitionException extends ApplicationException {
    private static final long serialVersionUID = 1L;

    public InvalidDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDefinitionException(String message) {
        super(message);
    }

    public InvalidDefinitionException(Throwable cause) {
        super(cause);
    }

}
