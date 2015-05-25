package ru.runa.wfe.var;

import ru.runa.wfe.lang.ProcessDefinition;

import com.google.common.base.Objects;

public class ScriptingVariableProvider extends DelegableVariableProvider {
    private final ProcessDefinition processDefinition;

    public ScriptingVariableProvider(ProcessDefinition processDefinition, IVariableProvider variableProvider) {
        super(variableProvider);
        this.processDefinition = processDefinition;
    }

    @Override
    public Object getValue(String variableName) {
        Object object = super.getValue(variableName);
        if (object == null) {
            for (VariableDefinition definition : processDefinition.getVariables()) {
                if (Objects.equal(variableName, definition.getScriptingName())) {
                    object = super.getValue(definition.getName());
                    break;
                }
            }
        }
        if (object instanceof ComplexVariable) {
            object = new ScriptingComplexVariable((ComplexVariable) object);
        }
        return object;
    }

}
