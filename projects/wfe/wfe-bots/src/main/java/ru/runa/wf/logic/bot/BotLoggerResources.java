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
 * 
 * Created on 23.11.2006
 *
 */
public class BotLoggerResources extends ResourceCommons {

    private static final String BUNDLE = "botlogger";

    private static final String LOGGER_PROPERTY = "bot.logger.class";

    public static BotLogger createBotLogger() {
        try {
            String loggerClassName = readPropertyIfExist(LOGGER_PROPERTY, BUNDLE);
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

    private BotLoggerResources() {
        super(BUNDLE);
    }
}
