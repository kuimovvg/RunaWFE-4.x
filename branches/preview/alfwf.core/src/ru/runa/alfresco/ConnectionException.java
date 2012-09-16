package ru.runa.alfresco;

import ru.runa.ApplicationException;

/**
 * Connection refused exception.
 * @author dofs
 */
public class ConnectionException extends ApplicationException {
    public static final String MESSAGE = "Error starting session.";

    public ConnectionException() {
        super(MESSAGE);
    }

}
