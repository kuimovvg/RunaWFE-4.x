package ru.runa.alfresco.search;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;

import org.apache.commons.logging.LogFactory;

import ru.runa.alfresco.AlfObject;
import ru.runa.alfresco.WSObjectAccessor;
import ru.runa.wfe.commons.CalendarUtil;

/**
 * Operator for query condition.
 * 
 * @author dofs
 */
public enum Op {
    TYPE_OF("TYPE:\"$operand\""), EQUALS("$operand:\"$0\""), RANGE_INCLUSIVE("$operand:[$0 TO $1]"), LESS_THAN_INCLUSIVE("$operand:[MIN TO $0]"), GREATER_THAN_INCLUSIVE(
            "$operand:[$0 TO MAX]"), RANGE_EXCLUSIVE("$operand:{$0 TO $1}"), LESS_THAN_EXCLUSIVE("$operand:{MIN TO $0}"), GREATER_THAN_EXCLUSIVE(
            "$operand:{$0 TO MAX}"), IS_NULL("ISNULL:\"$operand\""), IS_NOT_NULL("ISNOTNULL:\"$operand\""), PRIMARYPARENT(
            "PRIMARYPARENT:\"$operand\"");

    private String regexp;

    private Op(String name) {
        this.regexp = name;
    }

    public String toExpression(N operand, Object[] params) {
        String result = regexp;
        result = result.replaceAll("\\$operand", Matcher.quoteReplacement(operand.toString()));
        for (int i = 0; i < params.length; i++) {
            String p;
            if (params[i] != null) {
                p = formatParam(params[i]);
            } else {
                p = "NULL";
                LogFactory.getLog(getClass()).warn("Null param [" + i + "] in " + this);
            }
            result = result.replaceAll("\\$" + i, Matcher.quoteReplacement(p));
        }
        return result;
    }

    private String formatParam(Object param) {
        if (param == null) {
            throw new NullPointerException(regexp);
        }
        if (param instanceof Calendar) {
            param = ((Calendar) param).getTime();
        }
        if (param instanceof Date) {
            return CalendarUtil.format((Date) param, WSObjectAccessor.ALF_DATE_FORMAT);
        }
        if (param instanceof AlfObject) {
            return ((AlfObject) param).getUuidRef();
        }
        return param.toString();
    }
}
