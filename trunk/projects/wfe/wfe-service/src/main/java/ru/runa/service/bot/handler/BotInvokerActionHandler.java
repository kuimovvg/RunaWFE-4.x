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
package ru.runa.service.bot.handler;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.handler.action.ActionHandler;

import com.google.common.base.Strings;

/**
 * Starts bot invocation at specified server.
 * 
 * @since 2.0
 */
public class BotInvokerActionHandler implements ActionHandler {
    private static final Log log = LogFactory.getLog(BotInvokerActionHandler.class);
    private String configuration;

    @Override
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
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
                botStation = Delegates.getBotService().getBotStation(configuration);
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
        Delegates.getBotInvokerService(botStation.getAddress()).invokeBots(botStation);
    }

}
