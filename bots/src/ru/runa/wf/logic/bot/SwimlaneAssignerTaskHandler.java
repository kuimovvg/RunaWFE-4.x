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

import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.af.Actor;
import ru.runa.af.organizationfunction.OrgFunctionHelper;
import ru.runa.af.service.ExecutorService;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskStub;
import ru.runa.wf.logic.bot.assigner.AssignerResources;
import ru.runa.wf.service.ExecutionService;

public class SwimlaneAssignerTaskHandler extends AbstractOrgFunctionTaskHandler {
    private static final Log log = LogFactory.getLog(SwimlaneAssignerTaskHandler.class);

    private AssignerResources resources;

    public void configure(String configurationName) throws TaskHandlerException {
        resources = new AssignerResources(configurationName);
    }

    public void configure(byte[] configuration) throws TaskHandlerException {
        resources = new AssignerResources(configuration);
    }

    public void handle(Subject subject, TaskStub taskStub) throws TaskHandlerException {
        try {
            log.debug("SwimlaneAssigner started, task " + taskStub);
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            Map<String, Object> variablesMap = executionService.getVariables(subject, taskStub.getId());

            String swimlaneName = resources.getSwimlaneName();
            List<Long> swimlaneValues = OrgFunctionHelper.evaluateOrgFunction(variablesMap, resources.getAssignerFunction(),
                    getActorToSubstituteCode(subject));
            if (swimlaneValues.size() != 1) {
                throw new TaskHandlerException("assigner (organization) function return more than 1 actor to be assigned in swimlane");
            }

            ExecutorService executorService = ru.runa.delegate.DelegateFactory.getInstance().getExecutorService();
            Actor actor = executorService.getActor(subject, swimlaneValues.get(0));

            variablesMap.put(swimlaneName, String.valueOf(actor.getCode()));

            executionService.completeTask(subject, taskStub.getId(), taskStub.getName(), taskStub.getTargetActor().getId(), variablesMap);
            log.debug("SwimlaneAssigner finished, task " + taskStub);
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }
}
