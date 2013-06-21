package ru.runa.common;

public class Version {
    private static String version = WebResources.getVersion();
    private static boolean display = WebResources.isVersionDisplay();

    public static boolean isDisplay() {
        return display;
    }

    public static String get() {
        return version;
    }

}
