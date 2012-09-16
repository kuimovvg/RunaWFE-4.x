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
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionFormException;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.web.html.FormBuilderFactory;
import ru.runa.wf.web.html.StartFormBuilder;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

/**
 * Created on 17.11.2004
 * 
 * @jsp.tag name = "startForm" body-content = "empty"
 */
public class StartFormTag extends WFFormTag {

    private static final long serialVersionUID = -1162637745236395968L;
    private Long definitionId;

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    @Override
    protected Long getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(Long definitionId) {
        this.definitionId = definitionId;
    }

    @Override
    protected String buildForm(Interaction interaction) throws AuthenticationException, WorkflowFormProcessingException, AuthorizationException,
            TaskDoesNotExistException {
        try {
            StartFormBuilder startFormBuilder = FormBuilderFactory.createStartFormBuilder(getFormBuilderType());
            return startFormBuilder.build(getSubject(), getDefinitionId(), pageContext, interaction);
        } catch (AuthenticationException e) {
            throw e;
        } catch (WorkflowFormProcessingException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new WorkflowFormProcessingException(e);
        } catch (ProcessDefinitionFormException e) {
            throw new WorkflowFormProcessingException(e);
        }
    }

    @Override
    protected Interaction getInteraction() throws AuthorizationException, AuthenticationException {
        try {
            return DelegateFactory.getInstance().getDefinitionService().getStartInteraction(getSubject(), definitionId);
        } catch (ProcessDefinitionDoesNotExistException e) {
            throw new InternalApplicationException(e);
        }
    }

    public List<String> getTransitionNames() throws AuthenticationException, TaskDoesNotExistException {
        return DelegateFactory.getInstance().getDefinitionService().getOutputTransitionNames(getSubject(), definitionId, null);
    }

    @Override
    protected List<String> getFormButtonNames() {
        try {
            return getTransitionNames();
        } catch (AuthenticationException e) {
            throw new InternalApplicationException(e);
        } catch (TaskDoesNotExistException e) {
            throw new InternalApplicationException(e);
        }
    }

    @Override
    protected boolean isMultipleSubmit() {
        try {
            return getTransitionNames().size() > 1;
        } catch (AuthenticationException e) {
            throw new InternalApplicationException(e);
        } catch (TaskDoesNotExistException e) {
            throw new InternalApplicationException(e);
        }
    }

    @Override
    protected void fillFormElement(TD tdFormElement) throws JspException {
        super.fillFormElement(tdFormElement);
        tdFormElement.addElement(new Input(Input.HIDDEN, IdForm.ID_INPUT_NAME, String.valueOf(definitionId)));
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.LABEL_START_INSTANCE, pageContext);
    }

    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.START_INSTANCE_PARAMETER;
    }
}
