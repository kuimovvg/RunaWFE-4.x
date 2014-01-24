package ru.runa.wfe.commons;

import java.util.Calendar;

public class SystemProperties {
    private static final PropertyResources RESOURCES = new PropertyResources("system.properties");
    private static final boolean developmentMode = "true".equals(System.getProperty("devmode"));
    private static final boolean v3CompatibilityMode = "true".equals(System.getProperty("v3compatibility"));
    public static final String WEB_SERVICE_NAMESPACE = "http://runa.ru/wfe";

    public static final String RESOURCE_EXTENSION_PREFIX = "wfe.custom.";
    public static final String DEPRECATED_PREFIX = "deprecated.";
    public static final String startup = CalendarUtil.formatDateTime(Calendar.getInstance());

    public static PropertyResources getResources() {
        return RESOURCES;
    }

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

    /**
     * System version
     */
    public static String getVersion() {
        return RESOURCES.getStringProperty("version");
    }

    public static String getStartup() {
        return startup;
    }

    public static String getAdministratorName() {
        return RESOURCES.getStringPropertyNotNull("default.administrator.name");
    }

    public static String getAdministratorDefaultPassword() {
        return RESOURCES.getStringPropertyNotNull("default.administrator.password");
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
        return RESOURCES.getStringProperty("file.variable.local.storage.path", IOCommons.getAppServerDirPath() + "/wfe.filedata");
    }

    public static int getLocalFileStorageFileLimit() {
        return RESOURCES.getIntegerProperty("file.variable.local.storage.enableforfilesgreaterthan", 100000);
    }

    /**
     * @return value between 0..100 [%]
     */
    public static int getTaskAlmostDeadlineInPercents() {
        int percents = RESOURCES.getIntegerProperty("task.almostDeadlinePercents", 90);
        if (percents < 0 || percents > 100) {
            percents = 90;
        }
        return percents;
    }

    /**
     * Change this value sync with DB.
     * 
     * @return max string value
     */
    public static int getStringVariableValueLength() {
        return RESOURCES.getIntegerProperty("string.variable.length", 1024);
    }

    public static int getTokenMaximumDepth() {
        return RESOURCES.getIntegerProperty("token.maximum.depth", 100);
    }

    public static boolean isLDAPSynchronizationEnabled() {
        return RESOURCES.getBooleanProperty("ldap.synchronizer.enabled", false);
    }

    public static String getEARFileName() {
        return RESOURCES.getStringProperty("ear.filename", "runawfe.ear");
    }

    public static boolean isAllowedNotDefinedVariables() {
        return RESOURCES.getBooleanProperty("undefined.variables.allowed", false);
    }

    public static boolean isStrongVariableFormatEnabled() {
        return RESOURCES.getBooleanProperty("strong.variables.format.enabled", true);
    }

    public static boolean isVariableAutoCastingEnabled() {
        return RESOURCES.getBooleanProperty("variables.autocast.enabled", true);
    }

}
