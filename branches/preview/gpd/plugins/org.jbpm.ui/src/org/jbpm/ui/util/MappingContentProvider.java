package ru.runa.bpm.ui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.DesignerPlugin;
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.common.model.Delegable;
import ru.runa.bpm.ui.custom.CustomizationRegistry;
import ru.runa.bpm.ui.orgfunctions.OrgFunctionDefinition;
import ru.runa.bpm.ui.orgfunctions.OrgFunctionsRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
            DesignerLogger.logError(e);
        }
    }

    private void writeOrg(OutputStream outputStream) {
        try {
            Document document = XmlUtil.createDocument(FUNCTIONS_ELEMENT_NAME, PluginConstants.ORG_FUNCTIONS_XSD_NAME);
            Element root = document.getDocumentElement();
            List<OrgFunctionDefinition> definitions = OrgFunctionsRegistry.getAllOrgFunctionDefinitions();
            for (OrgFunctionDefinition definition : definitions) {
                Element mappingElement = document.createElement(FUNCTION_ELEMENT_NAME);
                root.appendChild(mappingElement);
                mappingElement.setAttribute(CLASS_ATTRIBUTE_NAME, definition.getClassName());
                mappingElement.setAttribute(NAME_ATTRIBUTE_NAME, TypeNameMapping.getTypeName(definition.getClassName()));
            }
            XmlUtil.writeXml(document, outputStream);
        } catch (ParserConfigurationException e) {
            DesignerLogger.logError(e);
        } catch (TransformerException e) {
            DesignerLogger.logError(e);
        }
    }

    private void writeMapping(OutputStream outputStream) {
        try {
            Document document = XmlUtil.createDocument(MAPPING_ELEMENTS_NAME, PluginConstants.MAPPING_XSD_NAME);
            Element root = document.getDocumentElement();

            TypeNameMapping.getMapping();
            TypeNameMapping.getHiddenMapping();

            for (String key : TypeNameMapping.getMapping().keySet()) {
                Element mappingElement = document.createElement(MAPPING_ELEMENT_NAME);
                root.appendChild(mappingElement);
                mappingElement.setAttribute(FORMAT_ATTRIBUTE_NAME, key);
                mappingElement.setAttribute(NAME_ATTRIBUTE_NAME, TypeNameMapping.getTypeName(key));
                mappingElement.setAttribute(HIDDEN_ATTRIBUTE, "0");
            }

            for (String key : TypeNameMapping.getHiddenMapping().keySet()) {
                Element mappingElement = document.createElement(MAPPING_ELEMENT_NAME);
                root.appendChild(mappingElement);
                mappingElement.setAttribute(FORMAT_ATTRIBUTE_NAME, key);
                mappingElement.setAttribute(NAME_ATTRIBUTE_NAME, TypeNameMapping.getTypeName(key));
                mappingElement.setAttribute(HIDDEN_ATTRIBUTE, "1");
            }
            XmlUtil.writeXml(document, outputStream);
        } catch (ParserConfigurationException e) {
            DesignerLogger.logError(e);
        } catch (TransformerException e) {
            DesignerLogger.logError(e);
        }
    }

    public File getMappingFile() {
        return new File(DesignerPlugin.getPreferencesFolder(), MAPPING_XML_FILE_NAME);
    }

    public File getOrgFile() {
        return new File(DesignerPlugin.getPreferencesFolder(), ORG_XML_FILE_NAME);
    }

    public void addMappingInfo() {
        try {
            File file = getMappingFile();
            if (file.exists()) {
                Document document = XmlUtil.parseDocumentValidateXSD(new FileInputStream(file));
                NodeList mappingElementsList = document.getDocumentElement().getElementsByTagName(MAPPING_ELEMENT_NAME);
                Map<String, String> mapping = new HashMap<String, String>();
                Map<String, String> hiddenMapping = new HashMap<String, String>();
                for (int j = 0; j < mappingElementsList.getLength(); j++) {
                    Element variableElement = (Element) mappingElementsList.item(j);
                    String key = variableElement.getAttribute(FORMAT_ATTRIBUTE_NAME);
                    String value = variableElement.getAttribute(NAME_ATTRIBUTE_NAME);
                    String hidden = variableElement.getAttribute(HIDDEN_ATTRIBUTE);
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
            DesignerLogger.logError(e);
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
