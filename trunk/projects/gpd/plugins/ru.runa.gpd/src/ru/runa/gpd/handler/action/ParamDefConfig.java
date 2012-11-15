package ru.runa.gpd.handler.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.PluginConstants;

@SuppressWarnings("unchecked")
public class ParamDefConfig {

    private static final Pattern VARIABLE_REGEXP = Pattern.compile("\\$\\{(.*?[^\\\\])\\}");

    private final String name;
    private final List<ParamDefGroup> groups = new ArrayList<ParamDefGroup>();

    public ParamDefConfig(String name) {
        this.name = name;
    }

    public static ParamDefConfig parse(Document doc) {
        Element configElement = doc.getRootElement();
        ParamDefConfig config = new ParamDefConfig(configElement.getName());
        List<Element> groupElements = configElement.elements();
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

    public Map<String, String> parseConfiguration(String configuration) {
        Map<String, String> properties = new HashMap<String, String>();
        if (configuration == null || configuration.trim().length() == 0) {
            return properties;
        }
        try {
            Document doc = DocumentHelper.parseText(configuration);
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
            return properties;
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Invalid configuration " + configuration, e);
            return null;
        }
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
            if (props == null) {
                return false;
            }
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

    public String toConfiguration(Map<String, String> properties) {
        return writeDoc(toConfigurationDocument(properties));
    }

    public Document toConfigurationDocument(Map<String, String> properties) {
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
                    if (param.isUseVariable()) {
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

    // TODO move to utils
    public static String writeDoc(Document doc) {
        try {
            OutputFormat format = new OutputFormat("  ", true);
            format.setPadText(true);
            format.setSuppressDeclaration(true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLWriter writer = new XMLWriter(baos, format);
            writer.write(doc);
            writer.flush();
            return new String(baos.toByteArray(), PluginConstants.UTF_ENCODING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
