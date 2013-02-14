package ru.runa.wfe.var.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

@XmlAccessorType(XmlAccessType.FIELD)
public class WfVariable implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private VariableDefinition definition;
    private Object value;

    public WfVariable() {
    }

    public WfVariable(String name, Object value) {
        Preconditions.checkNotNull(name);
        this.name = name;
        this.value = value;
    }

    public WfVariable(VariableDefinition definition, Object value) {
        Preconditions.checkNotNull(definition);
        this.name = definition.getName();
        this.definition = definition;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    /**
     * Can be <code>null</code>.
     * 
     * @return
     */
    public VariableDefinition getDefinition() {
        return definition;
    }

    public VariableFormat<Object> getFormatNotNull() {
        return FormatCommons.create(getFormatClassNameNotNull());
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

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfVariable) {
            return Objects.equal(definition, ((WfVariable) obj).name);
        }
        return super.equals(obj);
    }

}
