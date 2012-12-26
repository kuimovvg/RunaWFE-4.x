package ru.runa.alfresco;

import ru.runa.wfe.ApplicationException;

/**
 * Connection refused exception.
 * 
 * @author dofs
 */
public class ConnectionException extends ApplicationException {
    private static final long serialVersionUID = 1L;
    public static final String MESSAGE = "Error starting session.";

    public ConnectionException() {
        super(MESSAGE);
    }

}
