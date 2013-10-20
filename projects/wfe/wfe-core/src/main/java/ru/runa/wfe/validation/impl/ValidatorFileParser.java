/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.validation.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.validation.ValidatorConfig;

import com.google.common.collect.Maps;

public class ValidatorFileParser {

    public static Map<String, String> parseValidatorDefinitions(InputStream is) {
        Map<String, String> result = Maps.newHashMap();
        Document document = XmlUtils.parseWithoutValidation(is);
        List<Element> nodes = document.getRootElement().elements("validator");
        for (Element validatorElement : nodes) {
            String name = validatorElement.attributeValue("name");
            String className = validatorElement.attributeValue("class");
            result.put(name, className);
        }
        return result;
    }

    public static List<ValidatorConfig> parseValidatorConfigs(byte[] validationXml) {
        List<ValidatorConfig> configs = new ArrayList<ValidatorConfig>();
        Document doc = XmlUtils.parseWithoutValidation(validationXml);
        List<Element> fieldElements = doc.getRootElement().elements("field");
        List<Element> validatorElements = doc.getRootElement().elements("validator");
        addValidatorConfigs(validatorElements, new HashMap<String, String>(), configs);
        for (Element fieldElement : fieldElements) {
            String fieldName = fieldElement.attributeValue("name");
            Map<String, String> extraParams = new HashMap<String, String>();
            extraParams.put("fieldName", fieldName);
            validatorElements = fieldElement.elements("field-validator");
            addValidatorConfigs(validatorElements, extraParams, configs);
        }
        return configs;
    }

    private static void addValidatorConfigs(List<Element> validatorElements, Map<String, String> extraParams, List<ValidatorConfig> configs) {
        for (Element validatorElement : validatorElements) {
            String validatorType = validatorElement.attributeValue("type");
            Map<String, String> params = new HashMap<String, String>(extraParams);
            List<Element> paramElements = validatorElement.elements("param");
            for (Element paramElement : paramElements) {
                String paramName = paramElement.attributeValue("name");
                String text = paramElement.getText();
                params.put(paramName, text);
            }
            String message = validatorElement.elementText("message");
            ValidatorConfig config = new ValidatorConfig(validatorType, params, message);
            configs.add(config);
        }
    }
}
