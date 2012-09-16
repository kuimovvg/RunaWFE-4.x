package ru.runa.bpm.ui.validation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.util.IOUtils;
import ru.runa.bpm.ui.util.ValidationUtil;
import ru.runa.bpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ValidatorParser {

    public static Map<String, Map<String, ValidatorConfig>> parseValidatorConfigs(IFile validationFile) {
        try {
            Map<String, Map<String, ValidatorConfig>> validatorCfgs = new HashMap<String, Map<String, ValidatorConfig>>();
            Document doc = XmlUtil.parseDocument(validationFile.getContents(true));
            NodeList fieldNodes = doc.getElementsByTagName("field");

            NodeList gValidatorNodes = doc.getElementsByTagName("validator");
            Map<String, ValidatorConfig> cfgs = new HashMap<String, ValidatorConfig>();
            validatorCfgs.put(ValidatorConfig.GLOBAL_FIELD_ID, cfgs);
            addValidatorConfigs(gValidatorNodes, cfgs, true);

            for (int i = 0; i < fieldNodes.getLength(); i++) {
                Element fieldElement = (Element) fieldNodes.item(i);
                String fieldName = fieldElement.getAttribute("name");

                NodeList validatorNodes = fieldElement.getElementsByTagName("field-validator");

                cfgs = new HashMap<String, ValidatorConfig>();
                validatorCfgs.put(fieldName, cfgs);
                addValidatorConfigs(validatorNodes, cfgs, false);
            }
            return validatorCfgs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static IFile writeValidatorXml(IFile validationFile, Map<String, Map<String, ValidatorConfig>> fieldConfigs) {
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<validators>\n");
            for (String fieldName : fieldConfigs.keySet()) {
                Map<String, ValidatorConfig> configs = fieldConfigs.get(fieldName);
                if (fieldName.length() > 0) {
                    buffer.append("\n<field name=\"").append(fieldName).append("\">\n");
                }
                for (ValidatorConfig config : configs.values()) {
                    ValidatorDefinition definition = ValidationUtil.getValidatorDefinition(config.getType());
                    if (definition != null) {
                        String xmlConfig = definition.formatConfig(config);
                        buffer.append(xmlConfig);
                    }
                }
                if (fieldName.length() > 0) {
                    buffer.append("\n</field>\n");
                }
            }
            buffer.append("\n</validators>");
            byte[] bytes = buffer.toString().getBytes(PluginConstants.UTF_ENCODING);
            InputStream is = new ByteArrayInputStream(bytes);
            if (!validationFile.exists()) {
                validationFile = IOUtils.createFileSafely(validationFile); 
            }
            validationFile.setContents(is, true, false, null);
            return validationFile;
        } catch (Exception e) {
            DesignerLogger.logError("Validation file update error", e);
            return null;
        }
    }

    private static void addValidatorConfigs(NodeList validatorNodes, Map<String, ValidatorConfig> validatorCfgs, boolean discriminate) {
        for (int j = 0; j < validatorNodes.getLength(); j++) {
            Element validatorElement = (Element) validatorNodes.item(j);
            String validatorType = validatorElement.getAttribute("type");
            Map<String, String> params = new HashMap<String, String>();
            NodeList paramNodes = validatorElement.getElementsByTagName("param");

            for (int k = 0; k < paramNodes.getLength(); k++) {
                Element paramElement = (Element) paramNodes.item(k);
                String paramName = paramElement.getAttribute("name");
                params.put(paramName, paramElement.getTextContent().trim());
            }

            NodeList messageNodes = validatorElement.getElementsByTagName("message");
            Element messageElement = (Element) messageNodes.item(0);
            Node messageNode = messageElement.getFirstChild();
            String message = messageNode != null ? messageNode.getNodeValue() : "";

            String validatorTypeKey = discriminate ? (validatorType + j) : validatorType;
            validatorCfgs.put(validatorTypeKey, new ValidatorConfig(validatorType, message, params));
        }
    }

}
