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

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.common.WebResources;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.delegate.Delegates;
import ru.runa.wf.web.TaskFormBuilder;
import ru.runa.wf.web.html.FormBuilderFactory;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;

/**
 * Created on 17.11.2004
 * 
 * @jsp.tag name = "taskForm" body-content = "empty"
 */

public class TaskFormTag extends WFFormTag {
    private static final long serialVersionUID = -8864271538433581304L;

    private Long taskId;

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public List<String> getTransitionNames() throws AuthenticationException, TaskDoesNotExistException {
        List<String> result = Delegates.getDefinitionService().getOutputTransitionNames(getUser(), null, taskId);
        result.remove(Transition.TIMEOUT_TRANSITION_NAME);
        return result;
    }

    @Override
    protected List<String> getFormButtonNames() {
        return getTransitionNames();
    }

    @Override
    protected boolean isMultipleSubmit() {
        return getTransitionNames().size() > 1;
    }

    @Override
    protected Long getDefinitionId() throws AuthorizationException, AuthenticationException {
        return Delegates.getExecutionService().getTask(getUser(), taskId).getDefinitionId();
    }

    @Override
    protected Interaction getInteraction() throws AuthorizationException, AuthenticationException, TaskDoesNotExistException {
        return Delegates.getDefinitionService().getTaskInteraction(getUser(), taskId);
    }

    @Override
    protected String buildForm(Interaction interaction) throws Exception {
        TaskFormBuilder taskFormBuilder = FormBuilderFactory.createTaskFormBuilder(interaction.getType());
        WfTask task = Delegates.getExecutionService().getTask(getUser(), taskId);
        return taskFormBuilder.build(getUser(), pageContext, interaction, task);
    }

    @Override
    protected void fillFormElement(TD tdFormElement) throws JspException {
        super.fillFormElement(tdFormElement);
        tdFormElement.addElement(new Input(Input.HIDDEN, IdForm.ID_INPUT_NAME, String.valueOf(taskId)));
        tdFormElement.addElement(new Input(Input.HIDDEN, WebResources.ACTION_MAPPING_SUBMIT_TASK_DISPATCHER, "redirectEnabled"));
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.EXECUTE_TASK_PARAMETER;
    }
}
