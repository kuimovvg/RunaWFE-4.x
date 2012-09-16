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

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.AuthenticationException;
import ru.runa.af.presentation.Profile;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.AutomaticFormOpenerResources;

/**
 * Created on 18.08.2004
 * 
 * @struts:action path="/startProcessInstance" name="idForm" validate="true" input = "/WEB-INF/wf/manage_process_definitions.jsp"
 * @struts.action-forward name="success" path="/manage_process_definitions.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_process_definitions.do" redirect = "true"
 * @struts.action-forward name="success_display_start_form" path="/submit_start_process_instance.do" redirect = "true"
 * @struts.action-forward name="submitTask" path="/submit_task.do" redirect = "false"
 * @struts.action-forward name="tasksList" path="/manage_tasks.do" redirect = "true"
 */
public class StartProcessInstanceAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        IdForm idForm = (IdForm) form;
        Long definitionId = idForm.getId();
        ActionForward successForward = null;
        ActionMessages messages = new ActionMessages();
        try {
            saveToken(request);
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            if (definitionService.getStartInteraction(subject, definitionId).hasFile()
                    || definitionService.getOutputTransitionNames(subject, definitionId, null).size() > 1) {
                successForward = Commons.forward(mapping.findForward(ru.runa.wf.web.Resources.FORWARD_SUCCESS_DISPLAY_START_FORM),
                        IdForm.ID_INPUT_NAME, String.valueOf(definitionId));
            } else {
                Long processId = executionService.startProcessInstance(subject, definitionService.getProcessDefinitionStub(subject, definitionId)
                        .getName());
                request.getSession().setAttribute(Resources.USER_MESSAGE_KEY,
                        new ActionMessage(Messages.PROCESS_INSTANCE_STARTED, processId.toString()));
                successForward = mapping.findForward(Resources.FORWARD_SUCCESS);

                AutomaticFormOpenerResources formOpenerRes = new AutomaticFormOpenerResources(AutoShowFormHelper.AutoShowFormPropertyFile);
                if (formOpenerRes.isAutoShowForm()) {
                    Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
                    ActionForward forward = AutoShowFormHelper.getNextActionForward(subject, mapping, profile, processId);
                    if (forward != null) {
                        return forward;
                    }
                }
            }
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        } else {
            saveMessages(request.getSession(), messages);
        }
        return successForward;
    }

}
