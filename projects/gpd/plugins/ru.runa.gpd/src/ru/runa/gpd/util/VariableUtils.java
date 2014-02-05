package ru.runa.gpd.util;

import java.util.List;
import java.util.Map;

import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.lang.model.VariableUserType;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class VariableUtils {

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

    public static String generateNameForScripting(VariableContainer variableContainer, String variableName, Variable excludedVariable) {
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
        if (excludedVariable != null) {
            if (excludedVariable.getScriptingName() == null || Objects.equal(excludedVariable.getScriptingName(), scriptingName)) {
                return scriptingName;
            }
        }
        while (getVariableByScriptingName(variableContainer.getVariables(false, true), scriptingName) != null) {
            scriptingName += "_";
        }
        return scriptingName;
    }

    public static List<String> getVariableNamesForScripting(List<Variable> variables) {
        List<String> result = Lists.newArrayListWithExpectedSize(variables.size());
        for (Variable variable : variables) {
            // this is here due to strage NPE
            Preconditions.checkNotNull(variable.getScriptingName(), variable.getName());
            result.add(variable.getScriptingName());
        }
        return result;
    }

    public static List<String> getVariableNames(List<? extends Variable> variables) {
        List<String> result = Lists.newArrayList();
        for (Variable variable : variables) {
            result.add(variable.getName());
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

    /**
     * @return variable or <code>null</code>
     */
    public static Variable getVariableByName(VariableContainer variableContainer, String name) {
        List<Variable> variables = variableContainer.getVariables(false, true);
        for (Variable variable : variables) {
            if (Objects.equal(variable.getName(), name)) {
                return variable;
            }
        }
        if (name.contains(VariableUserType.DELIM)) {
            int index = name.indexOf(VariableUserType.DELIM);
            String complexVariableName = name.substring(0, index);
            Variable complexVariable = getVariableByName(variableContainer, complexVariableName);
            if (complexVariable == null) {
                return null;
            }
            String scriptingName = complexVariable.getScriptingName();
            String attributeName = name.substring(index + 1);
            while (attributeName.contains(VariableUserType.DELIM)) {
                index = attributeName.indexOf(VariableUserType.DELIM);
                complexVariableName = attributeName.substring(0, index);
                complexVariable = getVariableByName(complexVariable.getUserType(), complexVariableName);
                if (complexVariable == null) {
                    return null;
                }
                scriptingName += VariableUserType.DELIM + complexVariable.getScriptingName();
                attributeName = attributeName.substring(index + 1);
            }
            Variable attribute = getVariableByName(complexVariable.getUserType(), attributeName);
            if (attribute != null) {
                scriptingName += VariableUserType.DELIM + attribute.getScriptingName();
                return new Variable(name, scriptingName, attribute);
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
