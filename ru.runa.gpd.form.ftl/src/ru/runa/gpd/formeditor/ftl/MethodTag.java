package ru.runa.gpd.formeditor.ftl;

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
    private final Bundle bundle;
    private boolean enabled;
    private final ITagImageProvider imageProvider;
    public final String id;
    public final String name;
    public final List<Param> params = new ArrayList<Param>();

    private MethodTag(Bundle bundle, boolean enabled, String tagName, String label, ITagImageProvider imageProvider) {
        this.bundle = bundle;
        this.enabled = enabled;
        this.id = tagName;
        this.name = label != null ? label : id;
        this.imageProvider = imageProvider;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public ITagImageProvider getImageProvider() {
        return imageProvider;
    }

    @Override
    public String toString() {
        return id + " " + name + (enabled ? "" : " (disabled)");
    }

    public static class Param {
        private static final String TYPE_COMBO = "combo";
        private static final String TYPE_TEXT_OR_COMBO = "richcombo";
        private static final String TYPE_VAR_COMBO = "varcombo";
        // private static final String TYPE_TEXT = "text";
        public final String typeName;
        public final VariableAccess variableAccess;
        public final String label;
        public final List<OptionalValue> optionalValues = new ArrayList<OptionalValue>();
        public final boolean multiple;

        public Param(String typeName, VariableAccess variableAccess, String label, boolean multiple) {
            this.typeName = typeName;
            this.variableAccess = variableAccess;
            this.label = label;
            this.multiple = multiple;
        }

        public boolean isCombo() {
            return TYPE_COMBO.equals(typeName);
        }

        public boolean isRichCombo() {
            return TYPE_TEXT_OR_COMBO.equals(typeName);
        }

        public boolean isVarCombo() {
            return TYPE_VAR_COMBO.equals(typeName);
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
                        boolean enabled = getBooleanAttr(tagElement, "enabled", false);
                        MethodTag tag = new MethodTag(bundle, enabled, id, name, imageProvider);
                        IConfigurationElement[] paramElements = tagElement.getChildren();
                        for (IConfigurationElement paramElement : paramElements) {
                            String paramName = paramElement.getAttribute("name");
                            String paramType = paramElement.getAttribute("type");
                            VariableAccess variableAccess = VariableAccess.valueOf(paramElement.getAttribute("variableAccess"));
                            boolean multiple = getBooleanAttr(paramElement, "multiple", false);
                            Param param = new Param(paramType, variableAccess, paramName, multiple);
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

    private static boolean getBooleanAttr(IConfigurationElement element, String attrName, boolean defaultValue) {
        String attr = element.getAttribute(attrName);
        if (attr == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(attr);
    }
}
