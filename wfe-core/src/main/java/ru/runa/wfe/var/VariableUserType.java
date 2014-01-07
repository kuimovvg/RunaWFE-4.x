package ru.runa.wfe.var;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
public class VariableUserType {
    public static final String DELIM = ".";
    private String name;
    private final List<VariableDefinition> attributes = Lists.newArrayList();

    public VariableUserType() {
    }

    public VariableUserType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<VariableDefinition> getAttributes() {
        return attributes;
    }

    /**
     * @return attribute definition using recursion
     */
    public VariableDefinition getAttributeNotNull(String name) {
        int dotIndex = name.indexOf(VariableUserType.DELIM);
        if (dotIndex > 0) {
            String parentName = name.substring(0, dotIndex);
            String attributeName = name.substring(dotIndex + 1);
            VariableDefinition parentDefinition = getAttributeNotNull(parentName);
            if (!parentDefinition.isComplex()) {
                throw new InternalApplicationException(parentDefinition + "' is not user defined type");
            }
            return parentDefinition.getUserType().getAttributeNotNull(attributeName);
        }
        for (VariableDefinition definition : getAttributes()) {
            if (Objects.equal(name, definition.getName())) {
                return definition;
            }
        }
        throw new InternalApplicationException("No attribute found by name '" + name + "' in " + this);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, attributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VariableUserType)) {
            return false;
        }
        VariableUserType type = (VariableUserType) obj;
        return Objects.equal(name, type.name) && Objects.equal(attributes, type.attributes);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).add("name", name).add("attributes", attributes).toString();
    }

}
