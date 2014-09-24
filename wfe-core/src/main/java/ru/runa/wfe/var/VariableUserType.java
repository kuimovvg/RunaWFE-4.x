package ru.runa.wfe.var;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
public class VariableUserType implements Serializable {
    private static final long serialVersionUID = -1054823598655227725L;
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

    public VariableDefinition getAttribute(String name) {
        int firstDotIndex = name.indexOf(VariableUserType.DELIM);
        if (firstDotIndex != -1) {
            String attributeName = name.substring(0, firstDotIndex);
            VariableDefinition attributeDefinition = getAttribute(attributeName);
            if (attributeDefinition == null) {
                return null;
            }
            if (attributeDefinition.getUserType() == null) {
                throw new InternalApplicationException("Trying to retrieve complex attribute in non-complex parent attribute: " + name);
            }
            String nameRemainder = name.substring(firstDotIndex + 1);
            return attributeDefinition.getUserType().getAttribute(nameRemainder);
        }
        for (VariableDefinition definition : attributes) {
            if (Objects.equal(name, definition.getName())) {
                return definition;
            }
        }
        return null;
    }

    public VariableDefinition getAttributeNotNull(String name) {
        VariableDefinition definition = getAttribute(name);
        if (definition != null) {
            return definition;
        }
        throw new InternalApplicationException("No attribute '" + name + "' found in " + this);
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
