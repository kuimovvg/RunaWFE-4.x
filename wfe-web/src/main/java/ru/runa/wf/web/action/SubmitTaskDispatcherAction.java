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
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.WebResources;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Profile;

import com.google.common.base.Objects;

/**
 * Created on 20.04.2008
 * 
 * @struts:action path="/submitTaskDispatcher" name="idForm" validate="false"
 * @struts.action-forward name="tasksList" path="/manage_tasks.do" redirect = "true"
 * @struts.action-forward name="submitTask" path="/submit_task.do" redirect = "false"
 * @struts.action-forward name="executeTask" path="/submitTaskForm.do" redirect = "false"
 */
public class SubmitTaskDispatcherAction extends Action {
    private static final Log log = LogFactory.getLog(SubmitTaskDispatcherAction.class);

    private final String LOCAL_FORWARD_TASKS_LIST = "tasksList";

    private final String LOCAL_FORWARD_SUBMIT_TASK = "submitTask";

    private final String LOCAL_FORWARD_EXECUTE_TASK = "executeTask";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String forwardName = LOCAL_FORWARD_SUBMIT_TASK;
        Map<String, Object> params = new HashMap<String, Object>();

        saveToken(request);

        String executeTask = request.getParameter(WebResources.ACTION_MAPPING_SUBMIT_TASK_DISPATCHER);
        if (executeTask != null) {
            log.debug("User should be redirected to /submitTaskForm.do action.");
            params.put(ProcessForm.ID_INPUT_NAME, request.getParameter(ProcessForm.ID_INPUT_NAME));
            forwardName = LOCAL_FORWARD_EXECUTE_TASK;
        } else {
            log.debug("User should be redirected to /submit_task.do action.");
        }

        ActionMessages errors = getErrors(request);
        IdForm idForm = (IdForm) form;
        try {
            ExecutionService executionService = Delegates.getExecutionService();
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
            BatchPresentation batchPresentation = profile.getActiveBatchPresentation("listTasksForm").clone();
            List<WfTask> tasks = executionService.getTasks(subject, batchPresentation);
            WfTask currentTask = null;
            Long currentTaskId = idForm.getId();
            for (WfTask task : tasks) {
                if (Objects.equal(task.getId(), currentTaskId)) {
                    currentTask = task;
                    break;
                }
            }
            if (currentTask == null) {
                throw new TaskAlreadyAcceptedException(request.getParameter(ProcessForm.ID_INPUT_NAME));
            }
            if (currentTask.isFirstOpen()) {
                executionService.markTaskOpened(subject, currentTask.getId());
            }
        } catch (TaskAlreadyAcceptedException e) {
            // forward user to the tasks list screen cause current task was already accepted by another user...
            forwardName = LOCAL_FORWARD_TASKS_LIST;
            ActionExceptionHelper.addException(errors, e);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }
        return Commons.forward(mapping.findForward(forwardName), params);
    }

}
