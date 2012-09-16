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
package ru.runa.wf.jbpm.delegation.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.ConfigurationException;
import ru.runa.af.delegate.bot.impl.BotInvokerServiceDelegateRemoteImpl;
import ru.runa.bpm.graph.def.ActionHandler;
import ru.runa.bpm.graph.exe.ExecutionContext;

/**
 * Starts bot invocation at specified server.
 * 
 * Created on Jun 14, 2006
 *
 */
public class BotInvokerActionHandler implements ActionHandler {
    private static final long serialVersionUID = 4609336588375872230L;
    private static final Log log = LogFactory.getLog(BotInvokerActionHandler.class);
    private String configuration;

    @Override
    public void setConfiguration(String configuration) throws ConfigurationException {
        this.configuration = configuration;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        try {
            BotInvokerServiceDelegateRemoteImpl remoteImpl = new BotInvokerServiceDelegateRemoteImpl(configuration);
            remoteImpl.invokeBots();
        } catch (Throwable e) {
            log.warn("Bot invoker can't invoke bots.", e);
        }
    }

}
