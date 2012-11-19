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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.BotService;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotRunnerException;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.invoker.BotInvoker;
import ru.runa.wfe.commons.ClassLoaderUtil;

import com.google.common.collect.Sets;

/**
 * Created on 04.03.2005
 */
public class WorkflowBotInvoker implements BotInvoker {
    private static final Log log = LogFactory.getLog(WorkflowBotInvoker.class);
    private final Set<BotThread> botThreads = Sets.newHashSet();

    @Override
    public void invokeBots(BotStation botStation) {
        // lock
        updateConfig(botStation);
        synchronized (botThreads) {
            for (BotThread botThread : botThreads) {
                checkTaskListSizeNotExeeded(botThread);
                if (!botThread.isAlive()) {
                    botThread.start();
                }
            }
        }
    }

    private void checkTaskListSizeNotExeeded(BotThread botThread) {
        try {
            MultitaskBotRunner bot = botThread.getBot();
            int taskListSize = bot.getTaskListSize();
            int allowedTaskList = bot.getMaxAllowedTaskListSize();
            if (taskListSize > allowedTaskList) {
                log.warn("max allowed task list size exeeded for bot:" + bot);
            }
        } catch (BotRunnerException e) {
            log.error("bots execution error", e);
        }
    }

    private long configurationVersion = -1;
    private List<MultitaskBotRunner> workflowBots = null;

    public void updateConfig(BotStation botStation) {
        try {
            BotService botService = DelegateFactory.getBotService();
            if (botStation.getVersion() != configurationVersion) {
                log.info("updating bots configuration");
                workflowBots = new ArrayList<MultitaskBotRunner>();
                String username = BotStationResources.getSystemUsername();
                String password = BotStationResources.getSystemPassword();
                Subject botStationSubject = DelegateFactory.getAuthenticationService().authenticate(username, password);
                List<Bot> bots = botService.getBots(botStationSubject, botStation.getId());
                for (Bot bot : bots) {
                    Subject subject = DelegateFactory.getAuthenticationService().authenticate(bot.getUsername(), bot.getPassword());
                    MultitaskBotRunner wbot = new MultitaskBotRunner(subject, 150, bot.getLastInvoked());
                    List<BotTask> tasks = botService.getBotTasks(subject, bot.getId());
                    Iterator<BotTask> i = tasks.iterator();
                    while (i.hasNext()) {
                        BotTask task = i.next();
                        try {
                            TaskHandler handler = ClassLoaderUtil.instantiate(task.getTaskHandlerClassName());
                            handler.configure(task.getConfiguration());
                            wbot.addTask(task.getName(), handler, 0);
                            log.info("Configured task handler for " + task.getName());
                        } catch (Exception e) {
                            log.error("bot task error", e);
                        }
                    }
                    workflowBots.add(wbot);
                }
                configurationVersion = botStation.getVersion();
            } else {
                log.debug("bots configuration is up to date");
            }
        } catch (Exception e) {
            log.error("bots configuration update error", e);
        }
        if (configurationVersion != -1) {
            synchronized (botThreads) {
                for (MultitaskBotRunner workflowMultitaskBot : workflowBots) {
                    boolean isStarted = false;
                    for (BotThread thread : botThreads) {
                        if (thread.hashCode() == workflowMultitaskBot.getSubject().hashCode()) {
                            isStarted = true;
                            break;
                        }
                    }
                    if (!isStarted) {
                        botThreads.add(new BotThread(workflowMultitaskBot));
                    }
                }
            }
        }
    }

    private class BotThread extends Thread {
        private final MultitaskBotRunner bot;

        public MultitaskBotRunner getBot() {
            return bot;
        }

        public BotThread(MultitaskBotRunner bot) {
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
            synchronized (botThreads) {
                botThreads.remove(this);
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
