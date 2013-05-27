package ru.runa.wfe.commons;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.collect.Maps;

public class PropertyResources {
    private final String fileName;
    private final Properties PROPERTIES;

    public PropertyResources(String fileName) {
        this(fileName, true);
    }

    public PropertyResources(String fileName, boolean required) {
        this.fileName = fileName;
        PROPERTIES = ClassLoaderUtil.getProperties(fileName, required);
    }

    public Set<String> getAllPropertyNames() {
        return PROPERTIES.stringPropertyNames();
    }

    public Map<String, String> getAllProperties() {
        Map<String, String> map = Maps.newHashMap();
        for (String name : PROPERTIES.stringPropertyNames()) {
            map.put(name, PROPERTIES.getProperty(name));
        }
        return map;
    }

    public String getStringProperty(String name) {
        return PROPERTIES.getProperty(name);
    }

    public String getStringPropertyNotNull(String name) {
        String string = getStringProperty(name);
        if (string != null) {
            return string;
        }
        throw new InternalApplicationException("No property '" + name + "' was found in '" + fileName + "'");
    }

    public String getStringProperty(String name, String defaultValue) {
        String result = getStringProperty(name);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    public boolean getBooleanProperty(String name, boolean defaultValue) {
        String result = getStringProperty(name);
        if (result == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(result);
    }

    public int getIntegerProperty(String name, int defaultValue) {
        String result = getStringProperty(name);
        if (result == null) {
            return defaultValue;
        }
        return Integer.parseInt(result);
    }

    public long getLongProperty(String name, long defaultValue) {
        String result = getStringProperty(name);
        if (result == null) {
            return defaultValue;
        }
        return Long.parseLong(result);
    }

}
