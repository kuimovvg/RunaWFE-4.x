package ru.runa.gpd.util;

import java.util.List;
import java.util.Map;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class VariableUtils {
    public static boolean isVariableExists(List<Variable> variables, String variableName) {
        for (Variable variable : variables) {
            if (Objects.equal(variableName, variable.getName())) {
                return true;
            }
        }
        return false;
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

    public static Map<String, Variable> toMap(List<Variable> variables) {
        Map<String, Variable> result = Maps.newHashMapWithExpectedSize(variables.size());
        for (Variable variable : variables) {
            result.put(variable.getName(), variable);
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

    // TODO old stuff
    public static boolean isNameValid(String name) {
        char[] chars = name.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                if (!Character.isJavaIdentifierStart(chars[i])) {
                    return false;
                }
            } else {
                if (!Character.isJavaIdentifierPart(chars[i])) {
                    return false;
                }
            }
            if ('$' == chars[i]) {
                return false;
            }
        }
        return true;
    }

    public static String generateNameForScripting(ProcessDefinition processDefinition, String variableName, Variable excludedVariable) {
        char[] chars = variableName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                if (!Character.isJavaIdentifierStart(chars[i])) {
                    chars[i] = '_';
                }
            } else {
                if (!Character.isJavaIdentifierPart(chars[i])) {
                    chars[i] = '_';
                }
            }
            if ('$' == chars[i]) {
                chars[i] = '_';
            }
        }
        String scriptingName = new String(chars);
        if (Objects.equal(variableName, scriptingName)) {
            return scriptingName;
        }
        if (excludedVariable != null && Objects.equal(excludedVariable.getName(), scriptingName)) {
            return scriptingName;
        }
        while (processDefinition.getVariable(scriptingName, true) != null) {
            scriptingName += "_";
        }
        return scriptingName;
    }

    public static List<String> getVariableNamesForScripting(List<Variable> variables) {
        List<String> result = Lists.newArrayList();
        for (Variable variable : variables) {
            result.add(variable.getScriptingName());
        }
        return result;
    }

    /**
     * @return variable or <code>null</code>
     */
    public static Variable getVariableByScriptingName(List<Variable> variables, String name) {
        for (Variable variable : variables) {
            if (Objects.equal(variable.getScriptingName(), name)) {
                return variable;
            }
        }
        return null;
    }

    public static String wrapVariableName(String variableName) {
        return "${" + variableName + "}";
    }

    public static boolean isVariableNameWrapped(String value) {
        return value.length() > 3 && "${".equals(value.substring(0, 2)) && value.endsWith("}");
    }

    public static String unwrapVariableName(String value) {
        if (value.length() > 3) {
            return value.substring(2, value.length() - 1);
        }
        return "";
    }
}
