package ru.runa.wfe.office.storage;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.VariableFormat;

public class ConditionProcessor {

    private static final String LIKE_EXPR_END = ".toLowerCase()) >= 0";

    private static final String LIKE_EXPR_START = ".toLowerCase().indexOf(";

    private static final String LIKE_LITERAL = "like";

    private static final String OR_EXPR = "||";

    private static final String OR_LITERAL = "OR";

    private static final String AND_EXPR = "&&";

    private static final String SPACE = " ";

    private static final String AND_LITERAL = "AND";

    private static final Log log = LogFactory.getLog(ConditionProcessor.class);

    private static ScriptEngine engine;
    static {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByName("JavaScript");
    }

    public static boolean filter(String condition, Map<String, Object> attributes, List<WfVariable> variables) {
        boolean result = false;
        try {
            String query = parse(condition, attributes, variables);
            result = (Boolean) engine.eval(query);
        } catch (Exception e) {
            log.error("", e);
        }
        return result;
    }

    private static String parse(String condition, Map<String, Object> attributes, List<WfVariable> variables) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(condition);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equalsIgnoreCase(AND_LITERAL)) {
                sb.append(SPACE);
                sb.append(AND_EXPR);
            } else if (token.equalsIgnoreCase(OR_LITERAL)) {
                sb.append(SPACE);
                sb.append(OR_EXPR);
            } else if (token.startsWith("[") && token.endsWith("]")) {
                sb.append(SPACE);
                sb = appendAttribute(sb, attributes, token);
            } else if (token.equalsIgnoreCase(LIKE_LITERAL)) {
                sb.append(LIKE_EXPR_START);
                token = st.nextToken();
                sb.append(token);
                sb.append(LIKE_EXPR_END);
            } else if (token.startsWith("@")) {
                String variableName = token.substring(1);
                for (WfVariable wfVariable : variables) {
                    VariableDefinition definition = wfVariable.getDefinition();
                    if (definition.getName().equals(variableName)) {
                        VariableFormat format = definition.getFormatNotNull();
                        String toAppend = "";
                        if (format instanceof LongFormat) {
                            toAppend = wfVariable.getStringValue();
                        } else if (format instanceof DateTimeFormat || format instanceof DateFormat) {
                            Date date = (Date) wfVariable.getValue();
                            toAppend = String.valueOf(date.getTime());
                        } else {
                            toAppend = "'" + wfVariable.getStringValue() + "'";
                        }
                        sb.append(SPACE);
                        sb.append(toAppend);
                    }
                }
            } else {
                sb.append(SPACE);
                sb.append(token);
            }
        }
        return sb.toString();
    }

    private static StringBuilder appendAttribute(StringBuilder sb, Map<String, Object> variables, String token) {
        String var = token.substring(1, token.length() - 1);
        if (variables.keySet().contains(var)) {
            String toAppend = "";
            Object obj = variables.get(var);
            if (obj instanceof String) {
                toAppend = "'" + obj + "'";
            } else if (obj instanceof Date) {
                toAppend = String.valueOf(((Date) obj).getTime());
            } else {
                toAppend = obj + "";
            }
            sb.append(toAppend);
        }
        return sb;
    }
}
