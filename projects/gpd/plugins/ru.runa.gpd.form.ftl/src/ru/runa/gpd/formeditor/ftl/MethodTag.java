package ru.runa.gpd.formeditor.ftl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.PluginLogger;

public class MethodTag {
    private static final int DEFAULT_WIDTH = 250;
    private static final int DEFAULT_HEIGHT = 40;
    private final Bundle bundle;
    private boolean enabled;
    private final ITagImageProvider imageProvider;
    public final String id;
    public final String name;
    public final int width;
    public final int height;
    public final String helpPage;
    private final String imagePath;
    public final List<Param> params = new ArrayList<Param>();

    private MethodTag(Bundle bundle, boolean enabled, String tagName, String label, int width, int height, ITagImageProvider imageProvider,
            String imagePath, String helpPage) {
        this.bundle = bundle;
        this.enabled = enabled;
        this.id = tagName;
        this.name = label != null ? label : id;
        this.imageProvider = imageProvider;
        this.helpPage = helpPage;
        this.width = width;
        this.height = height;
        this.imagePath = imagePath;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public ITagImageProvider getImageProvider() {
        return imageProvider;
    }

    public boolean hasImage() {
        return imagePath != null;
    }

    public InputStream openImageStream() throws IOException {
        return new ByteArrayInputStream(EditorsPlugin.loadTagImage(bundle, imagePath));
    }

    @Override
    public String toString() {
        return id + " " + name + (enabled ? "" : " (disabled)");
    }

    public static class Param {
        private static final String TYPE_COMBO = "combo";
        private static final String TYPE_TEXT_OR_COMBO = "richcombo";
        private static final String TYPE_VAR_COMBO = "varcombo";
        private static final String TYPE_TEXT_FOR_ID_GENERATION = "textForIDGeneration";
        // private static final String TYPE_TEXT = "text";
        public final String typeName;
        public final VariableAccess variableAccess;
        public final String label;
        public final String help;
        public final boolean required;
        public final List<OptionalValue> optionalValues = new ArrayList<OptionalValue>();
        public final boolean multiple;
        public final boolean surroundBrackets;

        public Param(String typeName, VariableAccess variableAccess, String label, String help, boolean required, boolean multiple,
                boolean surroundBrackets) {
            this.typeName = typeName;
            this.variableAccess = variableAccess;
            this.label = label;
            this.help = help;
            this.required = required;
            this.multiple = multiple;
            this.surroundBrackets = surroundBrackets;
        }

        public boolean isCombo() {
            return TYPE_COMBO.equals(typeName);
        }

        public boolean isTextForIDGeneration() {
            return TYPE_TEXT_FOR_ID_GENERATION.equals(typeName);
        }

        public boolean isRichCombo() {
            return TYPE_TEXT_OR_COMBO.equals(typeName);
        }

        public boolean isVarCombo() {
            return TYPE_VAR_COMBO.equals(typeName);
        }

        public String getVariableTypeFilter() {
            if (!isVarCombo() && !isRichCombo()) {
                return null;
            }
            for (OptionalValue optionalValue : optionalValues) {
                if (optionalValue.filterType != null) {
                    return optionalValue.filterType;
                }
            }
            return null;
        }
    }

    public static enum VariableAccess {
        READ, WRITE, NONE
    }

    public static class OptionalValue {
        public final String name;
        public final String value;
        public final boolean container;
        public final boolean useFilter;
        public String filterType;

        public OptionalValue(String name, String value, boolean container) {
            this.name = name;
            this.value = value;
            this.container = container;
            this.useFilter = container && !Object.class.getName().equals(name);
            if (container && useFilter) {
                filterType = name;
            }
        }
    }

    private static class MethodTagComparator implements Comparator<MethodTag> {
        @Override
        public int compare(MethodTag t1, MethodTag t2) {
            return t1.name.compareTo(t2.name);
        }
    }

    private static Map<String, MethodTag> ftlMethods;

    public static MethodTag getTagNotNull(String key) {
        if (!hasTag(key)) {
            throw new RuntimeException("FTL Method not found for '" + key + "'");
        }
        return getAll().get(key);
    }

    public static boolean hasTag(String key) {
        return getAll().containsKey(key);
    }

    public static Map<String, MethodTag> getAll() {
        if (ftlMethods == null) {
            loadInternal();
        }
        return ftlMethods;
    }

    public static List<MethodTag> getEnabled() {
        List<MethodTag> tags = new ArrayList<MethodTag>();
        for (MethodTag tag : getAll().values()) {
            if (tag.enabled) {
                tags.add(tag);
            }
        }
        Collections.sort(tags, new MethodTagComparator());
        return tags;
    }

    private static void loadInternal() {
        try {
            ftlMethods = new HashMap<String, MethodTag>();
            IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.form.ftl.tags").getExtensions();
            if (EditorsPlugin.DEBUG) {
                PluginLogger.logInfo("ru.runa.gpd.form.ftl.tags extensions count: " + extensions.length);
            }
            for (IExtension extension : extensions) {
                Bundle bundle = Platform.getBundle(extension.getNamespaceIdentifier());
                if (EditorsPlugin.DEBUG) {
                    PluginLogger.logInfo("Loading extensions from " + bundle.getSymbolicName());
                }
                IConfigurationElement[] tagElements = extension.getConfigurationElements();
                for (IConfigurationElement tagElement : tagElements) {
                    String id = tagElement.getAttribute("id");
                    String name = tagElement.getAttribute("name");
                    try {
                        ITagImageProvider imageProvider;
                        if (tagElement.getAttribute("imageProvider") != null) {
                            imageProvider = (ITagImageProvider) tagElement.createExecutableExtension("imageProvider");
                        } else {
                            imageProvider = new DefaultTagImageProvider();
                        }
                        String image = tagElement.getAttribute("image");
                        String helpPage = tagElement.getAttribute("help_page");
                        int width = getIntAttr(tagElement, "width", DEFAULT_WIDTH);
                        int height = getIntAttr(tagElement, "height", DEFAULT_HEIGHT);
                        boolean enabled = getBooleanAttr(tagElement, "enabled", false);
                        MethodTag tag = new MethodTag(bundle, enabled, id, name, width, height, imageProvider, image, helpPage);
                        IConfigurationElement[] paramElements = tagElement.getChildren();
                        for (IConfigurationElement paramElement : paramElements) {
                            String paramName = paramElement.getAttribute("name");
                            String paramType = paramElement.getAttribute("type");
                            VariableAccess variableAccess = VariableAccess.valueOf(paramElement.getAttribute("variableAccess"));

                            boolean required = Boolean.valueOf(paramElement.getAttribute("required"));
                            String helpAttr = paramElement.getAttribute("help");
                            String help = null;
                            if (helpAttr != null && !helpAttr.isEmpty()) {
                                help = helpAttr.trim();
                            }
                            boolean multiple = getBooleanAttr(paramElement, "multiple", false);
                            boolean surroundBrackets = getBooleanAttr(paramElement, "surroundBrackets", false);

                            Param param = new Param(paramType, variableAccess, paramName, help, required, multiple, surroundBrackets);

                            String paramValues = paramElement.getAttribute("variableTypeFilter");
                            if (paramValues != null && paramValues.length() > 0) {
                                param.optionalValues.add(new OptionalValue(paramValues, null, true));
                            }
                            IConfigurationElement[] paramValueElements = paramElement.getChildren();
                            for (IConfigurationElement paramValueElement : paramValueElements) {
                                String pvName = paramValueElement.getAttribute("name");
                                String pvValue = paramValueElement.getAttribute("value");
                                param.optionalValues.add(new OptionalValue(pvValue, pvName, false));
                            }
                            tag.params.add(param);
                        }
                        if (EditorsPlugin.DEBUG) {
                            PluginLogger.logInfo("Registering " + tag);
                        }
                        if (ftlMethods.containsKey(id)) {
                            MethodTag oldTag = ftlMethods.get(id);
                            if (!oldTag.enabled || !tag.enabled) {
                                oldTag.enabled = false;
                                tag.enabled = false;
                            }
                            tag = tag.params.size() > oldTag.params.size() ? tag : oldTag;
                        }
                        ftlMethods.put(id, tag);
                    } catch (Exception e) {
                        EditorsPlugin.logError("Unable to load FTL method " + name, e);
                    }
                }
            }
        } catch (Exception e) {
            EditorsPlugin.logError("Unable to load FTL methods", e);
        }
    }

    private static int getIntAttr(IConfigurationElement element, String attrName, int defaultValue) {
        String attr = element.getAttribute(attrName);
        if (attr == null) {
            return defaultValue;
        }
        return Integer.parseInt(attr);
    }

    private static boolean getBooleanAttr(IConfigurationElement element, String attrName, boolean defaultValue) {
        String attr = element.getAttribute(attrName);
        if (attr == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(attr);
    }
}
