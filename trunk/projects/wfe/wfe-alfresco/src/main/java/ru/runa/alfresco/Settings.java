package ru.runa.alfresco;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;

import ru.runa.wfe.WfException;
import ru.runa.wfe.commons.xml.XmlUtils;

/**
 * Base class for configurable items. Configuration of the system is extendible
 * through one XML file.
 * 
 * @author dofs
 */
public class Settings {
    protected static Log log = LogFactory.getLog(Settings.class);
    private static final String CONFIG_RESOURCE = "/alfwf.settings.xml";

    protected static Document getConfigDocument() throws Exception {
        InputStream is = Settings.class.getResourceAsStream(CONFIG_RESOURCE);
        if (is == null) {
            throw new WfException("No resource found in " + CONFIG_RESOURCE);
        }
        return XmlUtils.parseWithoutValidation(is);
    }

    protected static boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return "true".equals(value);
    }

    protected static int parseInt(String value, int defaultValue) {
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Unparsable int: " + value, e);
            return defaultValue;
        }
    }

}
