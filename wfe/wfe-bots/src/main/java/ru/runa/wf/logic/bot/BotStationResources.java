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

import java.util.MissingResourceException;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.ResourceCommons;

/**
 * Bot station configuration.
 * 
 * @author dofs
 * @since 4.0
 */
public class BotStationResources extends ResourceCommons {
    private static final String BUNDLE = "botstation";

    private BotStationResources() {
        super(BUNDLE);
    }

    public static BotLogger createBotLogger() {
        try {
            String loggerClassName = readPropertyIfExist("bot.logger.class", BUNDLE);
            if (loggerClassName == null) {
                return null;
            }
            return (BotLogger) ClassLoaderUtil.instantiate(loggerClassName);
        } catch (MissingResourceException mbe) {
            return null;
        } catch (Throwable e) {
            throw new InternalApplicationException(e);
        }
    }

    public static String getBotStationName() {
        return readProperty("botstation.name", BUNDLE);
    }

    public static int getThreadPoolSize() {
        try {
            return Integer.parseInt(readPropertyIfExist("thread.pool.size", BUNDLE, "1"));
        } catch (Exception e) {
            return 1;
        }
    }

}
