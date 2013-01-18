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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.security.AuthenticationException;

/**
 * Created on 18.08.2004
 * 
 * @struts:action path="/cancelProcess" name="idForm" validate="true" input = "/WEB-INF/wf/manage_process.jsp"
 * @struts.action-forward name="success" path="/manage_process.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_process.do" redirect = "true"
 * @struts.action-forward name="failure_process_does_not_exist" path="/manage_processes.do" redirect = "true"
 */
public class CancelProcessAction extends Action {

    public static final String ACTION_PATH = "/cancelProcess";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse responce)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        IdForm form = (IdForm) actionForm;
        boolean processExists = true;
        try {
            ExecutionService executionService = Delegates.getExecutionService();
            executionService.cancelProcess(SubjectHttpSessionHelper.getActorSubject(request.getSession()), form.getId());
        } catch (ProcessDoesNotExistException e) {
            ActionExceptionHelper.addException(errors, e);
            processExists = false;
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
            if (processExists) {
                return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, form.getId());
            }
            return mapping.findForward(ru.runa.common.WebResources.FORWARD_FAILURE_PROCESS_DOES_NOT_EXIST);
        } else {
            ActionMessages messages = new ActionMessages();
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.PROCESS_CANCELED));
            saveMessages(request.getSession(), messages);
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, form.getId());
    }

}
