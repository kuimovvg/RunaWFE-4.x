package ru.runa.wfe.commons;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.dao.SettingDAO;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PropertyResources {
    private static final Log log = LogFactory.getLog(PropertyResources.class);
    private final String fileName;
    private final Properties PROPERTIES;
    private final boolean useDatabase;
    private static boolean databaseAvailable = false;

    public static void setDatabaseAvailable(boolean available) {
        databaseAvailable = available;
    }

    private SettingDAO settingDAO = null;

    public PropertyResources(String fileName) {
        this(fileName, true, true);
    }

    public PropertyResources(String fileName, boolean required) {
        this(fileName, required, true);
    }

    public PropertyResources(String fileName, boolean required, boolean useDatabase) {
        this.useDatabase = useDatabase;
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
        if (databaseAvailable && useDatabase) {
            if (settingDAO == null) {
                try {
                    settingDAO = ApplicationContextFactory.getSettingDAO();
                } catch (Exception e) {
                    log.error("No SettingDAO available", e);
                }
            }
            if (settingDAO != null) {
                try {
                    String v = settingDAO.getValue(fileName, name);
                    if (v != null) {
                        return v;
                    }
                } catch (Exception e) {
                    log.error("Database error", e);
                }
            }
        }
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

    public List<String> getMultipleStringProperty(String name) {
        String result = getStringProperty(name);
        if (result == null) {
            return null;
        }
        return Lists.newArrayList(result.split(";", -1));
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
