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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.StrIdsForm;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

/**
 * Created on 02.04.2008
 * 
 * @struts:action path="/processTaskAssignment" name="strIdsForm"
 *                validate="false"
 * @struts.action-forward name="tasksList" path="/manage_tasks.do" redirect =
 *                        "true"
 * @struts.action-forward name="submitTask" path="/submit_task.do" redirect =
 *                        "false"
 */
public class ProcessTaskAssignmentAction extends ActionBase {
    private static final Log log = LogFactory.getLog(ProcessTaskAssignmentAction.class);

    public static final String ACTION_PATH = "/processTaskAssignment";

    private final String LOCAL_FORWARD_TASKS_LIST = "tasksList";

    private final String LOCAL_FORWARD_SUBMIT_TASK = "submitTask";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String forwardName = LOCAL_FORWARD_TASKS_LIST;
        ActionMessages errors = getErrors(request);
        StrIdsForm idsForm = (StrIdsForm) form;
        List<Long> taskIds = Lists.newArrayList();
        List<Executor> taskPreviousOwners = Lists.newArrayList();
        boolean isOneTaskProcessing = false;
        if (request.getParameter(ru.runa.common.WebResources.HIDDEN_ONE_TASK_INDICATOR) != null) {
            isOneTaskProcessing = true;
        }
        if (isOneTaskProcessing) {
            forwardName = LOCAL_FORWARD_SUBMIT_TASK;
            taskIds.add(Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME)));
            taskPreviousOwners.add(getExecutor(getLoggedUser(request),
                    request.getParameter(ru.runa.common.WebResources.HIDDEN_TASK_PREVIOUS_OWNER_ID)));
        } else {
            for (String strId : idsForm.getStrIds()) {
                String[] ids = strId.split(":", -1);
                taskIds.add(Long.parseLong(ids[0]));
                taskPreviousOwners.add(getExecutor(getLoggedUser(request), ids[1]));
            }
        }
        try {
            ExecutionService executionService = Delegates.getExecutionService();
            ProfileHttpSessionHelper.getProfile(request.getSession());
            for (int i = 0; i < taskIds.size(); i++) {
                try {
                    Long taskId = taskIds.get(i);
                    Executor previousExecutor = taskPreviousOwners.get(i);
                    executionService.assignTask(getLoggedUser(request), taskId, previousExecutor, getLoggedUser(request).getActor());
                } catch (TaskAlreadyAcceptedException e) {
                    // forward user to the tasks list screen cause current task
                    // was already accepted by another user...
                    forwardName = LOCAL_FORWARD_TASKS_LIST;
                    ActionExceptionHelper.addException(errors, e);
                } catch (Exception e) {
                    ActionExceptionHelper.addException(errors, e);
                }
            }
        } catch (Exception e) {
            log.error(e);
            ActionExceptionHelper.addException(errors, e);
        }
        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }
        return mapping.findForward(forwardName);
    }

    private Executor getExecutor(User user, String idString) {
        if (idString == null || "null".equals(idString)) {
            return null;
        }
        return Delegates.getExecutorService().getExecutor(user, Long.parseLong(idString));
    }
}
