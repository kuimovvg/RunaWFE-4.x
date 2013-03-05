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

package ru.runa.wf.web.tag;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLOptionElement;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.action.LoadProcessDefinitionHtmlFileAction;
import ru.runa.wf.web.form.DefinitionFileForm;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

/**
 * 
 * Created on 21.02.2007
 * 
 */
public class HTMLFormConverter {

    private static final Log log = LogFactory.getLog(HTMLUtils.class);

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
    private static final String CSS_CLASS_REQUIRED = "required";

    private static final String[] STD_INPUT_NAMES = { "", "text", "hidden", "password" };

    public static byte[] changeUrls(PageContext pageContext, Long id, String htmlHref, byte[] originalBytes) {
        Document document = HTMLUtils.readHtml(originalBytes);
        if (pageContext != null) {
            for (Map.Entry<String, String> tagNameTagAttributeEntry : TAG_NAME_ATTRIBUTE_MAP.entrySet()) {
                String tagName = tagNameTagAttributeEntry.getKey();
                NodeList htmlTagElements = document.getElementsByTagName(tagName);
                if (htmlTagElements.getLength() > 0) {
                    String attributeName = tagNameTagAttributeEntry.getValue();
                    handleElements(htmlTagElements, attributeName, htmlHref, pageContext, id);
                }
            }
            if (WebResources.getBooleanProperty("form.tr.title.clean", true)) {
                NodeList trElements = document.getElementsByTagName("tr");
                for (int i = 0; i < trElements.getLength(); i++) {
                    Node node = trElements.item(i);
                    if (node.getAttributes().getNamedItem("title") != null) {
                        node.getAttributes().removeNamedItem("title");
                    }
                }
            }
        }
        return HTMLUtils.writeHtml(document);
    }

    private static void handleElements(NodeList nodeList, String hrefAttributeName, String htmlHref, PageContext pageContext, Long id) {
        String[] fileNameStructureElements = htmlHref.split("/");
        Map<String, Object> params = Maps.newHashMap();
        params.put(IdForm.ID_INPUT_NAME, id);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            Node hrefNode = node.getAttributes().getNamedItem(hrefAttributeName);
            if (hrefNode == null) {
                continue;
            }
            String href = hrefNode.getNodeValue();
            if (href == null || href.length() == 0 || "#".equals(href) || href.startsWith("javascript")) {
                continue;
            }
            if (isHrefRelative(href)) {
                String url = getNormalizedUrlForElement(htmlHref, fileNameStructureElements, href);
                int anchorIndex = url.indexOf("#");
                String anchor = null;
                if (anchorIndex >= 0) {
                    anchor = url.substring(anchorIndex + 1);
                    url = url.substring(0, anchorIndex);
                }
                params.put(DefinitionFileForm.URL_INPUT_NAME, url);
                String newHref = Commons.getActionUrl(LoadProcessDefinitionHtmlFileAction.ACTION_PATH, params, anchor, pageContext,
                        PortletUrlType.Resource);
                hrefNode.setNodeValue(newHref);
            }
        }
    }

    private static String getNormalizedUrlForElement(String htmlHref, String[] fileNameStructureElements, String originalHref) {
        StringBuilder normalizedUrl = new StringBuilder();

        if (originalHref.startsWith("./")) {
            originalHref = originalHref.substring(2);
        }

        if (originalHref.startsWith("../")) {
            int prefixLength = "../".length();
            int counter = 0;
            while (originalHref.startsWith("../", counter * prefixLength)) {
                counter++;
            }
            if (counter < fileNameStructureElements.length) {
                for (int j = 0; j < fileNameStructureElements.length - counter - 1; j++) {
                    normalizedUrl.append(fileNameStructureElements[j]);
                    normalizedUrl.append("/");
                }
            } else {
                normalizedUrl.append("#");
            }
        } else if (originalHref.startsWith("/")) {
            // DO NOTHING
        } else if (originalHref.startsWith("#")) {
            normalizedUrl.append(htmlHref);
        } else {
            int lastDirIndex = htmlHref.lastIndexOf("/");
            if (lastDirIndex > 0) {
                String additionalPath = htmlHref.substring(0, lastDirIndex + 1);
                normalizedUrl.append(additionalPath);
            }
        }
        return normalizedUrl.append(originalHref).toString();
    }

    private static boolean isHrefRelative(String href) {
        if (href.contains(PROTOCOL_SEPARATOR) || href.startsWith("/")) {
            return false;
        } else {
            return true;
        }
    }

    public static String fillForm(PageContext pageContext, String formHtml, Map<String, String[]> variables, Map<String, String> errors) {
        try {
            Document document = HTMLUtils.readHtml(formHtml.getBytes(Charsets.UTF_8));

            NodeList htmlTagElements = document.getElementsByTagName("input");
            for (int i = 0; i < htmlTagElements.getLength(); i++) {
                Element node = (Element) htmlTagElements.item(i);
                String typeName = node.getAttribute(TYPE_ATTR);
                String inputName = node.getAttribute(NAME_ATTR);

                String[] valueArray = variables.get(inputName);
                String stringValue = getString(valueArray);

                // handle input (type='text, password')
                if (hasValue(STD_INPUT_NAMES, typeName, true)) {
                    node.setAttribute(VALUE_ATTR, stringValue);
                } else if ("checkbox".equalsIgnoreCase(typeName)) {
                    String checkBoxValue = node.getAttribute(VALUE_ATTR);
                    if (hasValue(valueArray, checkBoxValue, false)) {
                        node.setAttribute(CHECKED_ATTR, CHECKED_ATTR);
                    } else {
                        // reset values for default ones
                        node.removeAttribute(CHECKED_ATTR);
                    }
                } else if ("radio".equalsIgnoreCase(typeName)) {
                    String radioValue = node.getAttribute(VALUE_ATTR);
                    if (stringValue.equals(radioValue)) {
                        node.setAttribute(CHECKED_ATTR, CHECKED_ATTR);
                    } else {
                        // reset values for default ones
                        node.removeAttribute(CHECKED_ATTR);
                    }
                } else if ("file".equalsIgnoreCase(typeName)) {
                    log.debug("file is not supported, ");
                } else {
                    log.error("- Strange input type: " + typeName);
                }
                handleErrors(errors, inputName, pageContext, document, node);
            }
            NodeList textareaElements = document.getElementsByTagName("textarea");
            for (int i = 0; i < textareaElements.getLength(); i++) {
                Element node = (Element) textareaElements.item(i);
                String inputName = node.getAttribute(NAME_ATTR);
                if (inputName != null && inputName.length() > 0) {
                    String[] valueArray = variables.get(inputName);
                    String stringValue = getString(valueArray);
                    stringValue = stringValue.replaceAll("\r\n", "\n");
                    if (node.getFirstChild() != null) {
                        node.getFirstChild().setNodeValue(stringValue);
                    } else {
                        Text text = document.createTextNode(stringValue);
                        node.appendChild(text);
                    }
                    handleErrors(errors, inputName, pageContext, document, node);
                }
            }
            NodeList selectElements = document.getElementsByTagName("select");
            for (int i = 0; i < selectElements.getLength(); i++) {
                Element node = (Element) selectElements.item(i);
                String inputName = node.getAttribute(NAME_ATTR);
                String[] valueArray = variables.get(inputName);
                NodeList options = node.getChildNodes();
                for (int j = 0; j < options.getLength(); j++) {
                    if (options.item(j) instanceof HTMLOptionElement) {
                        HTMLOptionElement option = (HTMLOptionElement) options.item(j);
                        String optionValue = option.getValue();
                        if (hasValue(valueArray, optionValue, false)) {
                            option.setAttribute(SELECTED_ATTR, SELECTED_ATTR);
                        } else {
                            option.removeAttribute(SELECTED_ATTR);
                        }
                    }
                }
                handleErrors(errors, inputName, pageContext, document, node);
            }

            if ((errors != null) && (errors.size() != 0)) {
                // Not for all errors inputs found, put just in the end of page.
                log.debug("Appending errors to the end of the form.");
                document.getLastChild().appendChild(document.createElement("hr"));
                Element font = document.createElement("font");
                for (String varName : errors.keySet()) {
                    font.appendChild(document.createElement("br"));
                    font.setAttribute("class", "error");
                    Element b = document.createElement("b");
                    b.appendChild(document.createTextNode(getErrorText(pageContext, errors, varName)));
                    font.appendChild(b);
                }
                document.getLastChild().appendChild(font);
            }
            return new String(HTMLUtils.writeHtml(document), Charsets.UTF_8);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static String getVarAsString(IVariableProvider variableProvider, String name) {
        Object value = variableProvider.getValue(name);
        if (value instanceof Date) {
            // we don't know user format of date
            return null;
        }
        if (value instanceof List<?>) {
            // we don't handle them
            return null;
        }
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    public static byte[] setInputValues(byte[] formBytes, IVariableProvider variableProvider, List<String> requiredVarNames) {
        try {
            Document document = HTMLUtils.readHtml(formBytes);

            NodeList htmlTagElements = document.getElementsByTagName("input");
            for (int i = 0; i < htmlTagElements.getLength(); i++) {
                Element node = (Element) htmlTagElements.item(i);
                String typeName = node.getAttribute(TYPE_ATTR);
                String inputName = node.getAttribute(NAME_ATTR);
                if (requiredVarNames.contains(inputName)) {
                    addRequiredClassAttribute(node);
                }
                String stringValue = getVarAsString(variableProvider, inputName);
                if (stringValue == null) {
                    continue;
                }

                // handle input (type='text, password')
                if (typeName == null || hasValue(STD_INPUT_NAMES, typeName, true)) {
                    node.setAttribute(VALUE_ATTR, stringValue);
                } else if ("checkbox".equalsIgnoreCase(typeName)) {
                    String checkBoxValue = node.getAttribute(VALUE_ATTR);
                    Object variableValue = variableProvider.getValue(inputName);
                    if (variableValue instanceof String[]) {
                        List<String> selectedValues = TypeConversionUtil.convertTo(List.class, variableValue);
                        if (selectedValues.contains(checkBoxValue)) {
                            node.setAttribute(CHECKED_ATTR, CHECKED_ATTR);
                        } else {
                            // reset values for default ones
                            node.removeAttribute(CHECKED_ATTR);
                        }
                    } else {
                        if ("true".equals(stringValue) || "on".equals(stringValue) || stringValue.equals(checkBoxValue)) {
                            node.setAttribute(CHECKED_ATTR, CHECKED_ATTR);
                        } else {
                            // reset values for default ones
                            node.removeAttribute(CHECKED_ATTR);
                        }
                    }
                } else if ("radio".equalsIgnoreCase(typeName)) {
                    String radioValue = node.getAttribute(VALUE_ATTR);
                    if (stringValue.equals(radioValue)) {
                        node.setAttribute(CHECKED_ATTR, CHECKED_ATTR);
                    } else {
                        // reset values for default ones
                        node.removeAttribute(CHECKED_ATTR);
                    }
                } else if ("file".equalsIgnoreCase(typeName)) {
                    log.debug("file is not supported, ");
                } else {
                    log.error("- Strange input type: " + typeName);
                }
            }
            NodeList textareaElements = document.getElementsByTagName("textarea");
            for (int i = 0; i < textareaElements.getLength(); i++) {
                Element node = (Element) textareaElements.item(i);
                String inputName = node.getAttribute(NAME_ATTR);
                if (requiredVarNames.contains(inputName)) {
                    addRequiredClassAttribute(node);
                }
                String stringValue = getVarAsString(variableProvider, inputName);
                if (stringValue == null) {
                    continue;
                }
                if (node.getFirstChild() != null) {
                    node.getFirstChild().setNodeValue(stringValue);
                } else {
                    Text text = document.createTextNode(stringValue);
                    node.appendChild(text);
                }
            }
            NodeList selectElements = document.getElementsByTagName("select");
            for (int i = 0; i < selectElements.getLength(); i++) {
                Element node = (Element) selectElements.item(i);
                String inputName = node.getAttribute(NAME_ATTR);
                if (requiredVarNames.contains(inputName)) {
                    addRequiredClassAttribute(node);
                }
                String stringValue = getVarAsString(variableProvider, inputName);
                if (stringValue == null) {
                    continue;
                }
                NodeList options = node.getChildNodes();
                for (int j = 0; j < options.getLength(); j++) {
                    if (options.item(j) instanceof HTMLOptionElement) {
                        HTMLOptionElement option = (HTMLOptionElement) options.item(j);
                        String optionValue = option.getValue();
                        if (stringValue.equals(optionValue)) {
                            option.setAttribute(SELECTED_ATTR, SELECTED_ATTR);
                        } else {
                            option.removeAttribute(SELECTED_ATTR);
                        }
                    }
                }
            }
            return HTMLUtils.writeHtml(document);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static void addRequiredClassAttribute(Element element) {
        String cssClasses = element.getAttribute(CSS_CLASS_ATTR);
        if (cssClasses == null) {
            cssClasses = "";
        } else {
            cssClasses += " ";
        }
        cssClasses += CSS_CLASS_REQUIRED;
        element.setAttribute(CSS_CLASS_ATTR, cssClasses);
    }

    private static String getString(String[] valueArray) {
        if ((valueArray != null) && (valueArray.length > 0)) {
            return valueArray[0];
        }
        return "";
    }

    private static boolean hasValue(String[] array, String value, boolean ignoreCase) {
        if ((value == null) || (array == null)) {
            return false;
        }
        for (int i = 0; i < array.length; i++) {
            if (ignoreCase) {
                if (value.equalsIgnoreCase(array[i])) {
                    return true;
                }
            } else {
                if (value.equals(array[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getErrorText(PageContext pageContext, Map<String, String> errors, String inputName) {
        String errorText = errors.get(inputName);
        if (errorText == null) {
            errorText = Commons.getMessage(Messages.MESSAGE_WEB_CLIENT_VARIABLE_FORMAT_ERROR, pageContext, new Object[] { inputName });
        }
        return errorText;
    }

    private static void handleErrors(Map<String, String> errors, String inputName, PageContext pageContext, Document document, Element node) {
        if (errors != null && errors.containsKey(inputName)) {
            addError(pageContext, document, node, getErrorText(pageContext, errors, inputName), inputName);
            // avoiding multiple error labels
            errors.remove(inputName);
        }
    }

    private static void addError(PageContext pageContext, Document document, Element node, String errorText, String inputName) {
        Element errorImg = document.createElement("img");
        errorImg.setAttribute("title", errorText);
        errorImg.setAttribute("src", Commons.getUrl("/images/error.gif", pageContext, PortletUrlType.Resource));
        Node parent = node.getParentNode();
        parent.insertBefore(errorImg, node.getNextSibling());
    }

}
