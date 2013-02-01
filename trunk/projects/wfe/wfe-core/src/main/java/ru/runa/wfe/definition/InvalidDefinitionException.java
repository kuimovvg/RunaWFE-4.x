package ru.runa.wfe.definition;

import ru.runa.wfe.ApplicationException;

/**
 * Thrown when trying to parse invalif process definition.
 * 
 * @author Dofs
 */
public class InvalidDefinitionException extends ApplicationException {
    private static final long serialVersionUID = 1L;
    private final String definitionName;

    public InvalidDefinitionException(String definitionName, String message, Throwable cause) {
        super(message, cause);
        this.definitionName = definitionName;
    }

    public InvalidDefinitionException(String definitionName, String message) {
        super(message);
        this.definitionName = definitionName;
    }

    public String getDefinitionName() {
        return definitionName;
    }

}
