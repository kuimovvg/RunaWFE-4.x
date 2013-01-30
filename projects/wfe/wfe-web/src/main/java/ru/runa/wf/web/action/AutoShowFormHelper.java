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

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Commons;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;

import com.google.common.base.Objects;

public class AutoShowFormHelper {
    private static final String LOCAL_FORWARD_TASKS_LIST = "tasksList";
    private static final String LOCAL_FORWARD_SUBMIT_TASK = "submitTask";

    public static ActionForward getNextActionForward(User user, ActionMapping mapping, Profile profile, Long processId) {
        ExecutionService executionService = Delegates.getExecutionService();
        BatchPresentation batchPresentation = profile.getActiveBatchPresentation("listTasksForm").clone();
        List<WfTask> tasks = executionService.getTasks(user, batchPresentation);
        List<WfTask> currentTasks = new ArrayList<WfTask>();
        for (WfTask task : tasks) {
            if (Objects.equal(task.getProcessId(), processId)) {
                currentTasks.add(task);
            }
        }
        if (currentTasks.size() == 1) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(ProcessForm.ID_INPUT_NAME, currentTasks.get(0).getId());
            return Commons.forward(mapping.findForward(LOCAL_FORWARD_SUBMIT_TASK), params);
        } else if (currentTasks.size() > 1) {
            // list tasks
            return mapping.findForward(LOCAL_FORWARD_TASKS_LIST);
        }

        return null;
    }
}
