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

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Cancels process instance task belongs to.
 * 
 * Created on 09.11.2006
 * 
 * @author Vitaliy S
 */
public class CancelCurrentProcessTaskHandler implements TaskHandler {
    private static final Log log = LogFactory.getLog(CancelCurrentProcessTaskHandler.class);

    @Override
    public void configure(String configurationName) throws TaskHandlerException {
        // do nothing with configuration
    }

    @Override
    public void configure(byte[] configuration) throws TaskHandlerException {
        // do nothing with configuration
    }

    @Override
    public void handle(Subject subject, IVariableProvider variableProvider, WfTask wfTask) throws TaskHandlerException {
        try {
            ExecutionService executionService = DelegateFactory.getExecutionService();
            executionService.cancelProcess(subject, wfTask.getProcessId());
            log.info("Process instance " + wfTask.getProcessId() + " was canceled");
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }
}
