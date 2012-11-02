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

import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.xml.XMLHelper;
import ru.runa.wfe.validation.ValidatorConfig;

import com.google.common.collect.Maps;

public class ValidatorFileParser {
    static final String MULTI_TEXTVALUE_SEPARATOR = " ";

    public static Map<String, String> parseValidatorDefinitions(InputStream is) {
        Map<String, String> result = Maps.newHashMap();
        try {
            Document doc = XMLHelper.getDocumentWithoutValidation(is);
            NodeList nodes = doc.getElementsByTagName("validator");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element validatorElement = (Element) nodes.item(i);
                String name = validatorElement.getAttribute("name");
                String className = validatorElement.getAttribute("class");
                result.put(name, className);
            }
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
        return result;
    }

    public static List<ValidatorConfig> parseValidatorConfigs(InputStream is) {
        try {
            List<ValidatorConfig> validatorCfgs = new ArrayList<ValidatorConfig>();
            Document doc = XMLHelper.getDocumentWithoutValidation(is);
            NodeList fieldNodes = doc.getElementsByTagName("field");
            {
                NodeList validatorNodes = doc.getElementsByTagName("validator");
                addValidatorConfigs(validatorNodes, new HashMap<String, String>(), validatorCfgs);
            }

            for (int i = 0; i < fieldNodes.getLength(); i++) {
                Element fieldElement = (Element) fieldNodes.item(i);
                String fieldName = fieldElement.getAttribute("name");
                Map<String, String> extraParams = new HashMap<String, String>();
                extraParams.put("fieldName", fieldName);

                NodeList validatorNodes = fieldElement.getElementsByTagName("field-validator");
                addValidatorConfigs(validatorNodes, extraParams, validatorCfgs);
            }
            return validatorCfgs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getTextValue(Element valueEle) {
        StringBuffer value = new StringBuffer();
        NodeList nl = valueEle.getChildNodes();
        boolean firstCDataFound = false;
        for (int i = 0; i < nl.getLength(); i++) {
            Node item = nl.item(i);
            if ((item instanceof CharacterData && !(item instanceof Comment)) || item instanceof EntityReference) {
                final String nodeValue = item.getNodeValue();
                if (nodeValue != null) {
                    if (firstCDataFound) {
                        value.append(MULTI_TEXTVALUE_SEPARATOR);
                    } else {
                        firstCDataFound = true;
                    }
                    value.append(nodeValue.trim());
                }
            }
        }
        return value.toString().trim();
    }

    private static void addValidatorConfigs(NodeList validatorNodes, Map<String, String> extraParams, List<ValidatorConfig> configs) {
        for (int j = 0; j < validatorNodes.getLength(); j++) {
            Element validatorElement = (Element) validatorNodes.item(j);
            String validatorType = validatorElement.getAttribute("type");
            Map<String, String> params = new HashMap<String, String>(extraParams);
            NodeList paramNodes = validatorElement.getElementsByTagName("param");
            for (int k = 0; k < paramNodes.getLength(); k++) {
                Element paramElement = (Element) paramNodes.item(k);
                String paramName = paramElement.getAttribute("name");
                params.put(paramName, getTextValue(paramElement));
            }
            NodeList messageNodes = validatorElement.getElementsByTagName("message");
            Node messageNode = messageNodes.item(0).getFirstChild();
            String message = messageNode.getNodeValue();
            ValidatorConfig config = new ValidatorConfig(validatorType, params, message);
            configs.add(config);
        }
    }
}
