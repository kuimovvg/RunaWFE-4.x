package ru.runa.wfe.extension.handler;

import java.util.Date;

import javax.script.ScriptException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.FormatCommons;

public class RhinoJSActionHandler implements ActionHandler {
    private String configuration;

    @Override
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public void execute(ExecutionContext executionContext) throws ScriptException {
        try {
            Context context = Context.enter();
            Scriptable scope = context.initStandardObjects();
            for (VariableDefinition definition : executionContext.getProcessDefinition().getVariables()) {
                Object value = executionContext.getVariable(definition.getName());
                if (value != null) {
                    Object js = javaToJs(context, scope, value);
                    ScriptableObject.putProperty(scope, definition.getScriptingName(), js);
                }
            }
            context.evaluateString(scope, configuration, "<cmd>", 1, null);
            for (VariableDefinition definition : executionContext.getProcessDefinition().getVariables()) {
                Object js = scope.get(definition.getScriptingName(), scope);
                if (js != Scriptable.NOT_FOUND) {
                    Object newValue = Context.jsToJava(js, FormatCommons.create(definition).getJavaClass());
                    if (newValue != null) {
                        executionContext.setVariable(definition.getName(), newValue);
                    }
                }
            }
        } finally {
            Context.exit();
        }
    }

    private Object javaToJs(Context context, Scriptable scope, Object value) {
        if (value instanceof Date) {
            return context.newObject(scope, "Date", new Object[] { ((Date) value).getTime() });
        }
        return Context.javaToJS(value, scope);
    }

}
