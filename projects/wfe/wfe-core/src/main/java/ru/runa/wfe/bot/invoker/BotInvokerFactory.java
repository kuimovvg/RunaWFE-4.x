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
package ru.runa.wfe.bot.invoker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.PropertyResources;

/**
 * Created on 23.03.2005
 * 
 */
public class BotInvokerFactory {
    private static final Log log = LogFactory.getLog(BotInvokerFactory.class);
    private static final PropertyResources RESOURCES = new PropertyResources("bot_invoker.properties");

    private static BotInvoker INSTANCE = null;

    private static String getBotInvokerClassName() {
        return RESOURCES.getStringPropertyNotNull("BotInvoker.class");
    }

    public static BotInvoker getBotInvoker() {
        if (INSTANCE == null) {
            INSTANCE = ClassLoaderUtil.instantiate(getBotInvokerClassName());
            log.info("Using " + INSTANCE.getClass().getName());
        }
        return INSTANCE;
    }

    public static long getBotInvocationPeriod() {
        long periodInSeconds = RESOURCES.getLongProperty("invocation.period", 30);
        if (periodInSeconds < 1) {
            log.warn("bot_ivoker.properies invocation.period is less than 1 sec. Invocation period was set to 30 sec.");
            periodInSeconds = 30;
        } else {
            log.info("Invocation period was set to " + periodInSeconds + " sec.");
        }
        return periodInSeconds * 1000;
    }

}
