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

import java.util.Map;

import javax.security.auth.Subject;

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.handler.bot.TaskHandler;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Cancels process task belongs to.
 * 
 * Created on 09.11.2006
 * 
 * @author Vitaliy S
 * @since 2.0
 */
public class CancelCurrentProcessTaskHandler implements TaskHandler {

    @Override
    public void setConfiguration(byte[] configuration) {
        // do nothing with configuration
    }

    @Override
    public Map<String, Object> handle(Subject subject, IVariableProvider variableProvider, WfTask wfTask) {
        DelegateFactory.getExecutionService().cancelProcess(subject, wfTask.getProcessId());
        return null;
    }
}
