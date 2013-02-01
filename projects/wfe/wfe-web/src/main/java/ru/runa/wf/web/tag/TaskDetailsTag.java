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
package ru.runa.wf.web.tag;

import java.util.List;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.common.WebResources;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * Created on 14.04.2008
 * 
 * @author YSK
 * @jsp.tag name = "taskDetails" body-content = "JSP"
 */
public class TaskDetailsTag extends BatchReturningTitledFormTag {
    private static final long serialVersionUID = -8864271538433581304L;

    private Long taskId;
    private boolean buttonEnabled = false;

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    private Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        ExecutionService executionService = Delegates.getExecutionService();
        BatchPresentation batchPresentation = getProfile().getActiveBatchPresentation("listTasksForm").clone();
        batchPresentation.setFieldsToGroup(new int[0]);
        WfTask current = getTask(executionService);
        if (current == null) {
            throw new TaskDoesNotExistException(getTaskId());
        }
        if (current.isGroupAssigned()) {
            setButtonEnabled(true);
        } else {
            setButtonEnabled(false);
        }
        tdFormElement.addElement(ListTasksFormTag.buildTasksTable(pageContext, batchPresentation, Lists.newArrayList(current), getReturnAction()
                + "?" + IdForm.ID_INPUT_NAME + "=" + taskId, true));

        tdFormElement.addElement(new Input(Input.HIDDEN, IdForm.ID_INPUT_NAME, String.valueOf(taskId)));
        tdFormElement.addElement(new Input(Input.HIDDEN, WebResources.HIDDEN_ONE_TASK_INDICATOR, WebResources.HIDDEN_ONE_TASK_INDICATOR));
        if (current.getOwner() != null) {
            tdFormElement.addElement(new Input(Input.HIDDEN, WebResources.HIDDEN_TASK_PREVIOUS_OWNER_ID, current.getOwner().getId().toString()));
        }
    }

    private WfTask getTask(ExecutionService executionService) {
        List<WfTask> tasks = executionService.getTasks(getUser(), getProfile().getActiveBatchPresentation("listTasksForm").clone());
        for (WfTask task : tasks) {
            if (Objects.equal(task.getId(), getTaskId())) {
                return task;
            }
        }
        return null;
    }

    @Override
    protected boolean isFormButtonEnabled() {
        return buttonEnabled;
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_ACCEPT_TASK, pageContext);
    }

    /**
     * @param buttonEnabled
     *            the buttonEnabled to set
     */
    public void setButtonEnabled(boolean buttonEnabled) {
        this.buttonEnabled = buttonEnabled;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.ACCEPT_TASK_PARAMETER;
    }
}
