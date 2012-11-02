package ru.runa.wfe.commons;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.execution.dto.WfProcess;
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
        return script.replaceAll("void", "null").replaceAll("transition", WfProcess.SELECTED_TRANSITION_KEY);
    }

}
