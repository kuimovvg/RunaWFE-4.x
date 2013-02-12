package ru.runa.gpd.editor;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.handler.ParamDef;
import ru.runa.gpd.extension.handler.ParamDefConfig;
import ru.runa.gpd.extension.handler.ParamDefGroup;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Preconditions;

/**
 * 
 * @author rivenforce
 * The class provide methods for perform operation with bot task config
 * 
 */
public class BotTaskConfigHelper {
    public static final ParamDefConfig getParamDefConfig(String botTaskConfig) {
        Document doc = XmlUtil.parseWithoutValidation(botTaskConfig);
        Element element = doc.getRootElement().element("parameters");
        Preconditions.checkNotNull(element);
        return ParamDefConfig.parse(element);
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

    public static String getConfigFromBotTaskConfig(String botTaskConfig) {
        Document document = XmlUtil.parseWithoutValidation(botTaskConfig);
        return document.getRootElement().elementTextTrim("botconfig");
    }

    public static String createConfigWithFormalParam(BotTask task, String config) {
        return "<bot><parameters>" + paramDefConfigToXML(task) + "</parameters><botconfig><![CDATA[" + config + "]]></botconfig></bot>";
    }

    public static String paramDefConfigToXML(BotTask task) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("config");
        for (ParamDefGroup group : task.getParamDefConfig().getGroups()) {
            Element groupParamElement = root.addElement(group.getName());
            for (ParamDef param : group.getParameters()) {
                Element paramElement = groupParamElement.addElement("param");
                paramElement.addAttribute("name", param.getName());
                if (param.getFormatFilters().size() > 0) {
                    paramElement.addAttribute("formatFilter", param.getFormatFilters().get(0));
                }
            }
        }
        return XmlUtil.toString(document);
    }

    public static boolean isConfigParamInPlugin(String config) {
        try {
            Document document = XmlUtil.parseWithoutValidation(config);
            return document.getRootElement().getName().equals("config");
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isConfigParamInFile(String config) {
        try {
            Document document = XmlUtil.parseWithoutValidation(config);
            return document.getRootElement().getName().equals("bot");
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isTaskConfigurationInPlugin(String className) {
        try {
            DelegableProvider provider = HandlerRegistry.getProvider(className);
            String xml = XmlUtil.getParamDefConfig(provider.getBundle(), className);
            ParamDefConfig.parse(xml);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
