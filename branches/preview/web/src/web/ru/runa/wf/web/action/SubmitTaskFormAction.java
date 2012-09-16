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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.Profile;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskStub;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.AutomaticFormOpenerResources;
import ru.runa.wf.web.form.ProcessForm;

import com.google.common.base.Objects;

/**
 * Created on 18.08.2004
 * 
 * @struts:action path="/submitTaskForm" name="processForm" validate="true"
 *                input = "/WEB-INF/wf/manage_tasks.jsp"
 * @struts.action-forward name="success" path="/manage_tasks.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure" path="/submit_task.do" redirect =
 *                        "false"
 * @struts.action-forward name="submitTask" path="/submit_task.do" redirect =
 *                        "false"
 * @struts.action-forward name="tasksList" path="/manage_tasks.do" redirect =
 *                        "true"
 */
public class SubmitTaskFormAction extends BaseProcessFormAction {

    @Override
    protected ActionForward executeProcessFromAction(HttpServletRequest request, ActionForm actionForm, ActionMapping mapping, Subject subject,
            Profile profile) throws Exception {
        ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
        DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
        ProcessForm form = (ProcessForm) actionForm;
        Long taskId = form.getId();
        Long actorId = form.getActorId();
        String taskName = form.getTaskName();
        Interaction wfForm = definitionService.getTaskInteraction(subject, taskId, taskName);
        Map<String, Object> variables = getFormVariables(request, actionForm, wfForm);

        BatchPresentation batchPresentation = profile.getActiveBatchPresentation("listTasksForm").clone();
        List<TaskStub> tasks = executionService.getTasks(subject, batchPresentation);
        Long processId = null;
        for (TaskStub task : tasks) {
            if (Objects.equal(task.getId(), taskId)) {
                processId = task.getProcessInstanceId();
            }
        }

        String transitionName = form.getSubmitButton();
        log.debug("User tries to complete task '" + taskName + "' in " + processId);
        executionService.completeTask(subject, taskId, taskName, actorId, variables, transitionName);

        AutomaticFormOpenerResources formOpenerRes = new AutomaticFormOpenerResources(AutoShowFormHelper.AutoShowFormPropertyFile);
        if (formOpenerRes.isAutoShowForm()) {
            ActionForward forward = AutoShowFormHelper.getNextActionForward(subject, mapping, profile, processId);
            if (forward != null) {
                return forward;
            }
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

    @Override
    protected ActionForward getErrorForward(ActionMapping mapping, ActionForm actionForm) {
        ProcessForm form = (ProcessForm) actionForm;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ProcessForm.ID_INPUT_NAME, String.valueOf(form.getId()));
        params.put(ProcessForm.ACTOR_ID_INPUT_NAME, String.valueOf(form.getActorId()));
        params.put(ProcessForm.TASK_INPUT_NAME, form.getTaskName());
        return Commons.forward(mapping.findForward(ru.runa.common.web.Resources.FORWARD_FAILURE), params);
    }

    @Override
    protected ActionMessage getMessage() {
        return new ActionMessage(Messages.TASK_COMPLETED);
    }
}
