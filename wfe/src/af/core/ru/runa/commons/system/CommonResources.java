package ru.runa.commons.system;

import ru.runa.commons.ResourceCommons;
// TODO move to web
public class CommonResources extends ResourceCommons {
    public static final String COMMON_SETTINGS_PROPERTY_FILE = "common_settings";

    public CommonResources() {
        super(COMMON_SETTINGS_PROPERTY_FILE);
    }

    public String[] readPropertyAsArray(String propertyName, String delimiter) {
        String value = readPropertyIfExist(propertyName);
        if (value != null) {
            return value.split(delimiter);
        }
        return new String[0];
    }
    
    public static long getDiagramRefreshInterval() {
        return Long.parseLong(readPropertyIfExist("diagram.refresh.interval.ms", COMMON_SETTINGS_PROPERTY_FILE, "0"));
    }
}
