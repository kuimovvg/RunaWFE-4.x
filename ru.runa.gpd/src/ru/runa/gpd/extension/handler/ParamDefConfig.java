package ru.runa.gpd.extension.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.CommonUtils;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("unchecked")
public class ParamDefConfig {
    public static final String NAME_CONFIG = "config";
    private static final Pattern VARIABLE_REGEXP = Pattern.compile("\\$\\{(.*?[^\\\\])\\}");
    private final String name;
    private final List<ParamDefGroup> groups = new ArrayList<ParamDefGroup>();

    public ParamDefConfig(String name) {
        this.name = name;
    }

    public ParamDefConfig() {
        this(NAME_CONFIG);
    }

    public static ParamDefConfig parse(String xml) {
        return parse(XmlUtil.parseWithoutValidation(xml));
    }

    public static ParamDefConfig parse(Document document) {
        Element rootElement = document.getRootElement();
        return parse(rootElement);
    }

    public static ParamDefConfig parse(Element rootElement) {
        ParamDefConfig config = new ParamDefConfig();
        List<Element> groupElements = rootElement.elements();
        for (Element groupElement : groupElements) {
            ParamDefGroup group = new ParamDefGroup(groupElement);
            List<Element> inputParamElements = groupElement.elements("param");
            for (Element element : inputParamElements) {
                group.getParameters().add(new ParamDef(element));
            }
            config.getGroups().add(group);
        }
        return config;
    }

    public String getName() {
        return name;
    }

    public List<ParamDefGroup> getGroups() {
        return groups;
    }

    /**
     * Retrieves all founded parameter to variable mappings
     * @param configuration param-based xml or <code>null</code> or empty string
     */
    public static Map<String, String> getAllParameters(String configuration) {
        Map<String, String> properties = new HashMap<String, String>();
        if (Strings.isNullOrEmpty(configuration)) {
            return properties;
        }
        Document doc = XmlUtil.parseWithoutValidation(configuration);
        List<Element> groupElements = doc.getRootElement().elements();
        for (Element groupElement : groupElements) {
            List<Element> paramElements = groupElement.elements("param");
            for (Element element : paramElements) {
                String value;
                if (element.attributeValue("variable") != null) {
                    value = element.attributeValue("variable");
                } else {
                    value = element.attributeValue("value");
                }
                String name = element.attributeValue("name");
                properties.put(name, value);
            }
        }
        return properties;
    }

    /**
     * Retrieves all founded parameter to variable mappings based on this definition.
     * @param configuration valid param-based xml
     * @return not <code>null</code> parameters (empty parameters on parsing error)
     */
    public Map<String, String> parseConfiguration(String configuration) {
        Map<String, String> properties = new HashMap<String, String>();
        if (Strings.isNullOrEmpty(configuration)) {
            return properties;
        }
        try {
            Document doc = XmlUtil.parseWithoutValidation(configuration);
            Map<String, String> allProperties = new HashMap<String, String>();
            for (ParamDefGroup group : groups) {
                Element groupElement = doc.getRootElement().element(group.getName());
                if (groupElement != null) {
                    List<Element> pElements = groupElement.elements();
                    for (Element element : pElements) {
                        if ("param".equals(element.getName())) {
                            String value;
                            if (element.attributeValue("variable") != null) {
                                value = element.attributeValue("variable");
                            } else {
                                value = element.attributeValue("value");
                            }
                            String name = element.attributeValue("name");
                            allProperties.put(name, value);
                        } else {
                            allProperties.put(element.getName(), element.getTextTrim());
                        }
                    }
                }
            }
            for (ParamDefGroup group : groups) {
                Element groupElement = doc.getRootElement().element(group.getName());
                if (groupElement != null) {
                    List<Element> pElements = groupElement.elements();
                    for (Element element : pElements) {
                        String name = "param".equals(element.getName()) ? element.attributeValue("name") : element.getName();
                        String value = allProperties.get(name);
                        String fName = fixParamName(name, allProperties);
                        if (fName == null) {
                            group.getDynaProperties().put(name, value);
                        } else {
                            properties.put(fName, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog(configuration, e);
        }
        return properties;
    }

    public String fixParamName(String name, Map<String, String> properties) {
        for (ParamDefGroup group : groups) {
            for (ParamDef paramDef : group.getParameters()) {
                String paramName = paramDef.getName();
                if (name.equals(paramName)) {
                    return name;
                }
                paramName = substitute(paramName, properties);
                if (name.equals(paramName)) {
                    return paramDef.getName();
                }
            }
        }
        return null;
    }

    public ParamDef getParamDef(String name) {
        for (ParamDefGroup group : groups) {
            for (ParamDef paramDef : group.getParameters()) {
                String paramName = paramDef.getName();
                if (name.equals(paramName)) {
                    return paramDef;
                }
            }
        }
        return null;
    }

    public boolean validate(String configuration) {
        try {
            Map<String, String> props = parseConfiguration(configuration);
            for (ParamDefGroup group : groups) {
                for (ParamDef paramDef : group.getParameters()) {
                    if (!paramDef.isOptional() && !isValid(props.get(paramDef.getName()))) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("validation of " + configuration, e);
            return false;
        }
    }

    protected boolean isValid(String value) {
        return value != null && value.trim().length() > 0;
    }

    public String toConfiguration(List<Variable> variables, Map<String, String> properties) {
        return XmlUtil.toString(toConfigurationXml(variables, properties));
    }

    public Document toConfigurationXml(List<Variable> variables, Map<String, String> properties) {
        Document doc = DocumentHelper.createDocument();
        doc.add(DocumentHelper.createElement(name));
        Element root = doc.getRootElement();
        Element prevGroupElement = null;
        for (ParamDefGroup group : groups) {
            Element groupElement;
            if (prevGroupElement != null && prevGroupElement.getName().equals(group.getName())) {
                groupElement = prevGroupElement;
            } else {
                groupElement = DocumentHelper.createElement(group.getName());
                root.add(groupElement);
                for (String dName : group.getDynaProperties().keySet()) {
                    String dValue = group.getDynaProperties().get(dName);
                    Element paramElement = DocumentHelper.createElement("param");
                    paramElement.addAttribute("name", dName);
                    paramElement.addAttribute("value", dValue);
                    groupElement.add(paramElement);
                }
            }
            for (ParamDef param : group.getParameters()) {
                String value = properties.get(param.getName());
                if (value == null) {
                    continue;
                }
                String paramName = param.getName();
                paramName = substitute(paramName, properties);
                Element paramElement;
                if (param.getXmlNodeType() == ParamDef.XML_TYPE_ATTR) {
                    paramElement = DocumentHelper.createElement("param");
                    paramElement.addAttribute("name", paramName);
                    if (param.isUseVariable() && CommonUtils.isVariableExists(variables, value)) {
                        paramElement.addAttribute("variable", value);
                    } else {
                        paramElement.addAttribute("value", value);
                    }
                } else {
                    paramElement = DocumentHelper.createElement(paramName);
                    paramElement.add(DocumentHelper.createText(value));
                }
                groupElement.add(paramElement);
            }
            prevGroupElement = groupElement;
        }
        return doc;
    }

    private String substitute(String value, Map<String, String> properties) {
        Matcher matcher = VARIABLE_REGEXP.matcher(value);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String pName = matcher.group(1);
            String parameter = properties.get(pName);
            if (parameter == null) {
                parameter = "";
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(parameter));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public void writeXml(Branch parent) {
        Element root = parent.addElement("config");
        for (ParamDefGroup group : getGroups()) {
            Element groupParamElement = root.addElement(group.getName());
            for (ParamDef param : group.getParameters()) {
                Element paramElement = groupParamElement.addElement("param");
                paramElement.addAttribute("name", param.getName());
                paramElement.addAttribute("label", param.getLabel());
                if (param.getFormatFilters().size() > 0) {
                    paramElement.addAttribute("formatFilter", param.getFormatFilters().get(0));
                }
            }
        }
    }
    
    public Set<String> getAllParameterNames() {
        Set<String> result = Sets.newHashSet();
        for (ParamDefGroup group : getGroups()) {
            for (ParamDef param : group.getParameters()) {
                result.add(param.getName());
            }
        }
        return result;
    }
}
