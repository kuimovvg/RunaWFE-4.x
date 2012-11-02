package ru.runa.wfe.var.dto;

import java.io.Serializable;

import ru.runa.wfe.var.VariableDefinition;


public class WfVariable implements Serializable {
    private static final long serialVersionUID = 1L;
    private VariableDefinition definition;
    private Object value;

    public WfVariable() {
    }

    public WfVariable(VariableDefinition definition, Object value) {
        this.definition = definition;
        this.value = value;
    }

    public VariableDefinition getDefinition() {
        return definition;
    }

    public Object getValue() {
        return value;
    }
}
