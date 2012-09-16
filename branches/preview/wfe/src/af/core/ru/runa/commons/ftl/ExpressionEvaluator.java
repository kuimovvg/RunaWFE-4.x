package ru.runa.commons.ftl;

import java.util.Date;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.calendar.BusinessCalendar;
import ru.runa.bpm.calendar.impl.Duration;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.commons.ApplicationContextFactory;
import ru.runa.commons.TypeConversionUtil;

public class ExpressionEvaluator {

    public static Date evaluateDuration(String expression, ExecutionContext executionContext) {
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

}
