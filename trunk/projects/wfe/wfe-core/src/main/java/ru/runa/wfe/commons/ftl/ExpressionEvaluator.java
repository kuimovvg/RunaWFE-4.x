package ru.runa.wfe.commons.ftl;

import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.calendar.BusinessCalendar;
import ru.runa.wfe.commons.calendar.impl.Duration;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Preconditions;

public class ExpressionEvaluator {
    private static final Pattern VARIABLE_REGEXP = Pattern.compile("\\$\\{(.*?[^\\\\])\\}");

    public static Date evaluateDuration(ExecutionContext executionContext, String expression) {
        Date baseDate = null;
        String durationString = null;

        if (expression.startsWith("#")) {
            String baseDateVariableName = expression.substring(2, expression.indexOf("}"));
            Object o = executionContext.getVariable(baseDateVariableName);
            baseDate = TypeConversionUtil.convertTo(o, Date.class);
            int endOfELIndex = expression.indexOf("}");
            if (endOfELIndex < (expression.length() - 1)) {
                String durationSeparator = expression.substring(endOfELIndex + 1).trim().substring(0, 1);
                if (!(durationSeparator.equals("+") || durationSeparator.equals("-"))) {
                    throw new InternalApplicationException("Invalid duedate, + or - missing after EL");
                }
                durationString = expression.substring(endOfELIndex + 1).trim();
            }
        } else {
            durationString = expression;
        }
        if (baseDate != null && (durationString == null || durationString.length() == 0)) {
            return baseDate;
        } else {
            Duration duration = new Duration(durationString);
            BusinessCalendar businessCalendar = ApplicationContextFactory.getBusinessCalendar();
            return businessCalendar.add((baseDate != null) ? baseDate : new Date(), duration);
        }
    }

    public static Object evaluateVariable(ExecutionContext executionContext, String expression) {
        if (expression.startsWith("${") && expression.endsWith("}")) {
            String variableName = expression.substring(2, expression.length() - 1);
            return executionContext.getVariable(variableName);
        }
        return expression;
    }

    public static Object evaluateVariable(IVariableProvider variableProvider, String expression) {
        if (expression.startsWith("${") && expression.endsWith("}")) {
            String variableName = expression.substring(2, expression.length() - 1);
            return variableProvider.getNotNull(variableName);
        }
        return expression;
    }

    public static String substitute(String value, Map<String, ? extends Object> variables) {
        Preconditions.checkNotNull(value, "invalid string to substitute");
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

    // TODO ref
    public static String substitute(String value, IVariableProvider variableProvider) {
        Preconditions.checkNotNull(value, "invalid string to substitute");
        Matcher matcher = VARIABLE_REGEXP.matcher(value);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object variable = variableProvider.getNotNull(variableName);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(variable.toString()));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

}
