package org.jbpm.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.jbpm.ui.common.ElementEntry;
import org.jbpm.ui.common.ElementTypeDefinition;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.util.ProjectFinder;
import org.jbpm.ui.util.XmlUtil;
import org.w3c.dom.Document;

public class JpdlVersionRegistry {
    private static Map<String, Map<String, ElementTypeDefinition>> types = new HashMap<String, Map<String, ElementTypeDefinition>>();

    private static Map<String, Map<String, Map<String, ElementTypeDefinition>>> palette = new TreeMap<String, Map<String, Map<String, ElementTypeDefinition>>>();

    private static Map<String, JpdlSerializer> jpdlContentProviders = new HashMap<String, JpdlSerializer>();

    private static List<String> jpdlVersions = new ArrayList<String>();
    static {
        processJpdlElements();
    }

    private static void processJpdlElements() {
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("org.jbpm.ui.elements").getExtensions();
        for (IExtension extension : extensions) {
            String jpdlVersion = extension.getLabel();
            Map<String, ElementTypeDefinition> typesMap = types.get(jpdlVersion);
            if (typesMap == null) {
                typesMap = new HashMap<String, ElementTypeDefinition>();
                types.put(jpdlVersion, typesMap);
            }
            Map<String, Map<String, ElementTypeDefinition>> paletteMap = palette.get(jpdlVersion);
            if (paletteMap == null) {
                paletteMap = new TreeMap<String, Map<String, ElementTypeDefinition>>();
                palette.put(jpdlVersion, paletteMap);
            }
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                try {
                    if (configElement.getName().equals("contentProvider")) {
                        JpdlSerializer contentProvider = (JpdlSerializer) configElement.createExecutableExtension("serializerClass");
                        contentProvider.setJpdlVersion(jpdlVersion);
                        jpdlVersions.add(jpdlVersion);
                        jpdlContentProviders.put(jpdlVersion, contentProvider);
                        continue;
                    }
                    processConfigElement(configElement, typesMap, paletteMap);
                } catch (Exception e) {
                    DesignerLogger.logError("Error processing org.jbpm.ui.elements extension", e);
                }
            }
        }
    }

    private static void processConfigElement(IConfigurationElement configElement, Map<String, ElementTypeDefinition> typesMap,
            Map<String, Map<String, ElementTypeDefinition>> paletteMap) throws CoreException {
        if (!configElement.getName().equals("element")) {
            throw new RuntimeException("unknown config element: " + configElement.getName());
        }
        ElementTypeDefinition type = new ElementTypeDefinition(configElement);
        typesMap.put(type.getName(), type);
        ElementEntry entry = type.getEntry();
        if (entry == null) {
            return;
        }
        String categoryId = entry.getCategoryId();
        Map<String, ElementTypeDefinition> category = paletteMap.get(categoryId);
        if (category == null) {
            category = new TreeMap<String, ElementTypeDefinition>();
            paletteMap.put(categoryId, category);
        }
        category.put(entry.getId(), type);
    }

    public static Set<String> getPaletteCategories(String jpdlVersion) {
        return palette.get(jpdlVersion).keySet();
    }

    public static Map<String, ElementTypeDefinition> getPaletteEntriesFor(String jpdlVersion, String categoryName) {
        return palette.get(jpdlVersion).get(categoryName);
    }

    public static ElementTypeDefinition getElementTypeDefinition(String jpdlVersion, String name) {
        return types.get(jpdlVersion).get(name);
    }

    public static List<ElementSerializer> getElementSerializers(String jpdlVersion) {
        List<ElementSerializer> list = new ArrayList<ElementSerializer>();
        for (ElementTypeDefinition definition : types.get(jpdlVersion).values()) {
            if (definition.getSerializer() != null) {
                list.add(definition.getSerializer());
            }
        }
        return list;
    }

    public static List<ElementTypeDefinition> getTypesWithVariableRenameProvider(String jpdlVersion) {
        List<ElementTypeDefinition> list = new ArrayList<ElementTypeDefinition>();
        for (ElementTypeDefinition definition : types.get(jpdlVersion).values()) {
            if (definition.hasVariableRenameProvider()) {
                list.add(definition);
            }
        }
        return list;
    }

    public static List<String> getAllJpdlVersions() {
        return jpdlVersions;
    }

    public static JpdlSerializer getContentProvider(String jpdlVersion) {
        return jpdlContentProviders.get(jpdlVersion);
    }

    public static ProcessDefinition parseProcessDefinition(IFile file) throws Exception {
        // Workaround to resource out of sync
        ProjectFinder.refreshProcessFolder(file);
        Document document = XmlUtil.parseDocument(file.getContents());
        String jpdlVersion = identifyJpdlVersionByContent(document);
        ProcessDefinition definition = getContentProvider(jpdlVersion).parseXML(document);
        definition.setJpdlVersion(jpdlVersion);
        return definition;
    }

    private static String identifyJpdlVersionByContent(Document document) {
        for (String jpdlVersion : jpdlVersions) {
            JpdlSerializer contentProvider = jpdlContentProviders.get(jpdlVersion);
            if (contentProvider.isSupported(document)) {
                return jpdlVersion;
            }
        }
        throw new RuntimeException("Jpdl version not found for this content");
    }

}
