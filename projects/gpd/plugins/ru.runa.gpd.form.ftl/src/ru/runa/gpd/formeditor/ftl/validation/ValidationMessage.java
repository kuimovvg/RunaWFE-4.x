package ru.runa.gpd.formeditor.ftl.validation;

public class ValidationMessage {
	public final Severity severity;
	public final String message;

	private ValidationMessage(Severity severity, String message) {
		this.severity = severity;
		this.message = message;
	}

	public static ValidationMessage warning(String message) {
		return new ValidationMessage(Severity.WARNING, message);
	}

	public static ValidationMessage error(String message) {
		return new ValidationMessage(Severity.ERROR, message);
	}

	public enum Severity {
		ERROR, 
		WARNING
	}
}
