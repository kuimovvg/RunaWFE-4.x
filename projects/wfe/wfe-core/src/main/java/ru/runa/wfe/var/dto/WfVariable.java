package ru.runa.wfe.var.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.jaxb.VariableAdapter;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

@XmlType(namespace = "http://stub.service.wfe.runa.ru/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlJavaTypeAdapter(VariableAdapter.class)
public class WfVariable implements Serializable {
    private static final long serialVersionUID = 1L;

    private VariableDefinition definition;
    private Object value;

    public WfVariable() {
        definition = new VariableDefinition();
    }

    public WfVariable(String name, Object value) {
        Preconditions.checkNotNull(name);
        definition = new VariableDefinition(true, name, StringFormat.class.getName(), name);
        this.value = value;
    }

    public WfVariable(VariableDefinition definition, Object value) {
        Preconditions.checkNotNull(definition);
        this.definition = definition;
        this.value = value;
    }

    public VariableDefinition getDefinition() {
        return definition;
    }

    public VariableFormat getFormatNotNull() {
        return FormatCommons.create(getDefinition());
    }

    public String getFormatClassNameNotNull() {
        if (definition != null && definition.getFormatClassName() != null) {
            return definition.getFormatClassName();
        }
        return StringFormat.class.getName();
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(definition);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfVariable) {
            return Objects.equal(definition.getName(), ((WfVariable) obj).definition.getName());
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("definition", definition).add("value", value).toString();
    }

}
