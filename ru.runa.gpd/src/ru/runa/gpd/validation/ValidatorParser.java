package ru.runa.gpd.validation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.ValidationUtil;
import ru.runa.gpd.util.XmlUtil;

public class ValidatorParser {
    public static Map<String, Map<String, ValidatorConfig>> parseValidatorConfigs(IFile validationFile) {
        try {
            Map<String, Map<String, ValidatorConfig>> validatorCfgs = new HashMap<String, Map<String, ValidatorConfig>>();
            Document doc = XmlUtil.parseWithoutValidation(validationFile.getContents(true));
            List<Element> fieldNodes = doc.getRootElement().elements("field");
            List<Element> gValidatorNodes = doc.getRootElement().elements("validator");
            Map<String, ValidatorConfig> cfgs = new HashMap<String, ValidatorConfig>();
            validatorCfgs.put(ValidatorConfig.GLOBAL_FIELD_ID, cfgs);
            addValidatorConfigs(gValidatorNodes, cfgs, true);
            for (Element fieldElement : fieldNodes) {
                String fieldName = fieldElement.attributeValue("name");
                List<Element> validatorNodes = fieldElement.elements("field-validator");
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
            PluginLogger.logError("Validation file update error", e);
            return null;
        }
    }

    private static void addValidatorConfigs(List<Element> validatorNodes, Map<String, ValidatorConfig> validatorCfgs, boolean discriminate) {
        int j = 0;
        for (Element validatorElement : validatorNodes) {
            String validatorType = validatorElement.attributeValue("type");
            Map<String, String> params = new HashMap<String, String>();
            List<Element> paramNodes = validatorElement.elements("param");
            for (Element paramElement : paramNodes) {
                String paramName = paramElement.attributeValue("name");
                params.put(paramName, paramElement.getTextTrim());
            }
            Element messageElement = validatorElement.element("message");
            String message = messageElement != null ? messageElement.getTextTrim() : "";
            String validatorTypeKey = discriminate ? (validatorType + j) : validatorType;
            validatorCfgs.put(validatorTypeKey, new ValidatorConfig(validatorType, message, params));
            j++;
        }
    }
}
