package ru.runa.wf.web.datafile.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

import com.google.common.base.Charsets;

public class BotDataFileBuilder implements DataFileBuilder {
    private final User user;

    public BotDataFileBuilder(User user) {
        this.user = user;
    }

    @Override
    public void build(ZipOutputStream zos, Document script) {
        List<BotStation> botStations = Delegates.getBotService().getBotStations();
        for (BotStation botStation : botStations) {
            populateBotStation(script, botStation);
            List<Bot> bots = Delegates.getBotService().getBots(user, botStation.getId());
            for (Bot bot : bots) {
                populateBot(script, bot, botStation.getName());
                List<BotTask> botTasks = Delegates.getBotService().getBotTasks(user, bot.getId());
                for (BotTask botTask : botTasks) {
                    populateBotTask(script, botTask, botStation.getName(), bot.getUsername());
                }
            }
        }

        new PermissionsDataFileBuilder(new ArrayList<Identifiable>(botStations), "addPermissionsOnBotStations", user).build(zos, script);
    }

    private void populateBotStation(Document script, BotStation botStation) {
        Element element = script.getRootElement().addElement("createBotStation", XmlUtils.RUNA_NAMESPACE);
        if (StringUtils.isNotEmpty(botStation.getName())) {
            element.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, botStation.getName());
        }
        if (StringUtils.isNotEmpty(botStation.getAddress())) {
            element.addAttribute(AdminScriptConstants.ADDRESS_ATTRIBUTE_NAME, botStation.getAddress());
        }
    }

    private void populateBot(Document script, Bot bot, String botStationName) {
        Element element = script.getRootElement().addElement("createBot", XmlUtils.RUNA_NAMESPACE);
        element.addAttribute(AdminScriptConstants.BOTSTATION_ATTRIBUTE_NAME, botStationName);
        if (StringUtils.isNotEmpty(bot.getUsername())) {
            element.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, bot.getUsername());
        }
        element.addAttribute(AdminScriptConstants.PASSWORD_ATTRIBUTE_NAME, bot.getPassword());
    }

    private void populateBotTask(Document script, BotTask botTask, String botStationName, String botName) {
        Element element = script.getRootElement().addElement("addConfigurationsToBot", XmlUtils.RUNA_NAMESPACE);
        element.addAttribute(AdminScriptConstants.BOTSTATION_ATTRIBUTE_NAME, botStationName);
        element.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, botName);
        Element subElement = element.addElement("botConfiguration", XmlUtils.RUNA_NAMESPACE);
        if (StringUtils.isNotEmpty(botTask.getName())) {
            subElement.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, botTask.getName());
        }
        if (StringUtils.isNotEmpty(botTask.getTaskHandlerClassName())) {
            subElement.addAttribute(AdminScriptConstants.HANDLER_ATTRIBUTE_NAME, botTask.getTaskHandlerClassName());
        }
        if (StringUtils.isNotEmpty(botTask.getName())) {
            subElement.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, botTask.getName());
        }
        if (botTask.getConfiguration() != null) {
            subElement
                    .addAttribute(AdminScriptConstants.CONFIGURATION_CONTENT_ATTRIBUTE_NAME, new String(botTask.getConfiguration(), Charsets.UTF_8));
        }
    }
}
