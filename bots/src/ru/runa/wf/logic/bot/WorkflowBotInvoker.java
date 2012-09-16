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
package ru.runa.wf.logic.bot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.af.Bot;
import ru.runa.af.BotStation;
import ru.runa.af.BotTask;
import ru.runa.af.bot.BotException;
import ru.runa.af.bot.BotInvokerException;
import ru.runa.af.logic.bot.BotInvoker;
import ru.runa.af.service.BotsService;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 04.03.2005
 */
public class WorkflowBotInvoker implements BotInvoker {
    protected static final Log log = LogFactory.getLog(WorkflowBotInvoker.class);

    private final Set<BotThread> botThreadSet = new HashSet<BotThread>();

    public void invokeBots() {
        // lock
        try {
            updateConfig(null);
        } catch (BotInvokerException e) {
            log.error("Failed to update config", e);
        }
        synchronized (botThreadSet) {
            for (BotThread botThread : botThreadSet) {
                checkTaskListSizeNotExeeded(botThread);
                if (!botThread.isAlive()) {
                    botThread.start();
                }
            }
        }
    }

    private void checkTaskListSizeNotExeeded(BotThread botThread) {
        try {
            WorkflowMultitaskBot bot = botThread.getBot();
            int taskListSize = bot.getTaskListSize();
            int allowedTaskList = bot.getMaxAllowedTaskListSize();
            if (taskListSize > allowedTaskList) {
                log.warn("max allowed task list size exeeded for bot:" + bot);
            }
        } catch (BotException e) {
            log.error("bots execution error", e);
        }
    }

    private long configurationVersion = -1;
    private List<WorkflowMultitaskBot> workflowBots = null;

    public void init(String configurationPath) {
    }

    public void updateConfig(String configurationPath) throws BotInvokerException {
        try {
            WorkflowBotConfigurationParser configurationParser = new WorkflowBotConfigurationParser();
            Subject subject = DelegateFactory.getInstance().getAuthenticationService().authenticate(configurationParser.login,
                    configurationParser.password);
            BotStation pattern = new BotStation(configurationParser.login);
            BotsService botsService = DelegateFactory.getInstance().getBotsService();
            BotStation THIS = botsService.getBotStation(subject, pattern);
            if (THIS == null) {
                log.error("botstation with name " + configurationParser.login + " doesn`t exist");
                throw new NullPointerException();
            }
            if (THIS.getVersion() != configurationVersion) {
                log.info("updating bots configuration");
                workflowBots = new ArrayList<WorkflowMultitaskBot>();
                List<Bot> bots = botsService.getBotList(subject, THIS);
                for (Bot bot : bots) {
                    Subject botSubject = DelegateFactory.getInstance().getAuthenticationService().authenticate(bot.getWfeUser(),
                            bot.getWfePass());
                    WorkflowMultitaskBot wbot = new WorkflowMultitaskBot(botSubject, 150, bot.getLastInvoked());
                    List<BotTask> tasks = botsService.getBotTaskList(subject, bot);
                    Iterator<BotTask> i = tasks.iterator();
                    while (i.hasNext()) {
                        BotTask task = i.next();
                        try {
                            TaskHandler handler = (TaskHandler) Class.forName(task.getClazz()).newInstance();
                            handler.configure(task.getConfiguration());
                            wbot.addTask(task.getName(), handler, 0);
                            log.info("Configured task handler for " + task.getName());
                        } catch (Exception e) {
                            log.error("bot task error", e);
                        }
                    }
                    workflowBots.add(wbot);
                }
                configurationVersion = THIS.getVersion();
            } else {
                log.debug("bots configuration is up to date");
            }
        } catch (WorkflowBotConfigurationParserException e) {
            log.error("cannot read login and password from botstation.xml", e);
        } catch (Exception e) {
            log.error("bots configuration updating error", e);
        }
        if (configurationVersion != -1) {
            synchronized (botThreadSet) {
                for (WorkflowMultitaskBot workflowMultitaskBot : workflowBots) {
                    boolean isStarted = false;
                    for (BotThread thread : botThreadSet) {
                        if (thread.hashCode() == workflowMultitaskBot.getSubject().hashCode()) {
                            isStarted = true;
                            break;
                        }
                    }
                    if (!isStarted) {
                        botThreadSet.add(new BotThread(workflowMultitaskBot));
                    }
                }
            }
        }
    }

    private class BotThread extends Thread {
        private final WorkflowMultitaskBot bot;

        public WorkflowMultitaskBot getBot() {
            return bot;
        }

        public BotThread(WorkflowMultitaskBot bot) {
            this.bot = bot;
        }

        @Override
        public void run() {
            try {
                bot.execute();
                log.debug("Executed bot:" + bot);
            } catch (Exception e) {
                log.error("Error during execution of bot: " + bot, e);
            }
            synchronized (botThreadSet) {
                botThreadSet.remove(this);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if ((obj == null) || !(obj instanceof BotThread)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            BotThread botThread = (BotThread) obj;
            if (bot.getSubject().equals(botThread.getBot().getSubject())) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return bot.getSubject().hashCode();
        }
    }
}
