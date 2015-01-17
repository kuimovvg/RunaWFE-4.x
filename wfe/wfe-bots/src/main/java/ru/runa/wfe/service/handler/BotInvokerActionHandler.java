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
package ru.runa.wfe.service.handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.runa.wf.logic.bot.BotStationResources;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.service.delegate.BotInvokerServiceDelegate;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * Starts bot invocation at specified server.
 * 
 * @since 2.0
 */
public class BotInvokerActionHandler extends ActionHandlerBase {
    private static final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    private static final Map<BotStation, Future<?>> invocations = Maps.newHashMap();

    @Override
    public void execute(ExecutionContext executionContext) {
        try {
            List<BotStation> botStations = Delegates.getBotService().getBotStations();
            BotStation botStation = null;
            if (!Strings.isNullOrEmpty(configuration)) {
                // old way: search by address
                for (BotStation bs : botStations) {
                    if (configuration.equals(bs.getAddress())) {
                        botStation = bs;
                        break;
                    }
                }
                if (botStation == null) {
                    botStation = Delegates.getBotService().getBotStationByName(configuration);
                }
            } else {
                if (botStations.size() > 0) {
                    botStation = botStations.get(0);
                }
            }
            if (botStation == null) {
                log.warn("No botstation can be found for invocation " + configuration);
                return;
            }
            synchronized (botStation) {
                Future<?> invocation = invocations.get(botStation);
                if (invocation != null && !invocation.isDone()) {
                    log.debug("invocation != null && !invocation.isDone() for " + botStation);
                    return;
                }
                long timeout = BotStationResources.getBotInvokerHandlerTimeout();
                log.info("Scheduling invocation of " + botStation + " for " + timeout + "ms");
                invocation = scheduledExecutorService.schedule(new InvokerRunnable(botStation), timeout, TimeUnit.MILLISECONDS);
                invocations.put(botStation, invocation);
            }
        } catch (Exception e) {
            log.error("Unable to invoke bot station due to " + e);
        }
    }

    private class InvokerRunnable implements Runnable {
        private final BotStation botStation;

        public InvokerRunnable(BotStation botStation) {
            this.botStation = botStation;
        }

        @Override
        public void run() {
            try {
                log.info("Invoking " + botStation);
                BotInvokerServiceDelegate.getService(botStation).invokeBots(botStation);
            } catch (Exception e) {
                log.warn("Unable to invoke " + botStation, e);
            }
        }

    }

}
