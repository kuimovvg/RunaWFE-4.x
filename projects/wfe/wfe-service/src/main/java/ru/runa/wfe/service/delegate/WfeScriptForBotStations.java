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
package ru.runa.wfe.service.delegate;

import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.script.AdminScriptRunner;
import ru.runa.wfe.service.utils.AdminScriptUtils;
import ru.runa.wfe.user.User;

public class WfeScriptForBotStations extends AdminScriptRunner {

    private final boolean replace;
    private BotStation botStation = null;
    private Map<String, byte[]> configs;

    public WfeScriptForBotStations(User user, boolean replace) {
        this.replace = replace;
        setUser(user);
        setProcessDefinitionsBytes(new byte[0][0]);
    }

    public void setBotStation(BotStation bs) {
        botStation = bs;
    }

    public void setConfigs(Map<String, byte[]> configs) {
        this.configs = configs;
    }

    public static byte[] createScriptForBotLoading(Bot bot, List<BotTask> tasks) {
        Document script = AdminScriptUtils.createScriptDocument();
        Element root = script.getRootElement();
        Element createBotElement = root.addElement("createBot", XmlUtils.RUNA_NAMESPACE);
        createBotElement.addAttribute(NAME_ATTRIBUTE_NAME, bot.getUsername());
        createBotElement.addAttribute(PASSWORD_ATTRIBUTE_NAME, "");
        // createBotElement.addAttribute(STARTTIMEOUT_ATTRIBUTE_NAME, "" +
        // bot.getStartTimeout());

        if (tasks.size() > 0) {
            Element removeTasks = root.addElement("removeConfigurationsFromBot", XmlUtils.RUNA_NAMESPACE);
            removeTasks.addAttribute(NAME_ATTRIBUTE_NAME, bot.getUsername());
            for (BotTask task : tasks) {
                Element taskElement = removeTasks.addElement("botConfiguration");
                taskElement.addAttribute(NAME_ATTRIBUTE_NAME, task.getName());
            }
            Element addTasks = root.addElement("addConfigurationsToBot", XmlUtils.RUNA_NAMESPACE);
            addTasks.addAttribute(NAME_ATTRIBUTE_NAME, bot.getUsername());
            for (BotTask task : tasks) {
                Element taskElement = addTasks.addElement("botConfiguration");
                taskElement.addAttribute(NAME_ATTRIBUTE_NAME, task.getName());
                taskElement.addAttribute(HANDLER_ATTRIBUTE_NAME, task.getTaskHandlerClassName());
                if (task.getConfiguration() != null) {
                    taskElement.addAttribute(CONFIGURATION_STRING_ATTRIBUTE_NAME, task.getName() + ".conf");
                }
            }
        }
        return XmlUtils.save(script);
    }

    @Override
    public void removeConfigurationsFromBot(Element element) {
        if (replace) {
            super.removeConfigurationsFromBotCommon(element, botStation);
        }
    }

    @Override
    public void addConfigurationsToBot(Element element) {
        addConfigurationsToBotCommon(element, botStation);
    }

    @Override
    public void createBot(Element element) {
        createBotCommon(element, botStation);
    }

    @Override
    protected byte[] getBotTaskConfiguration(String config) {
        return configs.get(config);
    }
}
