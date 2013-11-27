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

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.common.WebResources;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;

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

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        BatchPresentation batchPresentation = getProfile().getActiveBatchPresentation(BatchPresentationConsts.ID_TASKS).clone();
        batchPresentation.setFieldsToGroup(new int[0]);
        WfTask task = Delegates.getExecutionService().getTask(getUser(), getTaskId());
        if (task.isGroupAssigned()) {
            setButtonEnabled(true);
        } else {
            setButtonEnabled(false);
        }
        String url = getReturnAction() + "?" + IdForm.ID_INPUT_NAME + "=" + taskId + "&" + ProcessForm.ACTOR_ID_INPUT_NAME + "=" + actorId;
        tdFormElement.addElement(ListTasksFormTag.buildTasksTable(pageContext, batchPresentation, Lists.newArrayList(task), url, true));

        tdFormElement.addElement(new Input(Input.HIDDEN, IdForm.ID_INPUT_NAME, String.valueOf(taskId)));
        tdFormElement.addElement(new Input(Input.HIDDEN, WebResources.HIDDEN_ONE_TASK_INDICATOR, WebResources.HIDDEN_ONE_TASK_INDICATOR));
        if (task.getOwner() != null) {
            tdFormElement.addElement(new Input(Input.HIDDEN, WebResources.HIDDEN_TASK_PREVIOUS_OWNER_ID, task.getOwner().getId().toString()));
        }
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