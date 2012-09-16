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

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionFormException;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.web.Resources;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wf.web.html.FormBuilderFactory;
import ru.runa.wf.web.html.TaskFormBuilder;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

/**
 * Created on 17.11.2004
 * 
 * @jsp.tag name = "taskForm" body-content = "empty"
 */

public class TaskFormTag extends WFFormTag {

    private static final long serialVersionUID = -8864271538433581304L;

    private Long taskId;

    private Long actorId;

    private String taskName;

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    private Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public List<String> getTransitionNames() throws AuthenticationException, TaskDoesNotExistException {
        List<String> result = DelegateFactory.getInstance().getDefinitionService().getOutputTransitionNames(getSubject(), null, taskId);
        result.remove("time-out-transition");
        return result;
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
    private String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Override
    protected List<String> getFormButtonNames() {
        try {
            return getTransitionNames();
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    @Override
    protected boolean isMultipleSubmit() {
        try {
            return getTransitionNames().size() > 1;
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    @Override
    protected Long getDefinitionId() throws AuthorizationException, AuthenticationException {
        return DelegateFactory.getInstance().getExecutionService().getTask(getSubject(), taskId).getProcessDefinitionId();
    }

    @Override
    protected Interaction getInteraction() throws AuthorizationException, AuthenticationException, TaskDoesNotExistException {
        return DelegateFactory.getInstance().getDefinitionService().getTaskInteraction(getSubject(), taskId, taskName);
    }

    @Override
    protected String buildForm(Interaction interaction) throws AuthenticationException, WorkflowFormProcessingException, AuthorizationException,
            TaskDoesNotExistException {
        try {
            TaskFormBuilder taskFormBuilder = FormBuilderFactory.createTaskFormBuilder(getFormBuilderType());
            return taskFormBuilder.build(getSubject(), getTaskId(), getTaskName(), pageContext, interaction);
        } catch (ProcessDefinitionFormException e) {
            throw new WorkflowFormProcessingException(e);
        }
    }

    @Override
    protected void fillFormElement(TD tdFormElement) throws JspException {
        super.fillFormElement(tdFormElement);
        tdFormElement.addElement(new Input(Input.HIDDEN, IdForm.ID_INPUT_NAME, String.valueOf(taskId)));
        tdFormElement.addElement(new Input(Input.HIDDEN, ProcessForm.ACTOR_ID_INPUT_NAME, String.valueOf(actorId)));
        tdFormElement.addElement(new Input(Input.HIDDEN, ProcessForm.TASK_INPUT_NAME, taskName));
        tdFormElement.addElement(new Input(Input.HIDDEN, Resources.ACTION_MAPPING_SUBMIT_TASK_DISPATCHER, "redirectEnabled"));
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.EXECUTE_TASK_PARAMETER;
    }
}
