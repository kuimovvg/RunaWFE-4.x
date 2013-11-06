package ru.runa.gpd.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class NodeRegistry {
    private static Map<String, NodeTypeDefinition> typesByModelClass = Maps.newHashMap();
    private static List<NodeTypeDefinition> definitions = Lists.newArrayList();
    static {
        processJpdlElements();
    }

    private static void processJpdlElements() {
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.elements").getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                try {
                    processConfigElement(configElement);
                } catch (Exception e) {
                    PluginLogger.logError("Error processing ru.runa.gpd.elements extension", e);
                }
            }
        }
    }

    private static void processConfigElement(IConfigurationElement configElement) throws CoreException {
        if (!configElement.getName().equals("element")) {
            throw new RuntimeException("unknown config element: " + configElement.getName());
        }
        NodeTypeDefinition type = new NodeTypeDefinition(configElement);
        typesByModelClass.put(configElement.getAttribute("model"), type);
        definitions.add(type);
    }

    public static List<NodeTypeDefinition> getDefinitions() {
        return definitions;
    }

    public static NodeTypeDefinition getNodeTypeDefinition(Language language, String name) {
        for (NodeTypeDefinition definition : typesByModelClass.values()) {
            if (language == Language.JPDL && Objects.equal(definition.getJpdlElementName(), name)) {
                return definition;
            }
            if (language == Language.BPMN && Objects.equal(definition.getBpmnElementName(), name)) {
                return definition;
            }
        }
        throw new RuntimeException("No type found by name " + name);
    }

    public static NodeTypeDefinition getNodeTypeDefinition(Class<? extends GraphElement> clazz) {
        if (!typesByModelClass.containsKey(clazz.getName())) {
            throw new RuntimeException("No type found by class " + clazz);
        }
        return typesByModelClass.get(clazz.getName());
    }

    public static List<NodeTypeDefinition> getTypesWithVariableRenameProvider() {
        List<NodeTypeDefinition> list = new ArrayList<NodeTypeDefinition>();
        for (NodeTypeDefinition definition : typesByModelClass.values()) {
            if (definition.hasVariableRenameProvider()) {
                list.add(definition);
            }
        }
        return list;
    }

    public static ProcessDefinition parseProcessDefinition(IFile definitionFile) throws Exception {
        // Workaround for 'resource out of sync'
        definitionFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
        Document document = XmlUtil.parseWithoutValidation(definitionFile.getContents());
        for (Language language : Language.values()) {
            if (language.getSerializer().isSupported(document)) {
                ProcessDefinition definition = language.getSerializer().parseXML(document);
                definition.setLanguage(language);
                return definition;
            }
        }
        throw new RuntimeException("No language could be determined for this content");
    }
}
