package ru.runa.gpd.util;

import java.util.List;

import com.google.common.base.Objects;

import ru.runa.gpd.lang.model.Variable;

public class CommonUtils {

    public static boolean isVariableExists(List<Variable> variables, String variableName) {
        for (Variable variable : variables) {
            if (Objects.equal(variableName, variable.getName())) {
                return true;
            }
        }
        return false;
    }
}
