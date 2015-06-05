package ru.runa.wfe.commons;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.IVariableProvider;

public class BSHScriptExecutor extends GroovyScriptExecutor {

    @Override
    public Map<String, Object> executeScript(ExecutionContext executionContext, String script) {
        try {
            return super.executeScript(executionContext, adjustScript(script));
        } catch (RuntimeException e) {
            log.error("BSH adjusted conf: " + script);
            throw e;
        }
    }

    @Override
    public Object evaluateScript(ProcessDefinition processDefinition, IVariableProvider variableProvider, String script) {
        try {
            return super.evaluateScript(processDefinition, variableProvider, adjustScript(script));
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
    protected GroovyScriptBinding createBinding(ProcessDefinition processDefinition, IVariableProvider variableProvider) {
        if (SystemProperties.isV3CompatibilityMode()) {
            return new BackCompatibilityBinding(processDefinition, variableProvider);
        }
        return super.createBinding(processDefinition, variableProvider);
    }

    private static class BackCompatibilityBinding extends GroovyScriptBinding {

        public BackCompatibilityBinding(ProcessDefinition processDefinition, IVariableProvider variableProvider) {
            super(processDefinition, variableProvider);
        }

        @Override
        protected Object getVariableFromProcess(String name) {
            Object value = super.getVariableFromProcess(name);
            if (value instanceof Executor) {
                log.debug("Converting Executor -> String");
                value = TypeConversionUtil.convertTo(String.class, value);
            }
            return value;
        }

    }

}
