package ru.runa.wfe.service.exceptions;

public class PermissionDeniedException extends RuntimeException {

    private static final String MESSAGE = "Недостаточно прав для выполнения операции";
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public PermissionDeniedException() {
        super(MESSAGE);
    }

    public PermissionDeniedException(String message) {
        super(message);
    }

}
