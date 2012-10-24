package org.jbpm.ui.util;

import java.util.HashMap;
import java.util.Map;

public class TypeNameMapping {
    private static Map<String, String> mapping = new HashMap<String, String>();
    private static Map<String, String> hiddenMapping = new HashMap<String, String>();
    static {
        MappingContentProvider.INSTANCE.addMappingInfo();
    }

    public static String getTypeName(String key) {
        if (mapping.containsKey(key)) {
            return mapping.get(key);
        }
        return key;
    }

    public static boolean showType(String key) {
        return !hiddenMapping.containsKey(key);
    }

    public static Map<String, String> getMapping() {
        if (mapping.size() == 0) {
            MappingContentProvider.INSTANCE.addMappingInfo();
        }
        return mapping;
    }

    public static Map<String, String> getHiddenMapping() {
        if (mapping.size() == 0) {
            MappingContentProvider.INSTANCE.addMappingInfo();
        }
        return hiddenMapping;
    }

    public static void setMapping(Map<String, String> newMapping) {
        mapping = newMapping;
    }

    public static void setHiddenMapping(Map<String, String> newMapping) {
        hiddenMapping = newMapping;
    }

    public static void addMapping(String key, String value) {
        mapping.put(key, value);
    }

    public static void removeMapping(String key) {
        mapping.remove(key);
        hiddenMapping.remove(key);
    }

    public static boolean containsMapping(String key) {
        return mapping.containsKey(key);
    }
    
    public static void updateMapping(String key, String value) {
        if (mapping.containsKey(key)) {
            mapping.put(key, value);
        }
        if (hiddenMapping.containsKey(key)) {
            hiddenMapping.put(key, value);
        }
    }

}
