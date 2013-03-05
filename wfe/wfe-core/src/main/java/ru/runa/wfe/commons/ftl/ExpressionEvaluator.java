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
import com.google.common.base.Strings;

public class ExpressionEvaluator {
    private static final Pattern VARIABLE_REGEXP = Pattern.compile("\\$\\{(.*?[^\\\\])\\}");

    public static Date evaluateDuration(ExecutionContext executionContext, String expression) {
        Date baseDate = null;
        String durationString = null;

        if (expression != null && expression.startsWith("#")) {
            String baseDateVariableName = expression.substring(2, expression.indexOf("}"));
            Object o = executionContext.getVariable(baseDateVariableName);
            baseDate = TypeConversionUtil.convertTo(Date.class, o);
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
        if (baseDate != null && Strings.isNullOrEmpty(durationString)) {
            return baseDate;
        } else if (durationString != null) {
            Duration duration = new Duration(durationString);
            BusinessCalendar businessCalendar = ApplicationContextFactory.getBusinessCalendar();
            return businessCalendar.add((baseDate != null) ? baseDate : new Date(), duration);
        } else {
            return new Date();
        }
    }

    public static Object evaluateVariableNotNull(IVariableProvider variableProvider, String expression) {
        Preconditions.checkNotNull(expression);
        if (expression.startsWith("${") && expression.endsWith("}")) {
            String variableName = expression.substring(2, expression.length() - 1);
            return variableProvider.getValueNotNull(variableName);
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

    public static String substitute(String value, IVariableProvider variableProvider) {
        Preconditions.checkNotNull(value, "invalid string to substitute");
        Matcher matcher = VARIABLE_REGEXP.matcher(value);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object variable = variableProvider.getValueNotNull(variableName);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(variable.toString()));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

}
