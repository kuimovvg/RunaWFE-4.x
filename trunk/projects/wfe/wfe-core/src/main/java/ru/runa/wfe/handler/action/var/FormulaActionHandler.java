/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.handler.action.var;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.handler.action.ActionHandler;
import ru.runa.wfe.var.FileVariable;

public class FormulaActionHandler implements ActionHandler {
    private static final Log log = LogFactory.getLog(FormulaActionHandler.class);

    private ExecutionContext context;
    private final FormulaActionHandlerOperations actions = new FormulaActionHandlerOperations();
    private String inputData = null;
    private char[] formula = null;
    private int nowPosition = 0;
    private final String oneSymbolTokens = "=()+-*/!<>&|^'\",\n;";
    private final String[] operations = { "&|^", // priority 0
            "<!=>", // priority 1
            "+-", // priority 2
            "*/" // priority 3
    };
    private boolean stringVariableToken = false;
    private boolean quo = false;
    private String nextToken = null;

    private String nextStringToken(char limitingSymbol) { // return string,
                                                          // limited by
                                                          // 'limitingSymbol'
        if (formula[nowPosition] != limitingSymbol) {
            return null;
        }
        nowPosition++;
        String answer = "";
        boolean escapeCharacter = false;
        while (nowPosition < formula.length) {
            if (escapeCharacter) {
                escapeCharacter = false;
                answer += formula[nowPosition];
            } else {
                if (formula[nowPosition] == '\\') {
                    escapeCharacter = true;
                } else {
                    if (formula[nowPosition] == limitingSymbol) {
                        break;
                    } else {
                        answer += formula[nowPosition];
                    }
                }
            }
            nowPosition++;
        }
        if (nowPosition == formula.length) {
            return null;
        }
        nowPosition++;
        return answer;
    }

    // static String tststr = "";
    // private String nextToken() {
    // String r = nextToken2();
    // tststr += " [" + r + "]";
    // return r;
    // }

    private String nextToken() {
        quo = false;
        if (nextToken != null) {
            String ans = nextToken;
            nextToken = null;
            return ans;
        }
        if (stringVariableToken) {
            stringVariableToken = false;
            return nextStringToken('"');
        }
        while (nowPosition < formula.length && formula[nowPosition] == ' ') {
            nowPosition++;
        }
        if (nowPosition == formula.length) {
            return null;
        }
        if (formula[nowPosition] == '"') {
            stringVariableToken = true;
            return ":";
        }
        if (formula[nowPosition] == '\'') {
            quo = true;
            return nextStringToken('\'');
        }
        if (oneSymbolTokens.contains("" + formula[nowPosition])) {
            nowPosition++;
            return "" + formula[nowPosition - 1];
        }
        String answer = "";
        while (nowPosition < formula.length && formula[nowPosition] != ' ') {
            if (oneSymbolTokens.contains("" + formula[nowPosition])) {
                break;
            }
            answer += formula[nowPosition++];
        }
        return answer;
    }

    String idsuf = null;

    @Override
    public void execute(ExecutionContext context) {
        this.context = context;
        idsuf = " (process id = " + context.getProcess().getId() + ")";
        if (inputData == null) {
            log.error("Configuration not found in " + idsuf);
            return;
        } // log.info("***  " + inputData);
        formula = inputData.toCharArray();
        nowPosition = 0;
        stringVariableToken = false;
        nextToken = null;
        String nf = "";
        String s;// tststr = "";
        while ((s = nextToken()) != null) {
            if (!quo && (s.equals(";") || s.equals("\n"))) {
                if (nf.length() > 0) {
                    // log.info("_ " + tststr); tststr = "";
                    formula = nf.toCharArray();
                    int ip = nowPosition;
                    String nt = nextToken;
                    boolean b = stringVariableToken;
                    parseFormula(); // tststr = "";
                    nowPosition = ip;
                    stringVariableToken = b;
                    nextToken = nt;
                    formula = inputData.toCharArray();
                    nf = "";
                }
            } else {
                if (stringVariableToken) {
                    // String nt = nextToken();
                    // log.info("nt = [" + nt + "]");
                    // nt = nt.replaceAll(""+'"', ";");
                    // nt = nt.replaceAll(";", "\\\"");
                    // log.info("rt = [" + nt + "]");
                    // nf += '"' + nt + '"';
                    nf += '"' + nextToken().replaceAll("\"", "\\\\\"") + '"';
                } else {
                    s = s.replaceAll("'", "\\\\'");
                    boolean contains = false;
                    for (char c : (oneSymbolTokens + " ").toCharArray()) {
                        contains |= s.contains("" + c);
                    }
                    if ((s.length() > 1 && contains) || quo) {
                        nf += '\'' + s + '\'';
                    } else {
                        nf += s;
                    }
                }
            }
        }
        if (nf.length() > 0) {
            // log.info("> " + tststr); tststr = "";
            formula = nf.toCharArray();
            parseFormula();
        }

        /*
         * StringTokenizer st = new StringTokenizer(inputData, ";\n"); while
         * (st.hasMoreTokens()) { formula = st.nextToken().toCharArray(); if
         * (formula.length>0) parseFormula(); }
         */
    }

    private String errorMessage = null;

    private void parseFormula() {
        nowPosition = 0;
        errorMessage = null;
        String newVariable = nextToken();
        if (stringVariableToken) {
            error("Incorrect variable name: use ' instead \"");
            return;
        }
        if (newVariable == null) {
            error("Variable name expected");
            return;
        }
        if (newVariable.length() == 1 && oneSymbolTokens.contains(newVariable)) {
            error("Incorrect variable name: " + newVariable);
            return;
        }
        String equal = nextToken();
        if (equal == null || !equal.equals("=")) {
            error("'=' expected");
            return;
        }
        Object value = parsePriority0();
        if (value == null) {
            if (errorMessage == null) {
                errorMessage = "Error at position " + nowPosition;
            }
            error(errorMessage);
            return;
        }
        Object lastValue = context.getVariable(newVariable);
        if (lastValue != null && value.getClass() != lastValue.getClass()) {
            value = actions.translate(value, lastValue.getClass());
        }
        if (value == null) {
            error("Type mismatch");
            return;
        }
        if (FileVariable.class.isInstance(value)) {
            FileVariable file = (FileVariable) value;
            value = new FileVariable(file.getName(), file.getData().clone(), file.getContentType());
        }
        context.setVariable(newVariable, value);
    }

    private Object parsePriority0() {
        Object answer = parsePriority1();
        while (true) {
            if (answer == null) {
                return null;
            }
            String s = nextToken();
            if (s == null) {
                return answer;
            }
            if (s.equals(")") || s.equals(",")) {
                nowPosition--;
                return answer;
            }
            if (s.equals("&")) {
                Object operand = parsePriority1();
                answer = actions.and(answer, operand);
                continue;
            }
            if (s.equals("|")) {
                Object operand = parsePriority1();
                answer = actions.or(answer, operand);
                continue;
            }
            if (s.equals("^")) {
                Object operand = parsePriority1();
                answer = actions.xor(answer, operand);
                continue;
            }
            errorMessage = "Operator expected, but '" + s + "' found at position " + nowPosition;
            return null;
        }
    }

    private Object parsePriority1() {
        Object o1 = parsePriority2();
        if (o1 == null) {
            return null;
        }
        String s = nextToken();
        if (s == null) {
            return o1;
        }
        if (s.equals(")") || s.equals(",") || operations[0].contains(s)) {
            nowPosition--;
            return o1;
        }
        if (s.equals("<")) {
            if (nowPosition < formula.length && formula[nowPosition] == '=') {
                nowPosition++;
                Object o2 = parsePriority2();
                if (o2 == null) {
                    return null;
                }
                return actions.lessOrEqual(o1, o2);
            } else {
                Object o2 = parsePriority2();
                if (o2 == null) {
                    return null;
                }
                return actions.less(o1, o2);
            }
        }
        if (s.equals(">")) {
            if (nowPosition < formula.length && formula[nowPosition] == '=') {
                nowPosition++;
                Object o2 = parsePriority2();
                if (o2 == null) {
                    return null;
                }
                return actions.biggerOrEqual(o1, o2);
            } else {
                Object o2 = parsePriority2();
                if (o2 == null) {
                    return null;
                }
                return actions.bigger(o1, o2);
            }
        }
        if (s.equals("=")) {
            if (nowPosition < formula.length && formula[nowPosition] == '=') {
                nowPosition++;
                Object o2 = parsePriority2();
                if (o2 == null) {
                    return null;
                }
                return actions.equal(o1, o2);
            }
        }
        if (s.equals("!")) {
            if (nowPosition < formula.length && formula[nowPosition] == '=') {
                nowPosition++;
                Object o2 = parsePriority2();
                if (o2 == null) {
                    return null;
                }
                return actions.not(actions.equal(o1, o2));
            }
        }
        errorMessage = "Operator expected, but '" + s + "' found at position " + nowPosition;
        return null;
    }

    private Object parsePriority2() {
        Object answer = parsePriority3();
        while (true) {
            if (answer == null) {
                return null;
            }
            String s = nextToken();
            if (s == null) {
                return answer;
            }
            if (s.equals(")") || s.equals(",") || operations[0].contains(s) || operations[1].contains(s)) {
                nowPosition--;
                return answer;
            }
            if (s.equals("+")) {
                Object operand = parsePriority3();
                answer = actions.sum(answer, operand);
                continue;
            }
            if (s.equals("-")) {
                Object operand = parsePriority3();
                answer = actions.sub(answer, operand);
                continue;
            }
            errorMessage = "Operator expected, but '" + s + "' found at position " + nowPosition;
            return null;
        }
    }

    private Object parsePriority3() {
        Object answer = parseSimple();
        while (true) {
            if (answer == null) {
                return null;
            }
            String s = nextToken();
            if (s == null) {
                return answer;
            }
            if (s.equals(")") || s.equals(",") || operations[0].contains(s) || operations[1].contains(s) || operations[2].contains(s)) {
                nowPosition--;
                return answer;
            }
            if (s.equals("*")) {
                Object operand = parseSimple();
                answer = actions.mul(answer, operand);
                continue;
            }
            if (s.equals("/")) {
                Object operand = parseSimple();
                answer = actions.div(answer, operand);
                continue;
            }
            errorMessage = "Operator expected, but '" + s + "' found at position " + nowPosition;
            return null;
        }
    }

    private Object parseSimple() {
        String s = nextToken();
        if (s == null) {
            errorMessage = "Incorrect token at position " + nowPosition;
            return null;
        }
        if (s.equals("-")) {
            return actions.changeSign(parseSimple());
        }
        if (s.equals("!")) {
            return actions.not(parseSimple());
        }
        if (s.equals("(")) {
            Object answer = parsePriority0();
            nextToken = nextToken();
            if (nextToken == null || !nextToken.equals(")")) {
                errorMessage = "')' expected at position " + nowPosition;
                return null;
            }
            nextToken = null;
            return answer;
        }
        if (oneSymbolTokens.contains(s)) {
            return null;
        }
        nextToken = nextToken();
        if ("(".equals(nextToken)) {
            return tryParseFunction(s);
        }
        Object answer = context.getVariable(s);
        if (answer != null) {
            return answer;
        }
        answer = tryParseNumericalValue(s);
        if (answer != null) {
            return answer;
        }
        errorMessage = "Cannot parse '" + s + "' at position " + (nowPosition - s.length() + 1);
        return null;
    }

    private Object tryParseFunction(String s) {
        nextToken();
        if (s.equals("get_instance_id") || s.equals("get_process_id")) {
            if (!nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return context.getProcess().getId();
        }
        if (s.equals("current_date_time")) {
            if (!nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return new Date();
        }
        if (s.equals("current_date")) {
            if (!nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return actions.dateFunction(new Date());
        }
        if (s.equals("current_time")) {
            if (!nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return actions.timeFunction(new Date());
        }
        if (s.equals("date")) {
            Object param1 = parsePriority0();
            if (param1 == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return actions.dateFunction(param1);
        }
        if (s.equals("time")) {
            Object param1 = parsePriority0();
            if (param1 == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return actions.timeFunction(param1);
        }
        if (s.equals("hours_round_up")) {
            Object param1 = parsePriority0();
            if (param1 == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return actions.hoursRoundUpFunction(param1);
        }
        if (s.equals("round_up")) {
            Object param1 = parsePriority0();
            Double d = (Double) actions.translate(param1, Double.class);
            if (d == null) {
                incorrectParameters(s);
                return null;
            }
            Integer num = 0;
            String tok = nextToken();
            if (!tok.equals(")")) {
                if (!tok.equals(",")) {
                    incorrectParameters(s);
                    return null;
                }
                num = (Integer) actions.translate(parsePriority0(), Integer.class);
                if (num == null) {
                    incorrectParameters(s);
                    return null;
                }
                tok = nextToken();
            }
            if (!tok.equals(")")) {
                incorrectParameters(s);
                return null;
            }
            if (num <= 0) {
                return actions.roundUpFunction(d);
            }
            return actions.roundUpFunction(d, num);
        }
        if (s.equals("round_down")) {
            Object param1 = parsePriority0();
            Double d = (Double) actions.translate(param1, Double.class);
            if (d == null) {
                incorrectParameters(s);
                return null;
            }
            Integer num = 0;
            String tok = nextToken();
            if (!tok.equals(")")) {
                if (!tok.equals(",")) {
                    incorrectParameters(s);
                    return null;
                }
                num = (Integer) actions.translate(parsePriority0(), Integer.class);
                if (num == null) {
                    incorrectParameters(s);
                    return null;
                }
                tok = nextToken();
            }
            if (!tok.equals(")")) {
                incorrectParameters(s);
                return null;
            }
            if (num <= 0) {
                return actions.roundDownFunction(d);
            }
            return actions.roundDownFunction(d, num);
        }
        if (s.equals("round")) {
            Object param1 = parsePriority0();
            Double d = (Double) actions.translate(param1, Double.class);
            if (d == null) {
                incorrectParameters(s);
                return null;
            }
            Integer num = 0;
            String tok = nextToken();
            if (!tok.equals(")")) {
                if (!tok.equals(",")) {
                    incorrectParameters(s);
                    return null;
                }
                num = (Integer) actions.translate(parsePriority0(), Integer.class);
                if (num == null) {
                    incorrectParameters(s);
                    return null;
                }
                tok = nextToken();
            }
            if (!tok.equals(")")) {
                incorrectParameters(s);
                return null;
            }
            if (num <= 0) {
                return actions.roundFunction(d);
            }
            return actions.roundFunction(d, num);
        }
        if (s.equals("number_to_string_ru")) {
            Object param1 = parsePriority0();
            String tok = nextToken();
            if (param1 == null) {
                incorrectParameters(s);
                return null;
            }
            if (tok.equals(")")) {
                Long number = (Long) actions.translate(param1, Long.class);
                if (number == null) {
                    incorrectParameters(s);
                    return null;
                }
                return NumberToString_ru.numberToString(number);
            }
            if (!tok.equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param2 = parsePriority0();
            if (param2 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param3 = parsePriority0();
            if (param3 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param4 = parsePriority0();
            if (param4 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param5 = parsePriority0();
            if (param5 == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            Long number = (Long) actions.translate(param1, Long.class);
            int p = -1;
            if (param2.toString().equals("M")) {
                p = 0;
            }
            if (param2.toString().equals("F")) {
                p = 1;
            }
            if (p == -1 || number == null) {
                incorrectParameters(s);
                return null;
            }
            String s1 = param3.toString();
            String s2 = param4.toString();
            String s3 = param5.toString();
            return NumberToString_ru.numberToString(number, new NumberToString_ru.Word(p, new String[] { s1, s2, s3 }));
        }
        if (s.equals("FIO_case_ru")) {
            Object param1 = parsePriority0();
            if (param1 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param2 = parsePriority0();
            if (param2 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param3 = parsePriority0();
            if (param3 == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            String fio = param1.toString();
            Integer caseNumber = (Integer) actions.translate(param2, Integer.class);
            if (caseNumber == null || caseNumber < 1 || caseNumber > 6) {
                incorrectParameters(s);
                return null;
            }
            String mode = param3.toString();
            return actions.nameCaseRussian(fio, caseNumber, mode);
        }
        if (s.equals("number_to_short_string_ru")) {
            Object param1 = parsePriority0();
            if (param1 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param2 = parsePriority0();
            if (param2 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param3 = parsePriority0();
            if (param3 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param4 = parsePriority0();
            if (param4 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param5 = parsePriority0();
            if (param5 == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            Long number = (Long) actions.translate(param1, Long.class);
            int p = -1;
            if (param2.toString().equals("M")) {
                p = 0;
            }
            if (param2.toString().equals("F")) {
                p = 1;
            }
            if (p == -1 || number == null) {
                incorrectParameters(s);
                return null;
            }
            String s1 = param3.toString();
            String s2 = param4.toString();
            String s3 = param5.toString();
            return NumberToString_ru.numberToShortString(number, new NumberToString_ru.Word(p, new String[] { s1, s2, s3 }));
        }
        return null;
    }

    private void incorrectParameters(String function) {
        errorMessage = "Incorrect parameters for " + function + " function at position " + nowPosition;
    }

    private Object tryParseNumericalValue(String s) {
        if (s.equals(":")) {
            return nextToken();
        }
        try {
            return new Long(Long.parseLong(s));
        } catch (NumberFormatException e) {
        }
        try {
            return new Double(Double.parseDouble(s));
        } catch (NumberFormatException e) {
        }
        if (s.equalsIgnoreCase("true")) {
            return new Boolean(true);
        }
        if (s.equalsIgnoreCase("false")) {
            return new Boolean(false);
        }
        try {
            return CalendarUtil.convertToDate(s, CalendarUtil.DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT);
        } catch (Exception e) {
        }
        try {
            return CalendarUtil.convertToDate(s, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
        } catch (Exception e) {
        }
        try {
            return CalendarUtil.convertToDate(s, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
        } catch (Exception e) {
        }
        try {
            return CalendarUtil.convertToDate(s, CalendarUtil.HOURS_MINUTES_SECONDS_FORMAT);
        } catch (Exception e) {
        }
        try {
            return CalendarUtil.convertToDate(s, CalendarUtil.HOURS_MINUTES_FORMAT);
        } catch (Exception e) {
        }
        return null;
    }

    private void error(String message) {
        log.warn("Incorrect formula '" + idsuf + "' -> " + new String(formula));
        if (message != null) {
            log.warn(" - " + message);
        }
    }

    @Override
    public void setConfiguration(String configurationName) throws ConfigurationException {
        inputData = configurationName;
    }

    public FormulaActionHandler() {
    }

    public FormulaActionHandler(String configurationName) {
        inputData = configurationName;
    }
}
