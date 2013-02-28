package ru.runa.alfresco;

import org.alfresco.service.namespace.QName;

import ru.runa.alfresco.anno.Assoc;
import ru.runa.alfresco.anno.Property;

import com.google.common.base.Objects;

/**
 * Descriptor for Alfresco property and association mapping.
 * 
 * @author dofs
 */
public class AlfSerializerDesc {
    protected final String fieldName;
    protected String propertyName;
    protected final String namespace;
    protected String title;
    protected String dataType;
    protected String defaultValue;
    protected final Assoc assoc;
    protected final Property property;

    private AlfSerializerDesc(String namespace, String javaPropertyName, Assoc assoc, Property property) {
        this.namespace = namespace;
        fieldName = javaPropertyName;
        this.property = property;
        if (property != null) {
            propertyName = property.name();
        }
        this.assoc = assoc;
        if (assoc != null) {
            propertyName = assoc.name();
        }
    }

    public static AlfSerializerDesc newProp(String namespace, String javaPropertyName, Property property) {
        return new AlfSerializerDesc(namespace, javaPropertyName, null, property);
    }

    public static AlfSerializerDesc newAssoc(String namespace, String javaPropertyName, Assoc assoc) {
        return new AlfSerializerDesc(namespace, javaPropertyName, assoc, null);
    }

    public String getPropertyNameWithNamespace() {
        return getPropertyQName().toString();
    }

    public QName getPropertyQName() {
        if (propertyName.contains(":")) {
            int index = propertyName.indexOf(":");
            String prefix = propertyName.substring(0, index);
            String propName = propertyName.substring(index + 1);
            return QName.createQName(Mappings.getNamespace(prefix), propName);
        }
        return QName.createQName(namespace, propertyName);
    }

    public Assoc getAssoc() {
        return assoc;
    }

    public Property getProperty() {
        return property;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isNodeReference() {
        return property != null && property.noderef();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDefaultValue() {
        return defaultValue != null ? defaultValue : "";
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("alf", propertyName).add("java", fieldName).add("type", dataType).toString();
    }
}
