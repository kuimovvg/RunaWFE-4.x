package ru.runa.gpd.swimlane;

import java.util.List;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;

public abstract class SwimlaneInitializer {
    public static final String LEFT_BRACKET = "(";
    public static final String RIGHT_BRACKET = ")";

    public abstract void validate(Swimlane swimlane, List<ValidationError> errors);

    public abstract boolean hasReference(Variable variable);

    public abstract void onVariableRename(String variableName, String newVariableName);
}
