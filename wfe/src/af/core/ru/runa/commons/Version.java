package ru.runa.commons;

import org.apache.commons.logging.LogFactory;

public class Version {
    private static String version;
    private static boolean display = true;
    
    public void set(String version) {
        Version.version = version;
    }
    
    public static void setDisplay(boolean display) {
        Version.display = display;
    }
    
    public static boolean isDisplay() {
        return display;
    }

    public static String get() {
        if (version == null) {
            synchronized (Version.class) {
                if (version == null) {
                    loadAppVersion();
                }                
            }
        }
        return version;
    }

    private static void loadAppVersion() {
        try {
            version = ResourceCommons.readProperty("version", "common_settings");
        } catch (Exception e) {
            LogFactory.getLog(Version.class).error("Unable to get version", e);
            version = "UNDEFINED";
        }
    }
}
