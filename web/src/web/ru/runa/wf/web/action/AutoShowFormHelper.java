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
package ru.runa.wf.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationNotFoundException;
import ru.runa.af.presentation.Profile;
import ru.runa.common.web.Commons;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.form.ProcessForm;

public class AutoShowFormHelper {

    private static final String LOCAL_FORWARD_TASKS_LIST = "tasksList";
    private static final String LOCAL_FORWARD_SUBMIT_TASK = "submitTask";

    public static final String AutoShowFormPropertyFile = "autoShowForm";

    public static ActionForward getNextActionForward(Subject subject, ActionMapping mapping, 
            Profile profile, Long processInstanceId) throws AuthorizationException, AuthenticationException, BatchPresentationNotFoundException {
        ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
        BatchPresentation batchPresentation = profile.getActiveBatchPresentation("listTasksForm").clone();
        List<TaskStub> tasks = executionService.getTasks(subject, batchPresentation);
        List<TaskStub> currentTaskStubs = new ArrayList<TaskStub>();
        for (TaskStub task : tasks) {
            if (task.getProcessInstanceId() == processInstanceId) {
                currentTaskStubs.add(task);
            }
        }

        if (currentTaskStubs.size() == 1) {
            Map<String, Object> params = new HashMap<String, Object>();
            TaskStub taskStub = currentTaskStubs.get(0);
            params.put(ProcessForm.ID_INPUT_NAME, String.valueOf(taskStub.getId()));
            params.put(ProcessForm.ACTOR_ID_INPUT_NAME, String.valueOf(taskStub.getTargetActor().getId()));
            params.put(ProcessForm.TASK_INPUT_NAME, taskStub.getName());

            return Commons.forward(mapping.findForward(LOCAL_FORWARD_SUBMIT_TASK), params);
        } else if (currentTaskStubs.size() > 1) {
            //list tasks
            return mapping.findForward(LOCAL_FORWARD_TASKS_LIST);
        }

        return null;
    }
}
