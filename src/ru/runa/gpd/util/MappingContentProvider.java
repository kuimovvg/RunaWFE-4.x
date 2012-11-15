package ru.runa.gpd.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.handler.CustomizationRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.orgfunction.OrgFunctionsRegistry;

public class MappingContentProvider {
    public static final MappingContentProvider INSTANCE = new MappingContentProvider();
    public static final String MAPPING_XML_FILE_NAME = "mapping.xml";
    private static final String MAPPING_ELEMENTS_NAME = "typesMapping";
    private static final String MAPPING_ELEMENT_NAME = "mapping";
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String HIDDEN_ATTRIBUTE = "hidden";
    private static final String FORMAT_ATTRIBUTE_NAME = "format";
    public static final String ORG_XML_FILE_NAME = "orgfunctions.xml";
    private static final String FUNCTIONS_ELEMENT_NAME = "functions";
    private static final String FUNCTION_ELEMENT_NAME = "function";
    private static final String CLASS_ATTRIBUTE_NAME = "function";

    public void saveToInput() {
        try {
            File mappingFile = getMappingFile();
            if (!mappingFile.exists()) {
                mappingFile.createNewFile();
            }
            writeMapping(new FileOutputStream(mappingFile));
            File orgFile = getOrgFile();
            if (!orgFile.exists()) {
                orgFile.createNewFile();
            }
            writeOrg(new FileOutputStream(orgFile));
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    private void writeOrg(OutputStream outputStream) {
        try {
            Document document = XmlUtil.createDocument(FUNCTIONS_ELEMENT_NAME);
            Element root = document.getRootElement();
            List<OrgFunctionDefinition> definitions = OrgFunctionsRegistry.getAllOrgFunctionDefinitions();
            for (OrgFunctionDefinition definition : definitions) {
                Element mappingElement = root.addElement(FUNCTION_ELEMENT_NAME);
                mappingElement.addAttribute(CLASS_ATTRIBUTE_NAME, definition.getClassName());
                mappingElement.addAttribute(NAME_ATTRIBUTE_NAME, TypeNameMapping.getTypeName(definition.getClassName()));
            }
            XmlUtil.writeXml(document, outputStream);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    private void writeMapping(OutputStream outputStream) {
        try {
            Document document = XmlUtil.createDocument(MAPPING_ELEMENTS_NAME);
            Element root = document.getRootElement();
            TypeNameMapping.getMapping();
            TypeNameMapping.getHiddenMapping();
            for (String key : TypeNameMapping.getMapping().keySet()) {
                Element mappingElement = root.addElement(MAPPING_ELEMENT_NAME);
                mappingElement.addAttribute(FORMAT_ATTRIBUTE_NAME, key);
                mappingElement.addAttribute(NAME_ATTRIBUTE_NAME, TypeNameMapping.getTypeName(key));
                mappingElement.addAttribute(HIDDEN_ATTRIBUTE, "0");
            }
            for (String key : TypeNameMapping.getHiddenMapping().keySet()) {
                Element mappingElement = root.addElement(MAPPING_ELEMENT_NAME);
                mappingElement.addAttribute(FORMAT_ATTRIBUTE_NAME, key);
                mappingElement.addAttribute(NAME_ATTRIBUTE_NAME, TypeNameMapping.getTypeName(key));
                mappingElement.addAttribute(HIDDEN_ATTRIBUTE, "1");
            }
            XmlUtil.writeXml(document, outputStream);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    public File getMappingFile() {
        return new File(Activator.getPreferencesFolder(), MAPPING_XML_FILE_NAME);
    }

    public File getOrgFile() {
        return new File(Activator.getPreferencesFolder(), ORG_XML_FILE_NAME);
    }

    public void addMappingInfo() {
        try {
            File file = getMappingFile();
            if (file.exists()) {
                Document document = XmlUtil.parseWithoutValidation(new FileInputStream(file));
                List<Element> mappingElementsList = document.getRootElement().elements(MAPPING_ELEMENT_NAME);
                Map<String, String> mapping = new HashMap<String, String>();
                Map<String, String> hiddenMapping = new HashMap<String, String>();
                for (Element variableElement : mappingElementsList) {
                    String key = variableElement.attributeValue(FORMAT_ATTRIBUTE_NAME);
                    String value = variableElement.attributeValue(NAME_ATTRIBUTE_NAME);
                    String hidden = variableElement.attributeValue(HIDDEN_ATTRIBUTE);
                    if ("1".equals(hidden)) {
                        hiddenMapping.put(key, value);
                    } else {
                        mapping.put(key, value);
                    }
                }
                TypeNameMapping.setMapping(mapping);
                TypeNameMapping.setHiddenMapping(hiddenMapping);
            } else {
                Map<String, String> mapping = new HashMap<String, String>();
                addDefault(mapping);
                TypeNameMapping.setMapping(mapping);
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    private void addDefault(Map<String, String> mapping) {
        addMappingsForType(mapping, Delegable.ACTION_HANDLER);
        addMappingsForType(mapping, Delegable.DECISION_HANDLER);
        addMappingsForType(mapping, Delegable.ASSIGNMENT_HANDLER);
        addMappingsForType(mapping, "FORMAT"); // TODO ???
        List<OrgFunctionDefinition> definitions = OrgFunctionsRegistry.getAllOrgFunctionDefinitions();
        for (OrgFunctionDefinition definition : definitions) {
            addMappingForClass(mapping, definition.getClassName());
        }
    }

    private void addMappingsForType(Map<String, String> mapping, String type) {
        for (String typeName : CustomizationRegistry.getHandlerClasses(type)) {
            addMappingForClass(mapping, typeName);
        }
    }

    private void addMappingForClass(Map<String, String> mapping, String className) {
        int dli = className.lastIndexOf(".");
        String shortTypeName;
        if (dli > 0) {
            shortTypeName = className.substring(dli + 1);
        } else {
            shortTypeName = className;
        }
        mapping.put(className, shortTypeName);
    }
}
