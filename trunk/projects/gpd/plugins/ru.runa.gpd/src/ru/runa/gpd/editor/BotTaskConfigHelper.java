package ru.runa.gpd.editor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.handler.ParamDef;
import ru.runa.gpd.extension.handler.ParamDefConfig;
import ru.runa.gpd.extension.handler.ParamDefGroup;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.util.IOUtils;

/**
 * 
 * @author rivenforce
 * The class provide methods for perform operation with bot task config
 * 
 */
public class BotTaskConfigHelper {
    @SuppressWarnings("unchecked")
    public static final ParamDefConfig getParamDefConfig(String botTaskConfig) {
        ParamDefConfig paramDefConfig = null;
        try {
            Document doc = DocumentHelper.parseText(botTaskConfig);
            List<Element> elements = doc.getRootElement().elements("parameters");
            if (elements.size() == 0 || elements.size() > 1) {
                throw new Exception("Invalid configuration file");
            }
            String paramDefConfigContent = getContent(elements.get(0));
            paramDefConfig = ParamDefConfig.parse(DocumentHelper.parseText(paramDefConfigContent));
        } catch (Exception e) {
            paramDefConfig = createEmptyParamDefConfig();
        }
        return paramDefConfig;
    }

    @SuppressWarnings("unchecked")
    private static String getContent(Element element) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<Element> i = element.elementIterator(); i.hasNext();) {
            Element e = i.next();
            builder.append(e.asXML());
        }
        return builder.toString();
    }

    public static ParamDefConfig createEmptyParamDefConfig() {
        ParamDefConfig paramDefConfig = new ParamDefConfig("config");
        ParamDefGroup inputGroup = new ParamDefGroup(ParamDefGroup.NAME_INPUT);
        ParamDefGroup outputGroup = new ParamDefGroup(ParamDefGroup.NAME_OUTPUT);
        paramDefConfig.getGroups().add(inputGroup);
        paramDefConfig.getGroups().add(outputGroup);
        return paramDefConfig;
    }

    public static boolean isParamDefConfigEmpty(ParamDefConfig paramDefConfig) {
        boolean result = true;
        for (ParamDefGroup group : paramDefConfig.getGroups()) {
            result = result && (group.getParameters().size() == 0);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static String getConfigFromBotTaskConfig(String botTaskConfig) {
        String returnConfig = botTaskConfig;
        try {
            Document doc = DocumentHelper.parseText(botTaskConfig);
            List<Element> elements = doc.getRootElement().elements("botconfig");
            if (elements.size() == 0 || elements.size() > 1) {
                throw new Exception("Invalid configuration file");
            }
            returnConfig = elements.get(0).getText();
        } catch (Exception e) {
            returnConfig = botTaskConfig;
        }
        return returnConfig;
    }

    public static String createConfigWithFormalParam(BotTask task, String config) {
        return "<bot><parameters>" + paramDefConfigToXML(task) + "</parameters><botconfig><![CDATA[" + config + "]]></botconfig></bot>";
    }

    public static String paramDefConfigToXML(BotTask task) {
        Document doc = DocumentHelper.createDocument();
        doc.add(DocumentHelper.createElement(task.getParamDefConfig().getName()));
        Element root = doc.getRootElement();
        for (ParamDefGroup group : task.getParamDefConfig().getGroups()) {
            Element groupParamElement = DocumentHelper.createElement(group.getName());
            root.add(groupParamElement);
            for (ParamDef param : group.getParameters()) {
                Element paramElement = DocumentHelper.createElement("param");
                paramElement.addAttribute("name", param.getName());
                paramElement.addAttribute("label", param.getName());
                if (param.getFormatFilters().size() > 0) {
                    paramElement.addAttribute("formatFilter", param.getFormatFilters().iterator().next());
                }
                groupParamElement.add(paramElement);
            }
        }
        return writeDoc(doc);
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

    public static boolean isConfigParamInPlugin(String config) {
        boolean result = false;
        try {
            Document doc = DocumentHelper.parseText(config);
            result = doc.getRootElement().getName().equals("config");
        } catch (Exception e) {
        }
        return result;
    }

    public static boolean isConfigParamInFile(String config) {
        boolean result = false;
        try {
            Document doc = DocumentHelper.parseText(config);
            result = doc.getRootElement().getName().equals("bot");
        } catch (Exception e) {
        }
        return result;
    }

    public static boolean isTaskConfigurationInPlugin(String className) {
        boolean result = false;
        DelegableProvider provider = HandlerRegistry.getProvider(className);
        if (provider.getBundle() != null) {
            String path = "/conf/" + getSimpleClassName(className) + ".xml";
            try {
                InputStream is = provider.getBundle().getEntry(path).openStream();
                String content = IOUtils.readStream(is);
                result = content.length() > 0;
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static String getSimpleClassName(String className) {
        int dotIndex = className.lastIndexOf(".");
        return className.substring(dotIndex + 1);
    }
}
