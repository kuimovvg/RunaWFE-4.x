package ru.runa.gpd.formeditor.ftl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import ru.runa.gpd.formeditor.WYSIWYGPlugin;

public class FormatTag {
    public final String type;
    public final Map<String, FtlFormat> formats = new HashMap<String, FtlFormat>();

    public FormatTag(String type) {
        this.type = type;
    }

    public static class FtlFormat {
        public final String value;
        public final String name;

        public FtlFormat(String value, String name) {
            this.value = value;
            this.name = name;
        }
    }

    private static Map<String, FormatTag> ftlFormats;

    public static FormatTag getTag(String key) {
        if (!getAll().containsKey(key)) {
            throw new RuntimeException("FTL Format was not found for '" + key + "'");
        }
        return getAll().get(key);
    }

    private static Map<String, FormatTag> getAll() {
        if (ftlFormats == null) {
            loadInternal();
        }
        return ftlFormats;
    }

    private static void loadInternal() {
        try {
            ftlFormats = new HashMap<String, FormatTag>();
            IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.form.ftl.formats").getExtensions();
            for (IExtension extension : extensions) {
                IConfigurationElement[] tagElements = extension.getConfigurationElements();
                for (IConfigurationElement tagElement : tagElements) {
                    String name = tagElement.getAttribute("name");
                    FormatTag tag = new FormatTag(name);
                    IConfigurationElement[] paramElements = tagElement.getChildren();
                    for (IConfigurationElement paramElement : paramElements) {
                        String formatName = paramElement.getAttribute("name");
                        String formatValue = paramElement.getAttribute("value");
                        tag.formats.put(formatValue, new FtlFormat(formatValue, formatName));
                    }
                    ftlFormats.put(name, tag);
                }
            }
        } catch (Exception e) {
            WYSIWYGPlugin.logError("Unable to load FTL formats", e);
        }
    }
}
