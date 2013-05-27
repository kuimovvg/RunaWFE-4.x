package ru.runa.gpd.util;

import java.util.List;

import com.google.common.collect.Lists;

import ru.runa.gpd.lang.model.Variable;

public class BackCompatibilityUtils {
 // remove if way of importing wfe packages in downstream plugins will be found
    public static String getClassName(java.lang.String className) {
        return ru.runa.wfe.commons.BackCompatibilityClassNames.getClassName(className);
    }
    
    /**
     * Filtering by whitespace, etc...
     */
    public static List<Variable> getValidVariables(List<Variable> variables) {
        List<Variable> result = Lists.newArrayList(variables);
        for (Variable variable : variables) {
            if (variable.getName().indexOf(" ") != -1) {
                result.remove(variable);
            }
        }
        return result;
    }

    /**
     * Filtering by whitespace, etc...
     */
    public static List<String> getValidVariableNames(List<String> variableNames) {
        List<String> result = Lists.newArrayList(variableNames);
        for (String variableName : variableNames) {
            if (variableName.indexOf(" ") != -1) {
                result.remove(variableName);
            }
        }
        return result;
    }

}
