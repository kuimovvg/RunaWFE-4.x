package ru.runa.wfe.commons;

import java.util.Map;

import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.IVariableProvider;

public interface IScriptExecutor {

    public Map<String, Object> executeScript(ProcessDefinition processDefinition, IVariableProvider variableProvider, String script);

    public Object evaluateScript(ProcessDefinition processDefinition, IVariableProvider variableProvider, String script);

}
