package ru.runa.gpd.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import com.google.common.collect.Maps;

public class LocalizationsProvider {
    public static final LocalizationsProvider INSTANCE = new LocalizationsProvider();
    public static final String MAPPING_XML_FILE_NAME = "localizations.xml";
    private static final String ROOT_ELEMENT = "localizations";
    private static final String MESSAGE_NODE = "message";
    private static final String NAME_ATTR = "name";
    private static final String VALUE_ATTR = "value";
    private static final String HIDDEN_ATTR = "hidden";

    private File getMappingFile() {
        return new File(Activator.getPreferencesFolder(), MAPPING_XML_FILE_NAME);
    }

    public void save() {
        try {
            File mappingFile = getMappingFile();
            if (!mappingFile.exists()) {
                mappingFile.createNewFile();
            }
            Document document = XmlUtil.createDocument(ROOT_ELEMENT);
            Element root = document.getRootElement();
            LocalizationRegistry.getMapping();
            LocalizationRegistry.getHiddenMapping();
            for (String key : LocalizationRegistry.getMapping().keySet()) {
                Element mappingElement = root.addElement(MESSAGE_NODE);
                mappingElement.addAttribute(NAME_ATTR, key);
                mappingElement.addAttribute(VALUE_ATTR, LocalizationRegistry.getTypeName(key));
                mappingElement.addAttribute(HIDDEN_ATTR, "0");
            }
            for (String key : LocalizationRegistry.getHiddenMapping().keySet()) {
                Element mappingElement = root.addElement(MESSAGE_NODE);
                mappingElement.addAttribute(NAME_ATTR, key);
                mappingElement.addAttribute(VALUE_ATTR, LocalizationRegistry.getTypeName(key));
                mappingElement.addAttribute(HIDDEN_ATTR, "1");
            }
            List<OrgFunctionDefinition> definitions = OrgFunctionsRegistry.getAllOrgFunctionDefinitions();
            for (OrgFunctionDefinition definition : definitions) {
                Element mappingElement = root.addElement(MESSAGE_NODE);
                mappingElement.addAttribute(NAME_ATTR, definition.getClassName());
                mappingElement.addAttribute(VALUE_ATTR, LocalizationRegistry.getTypeName(definition.getClassName()));
            }
            XmlUtil.writeXml(document, new FileOutputStream(mappingFile));
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    public void init() {
        try {
            File file = getMappingFile();
            if (file.exists()) {
                Document document = XmlUtil.parseWithoutValidation(new FileInputStream(file));
                List<Element> mappingElementsList = document.getRootElement().elements(MESSAGE_NODE);
                Map<String, String> mapping = new HashMap<String, String>();
                Map<String, String> hiddenMapping = new HashMap<String, String>();
                for (Element variableElement : mappingElementsList) {
                    String key = variableElement.attributeValue(NAME_ATTR);
                    String value = variableElement.attributeValue(VALUE_ATTR);
                    String hidden = variableElement.attributeValue(HIDDEN_ATTR);
                    if ("1".equals(hidden)) {
                        hiddenMapping.put(key, value);
                    } else {
                        mapping.put(key, value);
                    }
                }
                LocalizationRegistry.setMapping(mapping);
                LocalizationRegistry.setHiddenMapping(hiddenMapping);
            } else {
                Map<String, String> localizations = Maps.newHashMap();
                addDefaultLocalizations(localizations);
                LocalizationRegistry.setMapping(localizations);
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    private void addDefaultLocalizations(Map<String, String> localizations) {
        addLocalizationsForType(localizations, Delegable.ACTION_HANDLER);
        addLocalizationsForType(localizations, Delegable.DECISION_HANDLER);
        addLocalizationsForType(localizations, Delegable.ASSIGNMENT_HANDLER);
        addLocalizationsForType(localizations, "FORMAT"); // TODO ???
        List<OrgFunctionDefinition> definitions = OrgFunctionsRegistry.getAllOrgFunctionDefinitions();
        for (OrgFunctionDefinition definition : definitions) {
            addLocalization(localizations, definition.getClassName());
        }
    }

    private void addLocalizationsForType(Map<String, String> localizations, String type) {
        for (String typeName : CustomizationRegistry.getHandlerClasses(type)) {
            addLocalization(localizations, typeName);
        }
    }

    private void addLocalization(Map<String, String> localizations, String className) {
        int dli = className.lastIndexOf(".");
        String shortTypeName;
        if (dli > 0) {
            shortTypeName = className.substring(dli + 1);
        } else {
            shortTypeName = className;
        }
        localizations.put(className, shortTypeName);
    }
}
