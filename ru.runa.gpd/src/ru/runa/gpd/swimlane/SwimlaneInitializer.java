package ru.runa.gpd.swimlane;

import java.util.List;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;

public abstract class SwimlaneInitializer {
    public static final String LEFT_BRACKET = "(";
    public static final String RIGHT_BRACKET = ")";

    public abstract List<String> getErrors(ProcessDefinition processDefinition);

    public abstract boolean hasReference(Variable variable);

    public abstract void onVariableRename(String variableName, String newVariableName);
}
