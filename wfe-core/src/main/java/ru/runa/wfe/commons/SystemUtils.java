package ru.runa.wfe.commons;

public class SystemUtils {
    private static final boolean developmentMode = "true".equals(System.getProperty("devmode"));
    public static final String WEB_SERVICE_NAMESPACE = "http://runa.ru/wfe";

    /**
     * Production or development mode?
     */
    public static boolean isDevMode() {
        return developmentMode;
    }
}
