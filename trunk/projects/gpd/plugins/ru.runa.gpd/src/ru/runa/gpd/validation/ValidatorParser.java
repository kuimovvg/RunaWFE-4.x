package ru.runa.gpd.validation;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.ValidationUtil;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Strings;

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

    public static void writeValidatorXml(IFile validationFile, Map<String, Map<String, ValidatorConfig>> fieldConfigs) {
        try {
            Document document = XmlUtil.createDocument("validators");
            for (Map.Entry<String, Map<String, ValidatorConfig>> config : fieldConfigs.entrySet()) {
                Element parentElement = document.getRootElement();
                if (!Strings.isNullOrEmpty(config.getKey())) {
                    parentElement = parentElement.addElement("field");
                    parentElement.addAttribute("name", config.getKey());
                }
                for (ValidatorConfig validatorConfig : config.getValue().values()) {
                    ValidatorDefinition definition = ValidationUtil.getValidatorDefinition(validatorConfig.getType());
                    if (definition != null) {
                        definition.writeConfig(parentElement, validatorConfig);
                    }
                }
            }
            if (!validationFile.exists()) {
                IOUtils.createFile(validationFile);
            }
            byte[] bytes = XmlUtil.writeXml(document);
            validationFile.setContents(new ByteArrayInputStream(bytes), true, false, null);
        } catch (Exception e) {
            PluginLogger.logError("Validation file update error", e);
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
