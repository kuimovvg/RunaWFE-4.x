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
import java.util.StringTokenizer;

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

import ru.runa.af.AuthenticationException;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.StrIdsForm;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskAlreadyAcceptedException;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.Resources;

/**
 * Created on 02.04.2008
 * 
 * @struts:action path="/processTaskAssignment" name="strIdsForm" validate="false"
 * @struts.action-forward name="tasksList" path="/manage_tasks.do" redirect = "true"
 * @struts.action-forward name="submitTask" path="/submit_task.do" redirect = "false"
 */
public class ProcessTaskAssignmentAction extends Action {
    private static final Log log = LogFactory.getLog(ProcessTaskAssignmentAction.class);

    public static final String ACTION_PATH = "/processTaskAssignment";

    private final String LOCAL_FORWARD_TASKS_LIST = "tasksList";

    private final String LOCAL_FORWARD_SUBMIT_TASK = "submitTask";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String forwardName = LOCAL_FORWARD_TASKS_LIST;
        ActionMessages errors = getErrors(request);
        StrIdsForm idsForm = (StrIdsForm) form;
        long[] processIdx = null;
        String[] swimlaneIdx = null;
        long[] actorIdx = null;
        boolean isOneTaskProcessing = false;
        if (request.getParameter(ru.runa.wf.web.Resources.HIDDEN_ONE_TASK_INDICATOR) != null) {
            isOneTaskProcessing = true;
        }
        if (isOneTaskProcessing) {
            forwardName = LOCAL_FORWARD_SUBMIT_TASK;
            processIdx = new long[] { Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME)) };
            swimlaneIdx = new String[] { request.getParameter(Resources.HIDDEN_TASK_SWIMLANE) };
            actorIdx = new long[] { Long.parseLong(request.getParameter(Resources.HIDDEN_ACTOR_ID_INPUT_NAME)) };
        } else {
            ArrayList<Long> processIdxList = new ArrayList<Long>();
            ArrayList<String> swimlaneIdxList = new ArrayList<String>();
            ArrayList<Long> actorIdxList = new ArrayList<Long>();
            for (String strId : idsForm.getStrIds()) {
                StringTokenizer token = new StringTokenizer(strId, ":");
                processIdxList.add(Long.parseLong(token.nextToken()));
                swimlaneIdxList.add(token.nextToken());
                actorIdxList.add(Long.parseLong(token.nextToken()));
            }
            processIdx = new long[processIdxList.size()];
            swimlaneIdx = new String[processIdxList.size()];
            actorIdx = new long[actorIdxList.size()];
            for (int i = 0; i < processIdxList.size(); ++i) {
                processIdx[i] = processIdxList.get(i).longValue();
                swimlaneIdx[i] = swimlaneIdxList.get(i);
                actorIdx[i] = actorIdxList.get(i);
            }

        }
        try {
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            ProfileHttpSessionHelper.getProfile(request.getSession());
            for (int i = 0; i < processIdx.length; i++) {
                try {
                    executionService.assignTask(subject, processIdx[i], swimlaneIdx[i], actorIdx[i]);
                } catch (TaskAlreadyAcceptedException e) {
                    // forward user to the tasks list screen cause current task was already accepted by another user...
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
}
