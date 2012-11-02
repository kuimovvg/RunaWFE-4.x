package ru.runa.wfe.commons;

import java.util.Map;

import ru.runa.wfe.var.IVariableProvider;

public interface IScriptExecutor {

    public Map<String, Object> executeScript(String script, IVariableProvider variableProvider);

    public <T extends Object> T evaluateScript(String script, IVariableProvider variableProvider);

}
