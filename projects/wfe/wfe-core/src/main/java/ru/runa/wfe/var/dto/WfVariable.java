package ru.runa.wfe.var.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ru.runa.wfe.var.VariableDefinition;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

@XmlAccessorType(XmlAccessType.FIELD)
public class WfVariable implements Serializable {
    private static final long serialVersionUID = 1L;

    private VariableDefinition definition;
    private Object value;

    public WfVariable() {
    }

    public WfVariable(VariableDefinition definition, Object value) {
        Preconditions.checkNotNull(definition);
        this.definition = definition;
        this.value = value;
    }

    public VariableDefinition getDefinition() {
        return definition;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(definition.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfVariable) {
            return Objects.equal(definition.getName(), ((WfVariable) obj).definition.getName());
        }
        return super.equals(obj);
    }

}
