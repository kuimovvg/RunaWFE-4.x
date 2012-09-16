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
package ru.runa.af.logic.bot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.InternalApplicationException;

/**
 * Created on 23.03.2005
 * 
 */
public class BotInvokerFactory {
    private static BotInvoker BOT_INVOKER_INSTANCE = null;

    private static final Log log = LogFactory.getLog(BotInvokerFactory.class);

    public static BotInvoker getBotInvoker() {
        try {
            if (BOT_INVOKER_INSTANCE == null) {
                BOT_INVOKER_INSTANCE = getBotInvokerClass().newInstance();
                log.info("Using " + BOT_INVOKER_INSTANCE.getClass().getName());
            }
            return BOT_INVOKER_INSTANCE;
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends BotInvoker> getBotInvokerClass() throws ClassNotFoundException {
        return (Class<? extends BotInvoker>) Class.forName(Resources.getBotInvokerClassName());
    }

    private static final long MILLISECONDS_IN_SEC = 1000;
    private static final long DEFAULT_INVOCATION_PERIOD = MILLISECONDS_IN_SEC * 30;

    public static long getBotInvocationPeriod() {
        long period = DEFAULT_INVOCATION_PERIOD;
        try {
            period = Long.parseLong(Resources.getBotInvocationPeriod()) * MILLISECONDS_IN_SEC;
            if (period < MILLISECONDS_IN_SEC) {
                log.warn("bot_ivoker.properies invocation.period is less than 1 sec. Invocation period was set to 30 sec.");
                period = DEFAULT_INVOCATION_PERIOD;
            } else {
                log.info("Invocation period was set to " + Resources.getBotInvocationPeriod() + " sec.");
            }
        } catch (NumberFormatException e) {
            log.warn("bot_ivoker.properies invocation.period does not represent a number. Invocation period was set to 30 sec.");
        }
        return period;
    }

    /**
     * prevents instantiation
     */
    private BotInvokerFactory() {
        // prevents creation
    }
}
