package ru.runa.wfe.commons;

import groovy.lang.Binding;
import groovy.lang.MissingPropertyException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.IVariableProvider;

public class BSHScriptExecutor extends GroovyScriptExecutor {

    private static final Log log = LogFactory.getLog(GroovyScriptExecutor.class);

    @Override
    public Map<String, Object> executeScript(String script, IVariableProvider variableProvider) {
        try {
            return super.executeScript(adjustScript(script), variableProvider);
        } catch (RuntimeException e) {
            log.error("BSH adjusted conf: " + script);
            throw e;
        }
    }

    @Override
    public <T extends Object> T evaluateScript(String script, IVariableProvider variableProvider) {
        try {
            return (T) super.evaluateScript(adjustScript(script), variableProvider);
        } catch (RuntimeException e) {
            log.error("BSH adjusted conf: " + script);
            throw e;
        }
    }

    private static String adjustScript(String script) {
        script = script.replaceAll(Pattern.quote("}"), Matcher.quoteReplacement("};"));
        script = script.replaceAll("transition", Matcher.quoteReplacement(WfProcess.SELECTED_TRANSITION_KEY));
        script = script.replaceAll("void", Matcher.quoteReplacement("null"));
        return script;
    }

    @Override
    protected Binding createBinding(IVariableProvider variableProvider) {
        if (SystemProperties.isV3CompatibilityMode()) {
            return new BackCompatibilityBinding(variableProvider);
        }
        return super.createBinding(variableProvider);
    }

    private static class BackCompatibilityBinding extends Binding {
        private final IVariableProvider variableProvider;

        public BackCompatibilityBinding(IVariableProvider variableProvider) {
            this.variableProvider = variableProvider;
        }

        @Override
        public Object getVariable(String name) {
            try {
                return super.getVariable(name);
            } catch (MissingPropertyException e) {
                Object value = variableProvider.getValue(name);
                if (value == null) {
                    log.warn("Variable '" + name + "' passed to script as null (not defined in process)");
                }
                if (value instanceof Executor) {
                    log.debug("Converting Executor -> String");
                    value = TypeConversionUtil.convertTo(String.class, value);
                }
                return value;
            }
        }

        @Override
        public boolean hasVariable(String name) {
            throw new UnsupportedOperationException("Implement if will be used");
        }

    }

}
