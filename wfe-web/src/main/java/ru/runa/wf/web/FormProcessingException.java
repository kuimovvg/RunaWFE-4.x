package ru.runa.wf.web;

import ru.runa.wfe.ApplicationException;

/**
 * Thrown when system failed to build task form.
 * 
 * @author Dofs
 * @since 3.0
 */
public class FormProcessingException extends ApplicationException {
    private static final long serialVersionUID = 1L;

    public FormProcessingException(Throwable cause) {
        super(cause);
    }

}
