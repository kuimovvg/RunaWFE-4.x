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
package ru.runa.wfe.service.impl;

import java.util.Timer;
import java.util.TimerTask;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.invoker.BotInvokerFactory;
import ru.runa.wfe.service.BotInvokerService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Interceptors({ EjbExceptionSupport.class })
@WebService(name = "BotInvokerAPI", serviceName = "BotInvokerWebService")
@SOAPBinding
public class BotInvokerServiceBean implements BotInvokerService {
    private static final Log log = LogFactory.getLog(BotInvokerServiceBean.class);
    private transient static Timer timer;

    @Override
    public synchronized void startPeriodicBotsInvocation(BotStation botStation) {
        if (timer == null) {
            log.info("Starting periodic bot execution...");
            timer = new Timer();
            timer.schedule(new IvokerTimerTask(botStation), 0, BotInvokerFactory.getBotInvocationPeriod());
        } else {
            log.info("BotRunner is running. skipping start...");
        }
    }

    @Override
    public boolean isRunning() {
        return timer != null;
    }

    @Override
    public synchronized void cancelPeriodicBotsInvocation() {
        if (timer != null) {
            log.info("Canceling periodic bot execution...");
            timer.cancel();
            timer = null;
        } else {
            log.info("BotRunner is not running. skipping cancel...");
        }
    }

    @Override
    public void invokeBots(BotStation botStation) {
        BotInvokerFactory.getBotInvoker().invokeBots(botStation);
    }

    private static class IvokerTimerTask extends TimerTask {
        private final BotStation botStation;

        public IvokerTimerTask(BotStation botStation) {
            this.botStation = botStation;
        }

        @Override
        public void run() {
            try {
                log.debug("Invoking bots...");
                Delegates.getBotInvokerService().invokeBots(botStation);
            } catch (Throwable th) {
                log.error("Unable to invoke bots", th);
            }
        }
    }
}
