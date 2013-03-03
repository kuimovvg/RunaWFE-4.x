package ru.runa.wfe.commons;

public class SystemProperties {
    private static final PropertyResources RESOURCES = new PropertyResources("wfe.properties");
    private static final boolean developmentMode = "true".equals(System.getProperty("devmode"));
    private static final boolean v3CompatibilityMode = "true".equals(System.getProperty("v3compatibility"));
    public static final String WEB_SERVICE_NAMESPACE = "http://runa.ru/wfe";

    /**
     * Production or development mode?
     */
    public static boolean isDevMode() {
        return developmentMode;
    }

    /**
     * Process-level compatibility with version 3.x.
     */
    public static boolean isV3CompatibilityMode() {
        return v3CompatibilityMode;
    }

    public static String getAdministratorName() {
        return RESOURCES.getStringPropertyNotNull("default.administrator.name");
    }

    public static String getAdministratorsGroupName() {
        return RESOURCES.getStringPropertyNotNull("default.administrators.group.name");
    }

    public static String getBotsGroupName() {
        return RESOURCES.getStringPropertyNotNull("default.bots.group.name");
    }

    public static String getDateFormatPattern() {
        return RESOURCES.getStringPropertyNotNull("date.format.pattern");
    }

    public static boolean isLocalFileStorageEnabled() {
        return RESOURCES.getBooleanProperty("file.variable.local.storage.enabled", true);
    }

    public static String getLocalFileStoragePath() {
        return RESOURCES.getStringProperty("file.variable.local.storage.path", IOCommons.getInstallationDirPath() + "/filedata");
    }

    public static int getLocalFileStorageFileLimit() {
        return RESOURCES.getIntegerProperty("file.variable.local.storage.enableforfilesgreaterthan", 100000);
    }

}
