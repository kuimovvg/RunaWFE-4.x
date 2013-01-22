package ru.runa.wfe.definition.par;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.InvalidDefinitionException;

public class ValidationXmlParser {
    private final static String FIELD_ELEMENT_NAME = "field";
    private final static String FIELD_VALIDATOR_ELEMENT_NAME = "field-validator";
    private final static String NAME_ATTRIBUTE_NAME = "name";
    private final static String TYPE_ATTRIBUTE_NAME = "type";
    private final static String REQUIRED_VALIDATOR_NAME = "required";

    public static List<String> readVariableNames(String fileName, byte[] xmlFileBytes) {
        try {
            Document document = XmlUtils.parseWithoutValidation(xmlFileBytes);
            List<Element> fieldElements = document.getRootElement().elements(FIELD_ELEMENT_NAME);
            List<String> varNames = new ArrayList<String>(fieldElements.size());
            for (Element fieldElement : fieldElements) {
                varNames.add(fieldElement.attributeValue(NAME_ATTRIBUTE_NAME));
            }
            return varNames;
        } catch (Exception e) {
            throw new InvalidDefinitionException("Error in " + fileName, e);
        }
    }

    public static List<String> readRequiredVariableNames(byte[] xmlFileBytes) {
        try {
            Document document = XmlUtils.parseWithoutValidation(xmlFileBytes);
            List<Element> fieldElements = document.getRootElement().elements(FIELD_ELEMENT_NAME);
            List<String> varNames = new ArrayList<String>(fieldElements.size());
            for (Element fieldElement : fieldElements) {
                String varName = fieldElement.attributeValue(NAME_ATTRIBUTE_NAME);
                List<Element> validatorElements = fieldElement.elements(FIELD_VALIDATOR_ELEMENT_NAME);
                for (Element validatorElement : validatorElements) {
                    String typeName = validatorElement.attributeValue(TYPE_ATTRIBUTE_NAME);
                    if (REQUIRED_VALIDATOR_NAME.equals(typeName)) {
                        varNames.add(varName);
                    }
                }
            }
            return varNames;
        } catch (Exception e) {
            throw new InvalidDefinitionException("${form}.validation.xml", e);
        }
    }
}
