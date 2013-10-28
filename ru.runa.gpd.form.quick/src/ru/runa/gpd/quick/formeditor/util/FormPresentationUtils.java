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

package ru.runa.gpd.quick.formeditor.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLOptionElement;

import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * 
 * Created on 21.02.2007
 * 
 */
public class FormPresentationUtils {
    private static final Log log = LogFactory.getLog(FormPresentationUtils.class);

    private static final String PROTOCOL_SEPARATOR = "//";
    /**
     * This map contains Tag name -> Href attribute name
     */
    private static final Map<String, String> TAG_NAME_ATTRIBUTE_MAP = new HashMap<String, String>();
    static {
        TAG_NAME_ATTRIBUTE_MAP.put("a", "href");
        TAG_NAME_ATTRIBUTE_MAP.put("img", "src");
        TAG_NAME_ATTRIBUTE_MAP.put("frame", "src");
        TAG_NAME_ATTRIBUTE_MAP.put("link", "href");
        TAG_NAME_ATTRIBUTE_MAP.put("script", "src");
    }
    private static final String VALUE_ATTR = "value";
    private static final String NAME_ATTR = "name";
    private static final String TYPE_ATTR = "type";
    private static final String CHECKED_ATTR = "checked";
    private static final String SELECTED_ATTR = "selected";
    private static final String CSS_CLASS_ATTR = "class";
    private static final String ERROR_CONTAINER = "span";

    

    
    public static String adjustForm(String formHtml, IVariableProvider variableProvider) {
        try {
            
            Document document = HTMLUtils.readHtml(formHtml.getBytes(Charsets.UTF_8));
            //adjustUrls(pageContext, document, definitionId, "form.ftl");
            NodeList htmlTagElements = document.getElementsByTagName("input");
            for (int i = 0; i < htmlTagElements.getLength(); i++) {
                Element node = (Element) htmlTagElements.item(i);
                String typeName = node.getAttribute(TYPE_ATTR);
                String inputName = node.getAttribute(NAME_ATTR);
                String stringValue = getStringValue(inputName, variableProvider, new HashMap<String, String[]>());
                if (stringValue == null) {
                    continue;
                }
                // handle input (type='text, password')
                if (Strings.isNullOrEmpty(typeName) || "text".equalsIgnoreCase(typeName) || "password".equalsIgnoreCase(typeName)
                        || "hidden".equalsIgnoreCase(typeName)) {
                    if (Strings.isNullOrEmpty(node.getAttribute(VALUE_ATTR))) {
                        log.debug("Setting " + inputName + "[value]=" + stringValue);
                        node.setAttribute(VALUE_ATTR, stringValue);
                    }
                } else if ("checkbox".equalsIgnoreCase(typeName)) {
                    String checkBoxValue = node.getAttribute(VALUE_ATTR);
                    if (Objects.equal(checkBoxValue, stringValue) || "true".equals(stringValue)) {
                        log.debug("Adding " + inputName + "[checked]");
                        node.setAttribute(CHECKED_ATTR, CHECKED_ATTR);
                    } else {
                        log.debug("Removing " + inputName + "[checked]");
                        node.removeAttribute(CHECKED_ATTR);
                    }
                } else if ("radio".equalsIgnoreCase(typeName)) {
                    String radioValue = node.getAttribute(VALUE_ATTR);
                    if (Objects.equal(stringValue, radioValue)) {
                        log.debug("Adding " + inputName + "[checked]");
                        node.setAttribute(CHECKED_ATTR, CHECKED_ATTR);
                    } else {
                        log.debug("Removing " + inputName + "[checked]");
                        node.removeAttribute(CHECKED_ATTR);
                    }
                } else if ("file".equalsIgnoreCase(typeName) || "button".equalsIgnoreCase(typeName)) {
                } else {
                    log.error("Strange input " + inputName + "[type='" + typeName + "']");
                }
            }
            NodeList textareaElements = document.getElementsByTagName("textarea");
            for (int i = 0; i < textareaElements.getLength(); i++) {
                Element node = (Element) textareaElements.item(i);
                String inputName = node.getAttribute(NAME_ATTR);
                
                String stringValue = getStringValue(inputName, variableProvider, new HashMap<String, String[]>());
                if (stringValue == null || Strings.isNullOrEmpty(inputName)) {
                    continue;
                }
                if (node.getFirstChild() != null) {
                    if (Strings.isNullOrEmpty(node.getFirstChild().getNodeValue())) {
                        log.debug("Setting " + inputName + " text");
                        node.getFirstChild().setNodeValue(stringValue);
                    }
                } else {
                    log.debug("Adding " + inputName + " text");
                    node.appendChild(document.createTextNode(stringValue));
                }
            }
            NodeList selectElements = document.getElementsByTagName("select");
            for (int i = 0; i < selectElements.getLength(); i++) {
                Element node = (Element) selectElements.item(i);
                String inputName = node.getAttribute(NAME_ATTR);
                
                String stringValue = getStringValue(inputName, variableProvider, new HashMap<String, String[]>());
                if (stringValue == null) {
                    continue;
                }
                NodeList options = node.getChildNodes();
                for (int j = 0; j < options.getLength(); j++) {
                    if (options.item(j) instanceof HTMLOptionElement) {
                        HTMLOptionElement option = (HTMLOptionElement) options.item(j);
                        if (Objects.equal(option.getValue(), stringValue)) {
                            log.debug("Setting selected option " + option.getValue() + " in select[name='" + inputName + "']");
                            option.setAttribute(SELECTED_ATTR, SELECTED_ATTR);
                        } else {
                            log.debug("Removing selected option " + option.getValue() + " in select[name='" + inputName + "']");
                            option.removeAttribute(SELECTED_ATTR);
                        }
                    }
                }
            }
            
            return new String(HTMLUtils.writeHtml(document), Charsets.UTF_8);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Rules:
     * 
     * 1) don't handling multiple input (we cannot do this properly; they are
     * handled in according tags.
     * 
     * 2) Don't fill long strings due to
     * java.lang.ArrayIndexOutOfBoundsException at
     * java.lang.String.getChars(String.java:854) at
     * org.apache.xml.serializer.WriterToUTF8Buffered
     * .write(WriterToUTF8Buffered.java:347)
     * 
     * 3) User input has precedence on variables
     * 
     * @param valueArray
     *            http values
     * @return <code>null</code> or replacement value
     */
    private static String getStringValue(String name, IVariableProvider variableProvider, Map<String, String[]> userInput) {
    	/*if (userInput != null && userInput.get(name) != null && userInput.get(name).length == 1 && userInput.get(name)[0].length() < 1000) {
            return userInput.get(name)[0];
        }*/
        /*Object value = variableProvider.getValue(name);
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }*/
        //return null;
    	return "";
    }
}
