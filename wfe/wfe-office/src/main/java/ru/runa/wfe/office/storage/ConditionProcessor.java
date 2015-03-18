package ru.runa.wfe.office.storage;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class ConditionProcessor {

    private static ScriptEngine engine;
    static {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByName("JavaScript");
    }

    public static boolean filter(Object value, Op op, Object compared) {
        try {
            boolean brackQuotes = value instanceof String;
            StringBuilder script = new StringBuilder();
            script.append("(function(){return ");
            if (op.equals(Op.LIKE) || op.equals(Op.ILIKE)) {
                script.append(String.format(op.getSymbol(), value, compared));
            } else {
                if (brackQuotes) {
                    script.append("'");
                }
                script.append(value.toString());
                if (brackQuotes) {
                    script.append("'");
                }
                script.append(" ");
                script.append(op.getSymbol());
                script.append(" ");
                if (brackQuotes) {
                    script.append("'");
                }
                script.append(compared.toString());
                if (brackQuotes) {
                    script.append("'");
                }
            }
            script.append(";})()");
            Object result = engine.eval(script.toString());
            return (Boolean) result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
