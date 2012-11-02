package ru.runa.wfe.definition.par;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.runa.wfe.commons.xml.ClasspathEntityResolver;
import ru.runa.wfe.commons.xml.LoggingErrorHandler;
import ru.runa.wfe.commons.xml.XMLHelper;
import ru.runa.wfe.definition.InvalidDefinitionException;

public class ValidationXmlParser {
    private final static String FIELD_ELEMENT_NAME = "field";
    private final static String FIELD_VALIDATOR_ELEMENT_NAME = "field-validator";
    private final static String NAME_ATTRIBUTE_NAME = "name";
    private final static String TYPE_ATTRIBUTE_NAME = "type";
    private final static String REQUIRED_VALIDATOR_NAME = "required";

    public static List<String> readVariableNames(byte[] xmlFileBytes) {
        try {
            Document document = XMLHelper.getDocumentWithoutValidation(new ByteArrayInputStream(xmlFileBytes));
            NodeList fieldElementsList = document.getElementsByTagName(FIELD_ELEMENT_NAME);

            List<String> varNames = new ArrayList<String>(fieldElementsList.getLength());
            for (int i = 0; i < fieldElementsList.getLength(); i++) {
                Element fieldElement = (Element) fieldElementsList.item(i);
                String varName = fieldElement.getAttribute(NAME_ATTRIBUTE_NAME);
                varNames.add(varName);
            }
            return varNames;
        } catch (Exception e) {
            throw new InvalidDefinitionException("${form}.validation.xml", e);
        }
    }

    public static List<String> readRequiredVariableNames(byte[] xmlFileBytes) {
        try {
            Document document = XMLHelper.getDocument(new ByteArrayInputStream(xmlFileBytes), ClasspathEntityResolver.getInstance(),
                    new LoggingErrorHandler(ValidationXmlParser.class));
            NodeList fieldElementsList = document.getElementsByTagName(FIELD_ELEMENT_NAME);
            List<String> varNames = new ArrayList<String>(fieldElementsList.getLength());
            for (int i = 0; i < fieldElementsList.getLength(); i++) {
                Element fieldElement = (Element) fieldElementsList.item(i);
                String varName = fieldElement.getAttribute(NAME_ATTRIBUTE_NAME);
                NodeList validatorElementsList = fieldElement.getElementsByTagName(FIELD_VALIDATOR_ELEMENT_NAME);
                for (int j = 0; j < validatorElementsList.getLength(); j++) {
                    Element validatorElement = (Element) validatorElementsList.item(j);
                    String typeName = validatorElement.getAttribute(TYPE_ATTRIBUTE_NAME);
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
