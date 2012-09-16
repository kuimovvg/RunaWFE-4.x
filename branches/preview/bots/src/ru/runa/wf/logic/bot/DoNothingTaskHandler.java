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

import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskStub;

/**
 * Created on 04.03.2005
 * 
 */
public class DoNothingTaskHandler implements TaskHandler {
    private static final Log log = LogFactory.getLog(DoNothingTaskHandler.class);

    public void configure(String bundleName) {
    }

    public void handle(Subject subject, TaskStub taskStub) throws TaskHandlerException {
        try {
            DelegateFactory.getInstance().getExecutionService().completeTask(subject, taskStub.getId(), taskStub.getName(),
                    taskStub.getTargetActor().getId(), new HashMap<String, Object>());
            log.info("Do nothing task hanlder have done  task : " + taskStub);
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }

    }

    public void configure(byte[] configuration) throws TaskHandlerException {
    }

}
