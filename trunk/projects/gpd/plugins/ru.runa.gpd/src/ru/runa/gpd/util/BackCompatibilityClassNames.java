package ru.runa.gpd.util;

// TODO remove if way of importing wfe packages in downstream plugins will be found
public class BackCompatibilityClassNames {
    public static String getClassName(java.lang.String className) {
        return ru.runa.wfe.commons.BackCompatibilityClassNames.getClassName(className);
    }
}
