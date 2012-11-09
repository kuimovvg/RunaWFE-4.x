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
import java.util.List;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wf.logic.bot.assigner.AssignerResources;
import ru.runa.wfe.os.OrgFunctionHelper;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.IVariableProvider;

public class SwimlaneAssignerTaskHandler implements TaskHandler {
    private static final Log log = LogFactory.getLog(SwimlaneAssignerTaskHandler.class);

    private AssignerResources resources;

    @Override
    public void configure(String configurationName) throws TaskHandlerException {
        resources = new AssignerResources(configurationName);
    }

    @Override
    public void configure(byte[] configuration) throws TaskHandlerException {
        resources = new AssignerResources(configuration);
    }

    @Override
    public void handle(Subject subject, IVariableProvider variableProvider, WfTask wfTask) throws TaskHandlerException {
        try {
            log.debug("SwimlaneAssigner started, task " + wfTask);
            ExecutionService executionService = DelegateFactory.getExecutionService();

            String swimlaneName = resources.getSwimlaneName();
            Long actorCode = SubjectPrincipalsHelper.getActor(subject).getCode();
            List<? extends Executor> executors = OrgFunctionHelper.evaluateOrgFunction(variableProvider, resources.getAssignerFunction(), actorCode);
            if (executors.size() != 1) {
                throw new TaskHandlerException("assigner (organization) function return more than 1 actor to be assigned in swimlane");
            }
            executionService.assignSwimlane(subject, wfTask.getProcessId(), swimlaneName, executors.get(0));
            executionService.completeTask(subject, wfTask.getId(), new HashMap<String, Object>());
            log.debug("SwimlaneAssigner finished, task " + wfTask);
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }
}
