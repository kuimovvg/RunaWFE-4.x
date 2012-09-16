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
package ru.runa.af.service.bot.impl.ejb;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.bot.BotInvokerException;
import ru.runa.af.logic.bot.BotInvoker;
import ru.runa.af.logic.bot.BotInvokerFactory;
import ru.runa.af.service.bot.BotInvokerService;
import ru.runa.af.service.impl.ejb.LoggerInterceptor;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class BotInvokerServiceBean implements BotInvokerService {
    private static final Log log = LogFactory.getLog(BotInvokerServiceBean.class);

    private transient static Timer timer;

    @Override
    public void startPeriodicBotsInvocation() {
        if (timer == null) {
            log.info("Starting periodic bot execution...");
            timer = new Timer();
            timer.schedule(new IvokerTimerTask(), 0, BotInvokerFactory.getBotInvocationPeriod());
        } else {
            log.info("Bot is running. skipping start...");
        }
    }

    @Override
    public boolean isRunning() {
        return timer != null;
    }

    @Override
    public void cancelPeriodicBotsInvocation() {
        if (timer != null) {
            log.info("Canceling periodic bot execution...");
            timer.cancel();
            timer = null;
        } else {
            log.info("Bot is not running. skipping cancel...");
        }
    }

    @Override
    public void invokeBots() throws BotInvokerException {
        BotInvoker botIvoker = BotInvokerFactory.getBotInvoker();
        URL confFile = BotInvoker.class.getResource(BotInvoker.DEFAUL_CONFIGURATION_PATH);
        botIvoker.init(confFile == null ? null : confFile.toString());
        botIvoker.invokeBots();
    }

    private static class IvokerTimerTask extends TimerTask {
        private BotInvokerService service;

        public IvokerTimerTask() {
            try {
                InitialContext initialContext = new InitialContext();
                service = (BotInvokerService) initialContext.lookup("BotInvokerService/local");
            } catch (NamingException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        public void run() {
            try {
                log.debug("Invoking bots...");
                service.invokeBots();
            } catch (Throwable th) {
                log.error("Unable to invoke bots", th);
            }
        }
    }
}
