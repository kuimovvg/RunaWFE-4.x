package ru.runa.gpd;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Localization {
    private static final String BUNDLE_NAME = "plugin";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Localization() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            System.out.println("Missed localization: '" + key + "'");
            return key;
        }
    }
}
