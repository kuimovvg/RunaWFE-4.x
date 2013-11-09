package ru.runa.gpd.lang;

import org.eclipse.core.resources.IMarker;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.GraphElement;

public class ValidationError {
    private final String message;
    private final GraphElement source;
    private final int severity;
    
    private ValidationError(GraphElement source, String message, int severity) {
        this.message = message;
        this.source = source;
        this.severity = severity;
    }
    
    public static ValidationError createError(GraphElement source, String message) {
        return new ValidationError(source, message, IMarker.SEVERITY_ERROR);
    }

    public static ValidationError createWarning(GraphElement source, String message) {
        return new ValidationError(source, message, IMarker.SEVERITY_WARNING);
    }
    
    private static String getLocalizedMessage(String messageKey, Object... params) {
        return Localization.getString("model.validation." + messageKey, params);
    }

    public static ValidationError createLocalizedError(GraphElement source, String messageKey, Object... params) {
        String message = getLocalizedMessage(messageKey, params);
        return createError(source, message);
    }

    public static ValidationError createLocalizedWarning(GraphElement source, String messageKey, Object... params) {
        String message = getLocalizedMessage(messageKey, params);
        return createWarning(source, message);
    }

    public String getMessage() {
        return message;
    }
    
    public int getSeverity() {
        return severity;
    }
    
    public GraphElement getSource() {
        return source;
    }
}
