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

import java.util.HashMap;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Created on 04.03.2005
 * 
 */
public class DoNothingTaskHandler implements TaskHandler {
    private static final Log log = LogFactory.getLog(DoNothingTaskHandler.class);

    @Override
    public void configure(String bundleName) {
    }

    @Override
    public void handle(Subject subject, IVariableProvider variableProvider, WfTask wfTask) throws TaskHandlerException {
        try {
            DelegateFactory.getExecutionService().completeTask(subject, wfTask.getId(), new HashMap<String, Object>());
            log.info("Do nothing task handler have done task : " + wfTask);
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }

    }

    @Override
    public void configure(byte[] configuration) throws TaskHandlerException {
    }

}
