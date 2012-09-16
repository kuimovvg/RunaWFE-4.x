package ru.runa.alfresco;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.alfresco.anno.Assoc;
import ru.runa.alfresco.anno.Property;
import ru.runa.alfresco.anno.Type;

/**
 * Registered mappings between Java and Alfresco types and namespace prefixes.
 * @author dofs
 */
public class Mappings extends Settings {
    private static Map<Class<?>, AlfTypeDesc> MAPPINGS_BY_CLASS = new HashMap<Class<?>, AlfTypeDesc>();
    private static Map<String, AlfTypeDesc> MAPPINGS_BY_NAMESPACED_TYPE_NAME = new HashMap<String, AlfTypeDesc>();
    private static Map<String, String> NAMESPACES = new HashMap<String, String>();
    private static Map<Class<?>, String> LOCATIONS = new HashMap<Class<?>, String>();

    private static boolean namespacesLoaded = false;
    private static boolean mappingsLoaded = false;

    public static String getFolderUUID(Class<?> objectClass) {
        loadMappings();
        do {
            if (LOCATIONS.containsKey(objectClass)) {
                return LOCATIONS.get(objectClass);
            }
            objectClass = objectClass.getSuperclass();
        } while (objectClass != null);
        return LOCATIONS.get(Object.class);
    }

    public static String getNamespacePrefix(String namespace) {
        loadNamespaces();
        for (String prefix : NAMESPACES.keySet()) {
            if (namespace.equals(NAMESPACES.get(prefix))) {
                return prefix;
            }
        }
        throw new RuntimeException("No prefix found for namespace " + namespace);
    }

    public static String getNamespace(String prefix) {
        loadNamespaces();
        if (NAMESPACES.containsKey(prefix)) {
            return NAMESPACES.get(prefix);
        }
        throw new RuntimeException("No namespace found for prefix " + prefix);
    }

    @SuppressWarnings("unchecked")
    private static synchronized void loadNamespaces() {
        if (namespacesLoaded) {
            return;
        }
        try {
            Document document = getConfigDocument();
            Element root = document.getRootElement();

            Element importsElement = root.element("imports");
            List<Element> namespaceElements = importsElement.elements("namespace");
            for (Element element : namespaceElements) {
                String prefix = element.attributeValue("prefix");
                String namespace = element.getTextTrim();
                NAMESPACES.put(prefix, namespace);
            }
        } catch (Throwable e) {
            log.fatal("Unable to load namespaces", e);
        }
        namespacesLoaded = true;
    }

    @SuppressWarnings("unchecked")
    private static synchronized void loadMappings() {
        loadNamespaces();
        if (mappingsLoaded) {
            return;
        }
        try {
            Document document = getConfigDocument();
            Element root = document.getRootElement();

            Element mappingsElement = root.element("mappings");
            String defaultFolderUUID = mappingsElement.attributeValue("location");
            LOCATIONS.put(Object.class, defaultFolderUUID);

            List<Element> classElements = mappingsElement.elements("class");
            for (Element element : classElements) {
                String className = element.getTextTrim();
                try {
                    Class<?> clazz = Class.forName(className);
                    String location = element.attributeValue("location");
                    if (location != null) {
                        LOCATIONS.put(clazz, location);
                    }
                    loadTypeHierarchyMappings(clazz);
                } catch (Throwable e) {
                    log.error("Unable to register mapping " + className, e);
                }
            }
        } catch (Throwable e) {
            log.fatal("Unable to load mappings", e);
        }
        mappingsLoaded = true;
    }

    private static AlfTypeDesc loadTypeMapping(Class<?> clazz) {
        Type typeAnn = clazz.getAnnotation(Type.class);
        if (typeAnn == null) {
            return null;
        }
        String typeName = typeAnn.name();
        String prefix = typeAnn.prefix();
        String namespace = NAMESPACES.get(prefix);
        AlfTypeDesc typeDesc = new AlfTypeDesc(prefix, namespace, clazz.getName(), typeName);
        typeDesc.setAspect(typeAnn.aspect());

        for (Field field : clazz.getDeclaredFields()) {
            String fieldName = field.getName();
            AlfSerializerDesc serializer = null;
            Property propertyAnn = field.getAnnotation(Property.class);
            if (propertyAnn != null) {
                serializer = AlfSerializerDesc.newProp(namespace, fieldName, propertyAnn);
            }
            Assoc assocAnn = field.getAnnotation(Assoc.class);
            if (assocAnn != null) {
                serializer = AlfSerializerDesc.newAssoc(namespace, fieldName, assocAnn);
            }
            if (serializer != null) {
                typeDesc.addPropertyMapping(serializer);
            }
        }
        MAPPINGS_BY_CLASS.put(clazz, typeDesc);
        MAPPINGS_BY_NAMESPACED_TYPE_NAME.put(typeDesc.getAlfrescoTypeNameWithNamespace(), typeDesc);
        return typeDesc;
    }

    private static void loadTypeHierarchyMappings(Class<?> clazz) {
        AlfTypeDesc result = null;
        do {
            AlfTypeDesc typeDesc = MAPPINGS_BY_CLASS.get(clazz);
            if (typeDesc == null) {
                typeDesc = loadTypeMapping(clazz);
            }
            if (typeDesc != null) {
                if (result == null) {
                    result = typeDesc;
                } else {
                    for (AlfSerializerDesc alfPropertyDesc : typeDesc.getAllDescs()) {
                        result.addPropertyMapping(alfPropertyDesc);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);
    }
    
    public static AlfTypeDesc getMapping(Class<?> clazz) {
        loadMappings();
        if (!MAPPINGS_BY_CLASS.containsKey(clazz)) {
            throw new RuntimeException("No mapping found for " + clazz);
        }
        return MAPPINGS_BY_CLASS.get(clazz);
    }

    public static AlfTypeDesc getMapping(String typeName) {
        loadMappings();
        if (!MAPPINGS_BY_NAMESPACED_TYPE_NAME.containsKey(typeName)) {
            throw new RuntimeException("No mapping found for " + typeName);
        }
        return MAPPINGS_BY_NAMESPACED_TYPE_NAME.get(typeName);
    }

}
