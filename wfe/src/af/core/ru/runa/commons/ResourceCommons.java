/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.commons;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Created on 09.08.2004
 * 
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public abstract class ResourceCommons {

    protected final String resourceBundleName;
    protected final byte[] resourceProperties;

    /*
     * public ResourceCommons(){ bundleName = null; properties = null; }
     */
    public ResourceCommons(String bundleName) {
        resourceBundleName = bundleName;
        resourceProperties = null;
    }

    public ResourceCommons(byte[] properties) {
        resourceBundleName = null;
        resourceProperties = properties;
    }

    protected ResourceBundle getResourceBundle(Locale locale) {
        try {
            if (resourceBundleName != null) {
                return ResourceBundle.getBundle(resourceBundleName, locale);
            }
            if (resourceProperties != null) {
                return new PropertyResourceBundle(new ByteArrayInputStream(resourceProperties));
            }
        } catch (IOException e) {
            throw new MissingResourceException("CommonResources error: can't create ResourceBundle via properties array: " + e.getMessage(), null,
                    null);
        }
        throw new MissingResourceException("CommonResources error: no property file or property array found.", null, null);
    }

    protected ResourceBundle getResourceBundle() {
        try {
            if (resourceBundleName != null) {
                return ResourceBundle.getBundle(resourceBundleName);
            }
            if (resourceProperties != null) {
                return new PropertyResourceBundle(new ByteArrayInputStream(resourceProperties));
            }
        } catch (IOException e) {
            throw new MissingResourceException("CommonResources error: can't create ResourceBundle via properties array: " + e.getMessage(), null,
                    null);
        }
        throw new MissingResourceException("CommonResources error: no property file or property array found.", null, null);
    }

    protected Enumeration<String> getKeys(Locale locale) {
        return getResourceBundle(locale).getKeys();
    }

    protected String readProperty(String propertyName, Locale locale) {
        String value = getResourceBundle(locale).getString(propertyName);
        if (value == null) {
            throw new MissingResourceException("Property not found", resourceBundleName, propertyName);
        }
        return value;
    }

    protected Enumeration<String> getKeys() {
        return getResourceBundle().getKeys();
    }

    protected String readProperty(String propertyName) {
        String value = getResourceBundle().getString(propertyName);
        if (value == null) {
            throw new MissingResourceException("Property not found", resourceBundleName, propertyName);
        }
        return value;
    }

    public String readPropertyIfExist(String propertyName) {
        String result = null;
        for (Enumeration<String> e = getResourceBundle().getKeys(); e.hasMoreElements();) {
            if (e.nextElement().equals(propertyName)) {
                result = getResourceBundle().getString(propertyName);
                break;
            }
        }
        return result;
    }

    public static String getServerId() {
        try {
            String result = ResourceBundle.getBundle("wfe_settings").getString("workflow.server.id");
            return result == null ? "server" : result;
        } catch (Throwable e) {
            return "server";
        }
    }

    protected static Enumeration<String> getKeys(String bundleName, Locale locale) {
        return ResourceBundle.getBundle(bundleName, locale).getKeys();
    }

    protected static String readProperty(String propertyName, String bundleName, Locale locale) {
        String value = ResourceBundle.getBundle(bundleName, locale).getString(propertyName);
        if (value == null) {
            throw new MissingResourceException("Property not found", bundleName, propertyName);
        }
        return value;
    }

    protected static Enumeration<String> getKeys(String bundleName) {
        return ResourceBundle.getBundle(bundleName).getKeys();
    }

    protected static String readProperty(String propertyName, String bundleName) {
        String value = ResourceBundle.getBundle(bundleName).getString(propertyName);
        if (value == null) {
            throw new MissingResourceException("Property not found", bundleName, propertyName);
        }
        return value;
    }

    protected static String readPropertyIfExist(String propertyName, String bundleName) {
        return readPropertyIfExist(propertyName, bundleName, null);
    }

    protected static String readPropertyIfExist(String propertyName, String bundleName, String defaultValue) {
        for (Enumeration<String> e = ResourceBundle.getBundle(bundleName).getKeys(); e.hasMoreElements();) {
            if (e.nextElement().equals(propertyName)) {
                return ResourceBundle.getBundle(bundleName).getString(propertyName);
            }
        }
        return defaultValue;
    }
}
