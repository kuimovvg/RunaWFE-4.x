package ru.runa.alfresco;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.namespace.QName;

import com.google.common.base.Objects;

/**
 * Descriptor for Alfresco type mapping.
 * 
 * @author dofs
 */
public class AlfTypeDesc {
    private Map<String, AlfPropertyDesc> BY_FIELD_NAME = new HashMap<String, AlfPropertyDesc>();
    private Map<String, AlfPropertyDesc> BY_NAMESPACED_PROPERTY_NAME = new HashMap<String, AlfPropertyDesc>();
    private final String javaClassName;
    private final String alfrescoTypeName;
    private final String namespace;
    private final String prefix;
    private boolean aspect = false;
    private boolean classDefinitionLoaded = false;
    private String title;

    public AlfTypeDesc(String prefix, String namespace, String javaClassName, String alfrescoTypeName) {
        this.javaClassName = javaClassName;
        this.alfrescoTypeName = alfrescoTypeName;
        this.prefix = prefix;
        this.namespace = namespace;
    }

    public AlfTypeDesc(AlfTypeDesc desc) {
        javaClassName = desc.javaClassName;
        alfrescoTypeName = desc.alfrescoTypeName;
        prefix = desc.prefix;
        namespace = desc.namespace;
    }

    public void addPropertyMapping(AlfPropertyDesc desc) {
        BY_FIELD_NAME.put(desc.getFieldName(), desc);
        BY_NAMESPACED_PROPERTY_NAME.put(desc.getPropertyNameWithNamespace(), desc);
    }

    public AlfPropertyDesc getPropertyDescByFieldName(String fieldName) {
        return BY_FIELD_NAME.get(fieldName);
    }

    public AlfPropertyDesc getPropertyDescByTypeName(String propertyName) {
        return BY_NAMESPACED_PROPERTY_NAME.get(propertyName);
    }

    public Collection<AlfPropertyDesc> getAllDescs() {
        return BY_FIELD_NAME.values();
    }

    public String getAlfrescoTypeName() {
        return alfrescoTypeName;
    }

    public String getAlfrescoTypeNameWithPrefix() {
        return prefix + ":" + alfrescoTypeName;
    }

    public String getAlfrescoTypeNameWithNamespace() {
        return QName.createQName(namespace, alfrescoTypeName).toString();
    }

    public String getNamespace() {
        return namespace;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public boolean isAspect() {
        return aspect;
    }

    public void setAspect(boolean aspect) {
        this.aspect = aspect;
    }

    public boolean isClassDefinitionLoaded() {
        return classDefinitionLoaded;
    }

    public void setClassDefinitionLoaded(boolean classDefinitionLoaded) {
        this.classDefinitionLoaded = classDefinitionLoaded;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("alf", getAlfrescoTypeNameWithPrefix()).add("java", javaClassName).toString();
    }
}
