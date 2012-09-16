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

import javax.security.auth.Subject;
import javax.servlet.jsp.JspException;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationNotFoundException;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.Resources;
import ru.runa.wf.web.form.ProcessForm;

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

    private Long actorId;

    private String taskName;

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

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long executorId) {
        actorId = executorId;
    }

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) throws JspException {
        try {
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            Subject subject = getSubject();
            BatchPresentation batchPresentation = getProfile().getActiveBatchPresentation("listTasksForm").clone();
            batchPresentation.setFieldsToGroup(new int[0]);
            TaskStub current = getTask(executionService, subject);
            if (current == null) {
                throw new TaskDoesNotExistException(getTaskId());
            }
            if (current.isGroupAssigned()) {
                setButtonEnabled(true);
            } else {
                setButtonEnabled(false);
            }
            tdFormElement.addElement(ListTasksFormTag.buildTasksTable(pageContext, batchPresentation, Lists.newArrayList(current), getReturnAction()
                    + "?" + IdForm.ID_INPUT_NAME + "=" + taskId + "&" + ProcessForm.ACTOR_ID_INPUT_NAME + "=" + actorId + "&"
                    + ProcessForm.TASK_INPUT_NAME + "=" + taskName, true));

            tdFormElement.addElement(new Input(Input.HIDDEN, IdForm.ID_INPUT_NAME, String.valueOf(taskId)));
            tdFormElement.addElement(new Input(Input.HIDDEN, ProcessForm.ACTOR_ID_INPUT_NAME, String.valueOf(actorId)));
            tdFormElement.addElement(new Input(Input.HIDDEN, ProcessForm.TASK_INPUT_NAME, taskName));
            tdFormElement.addElement(new Input(Input.HIDDEN, Resources.HIDDEN_ONE_TASK_INDICATOR, Resources.HIDDEN_ONE_TASK_INDICATOR));
            tdFormElement.addElement(new Input(Input.HIDDEN, Resources.HIDDEN_TASK_SWIMLANE, String.valueOf(current.getName())));
        } catch (Exception e) {
            handleException(e);
        }
    }

    private TaskStub getTask(ExecutionService executionService, Subject subject) throws AuthorizationException,
            AuthenticationException, BatchPresentationNotFoundException {
        List<TaskStub> tasks = executionService.getTasks(subject, getProfile().getActiveBatchPresentation("listTasksForm").clone());
        for (TaskStub taskStub : tasks) {
            if (Objects.equal(taskStub.getId(), getTaskId())) {
                return taskStub;
            }
        }
        return null;
    }

    @Override
    protected boolean isFormButtonEnabled() throws JspException {
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
