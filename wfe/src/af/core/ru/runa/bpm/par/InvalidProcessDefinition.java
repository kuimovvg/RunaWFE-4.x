package ru.runa.bpm.par;

public class InvalidProcessDefinition extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidProcessDefinition(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public InvalidProcessDefinition(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

}
