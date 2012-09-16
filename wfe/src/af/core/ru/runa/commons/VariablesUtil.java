package ru.runa.commons;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariablesUtil {
    private static final Pattern VARIABLE_REGEXP = Pattern.compile("\\$\\{(.*?[^\\\\])\\}");

    public static String substitute(String value, Map<String, Object> variables) {
        Matcher matcher = VARIABLE_REGEXP.matcher(value);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object variable = variables.get(variableName);
            if (variable == null) {
                throw new NullPointerException("Variable '" + variableName + "' is not defined");
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(variable.toString()));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

}
