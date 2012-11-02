package ru.runa.common;

public class Version {
    private static String version;
    private static boolean display = true;

    public static void set(String version) {
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
        version = WebResources.getVersion();
        display = WebResources.isVersionDisplay();
    }
}
