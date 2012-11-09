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
package ru.runa.af.web.action;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.af.web.form.UpdatePasswordForm;
import ru.runa.common.WebResources;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.af.ExecutorService;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

/**
 * Created on 24.08.2004
 * 
 * @struts:action path="/updatePassword" name="updatePasswordForm"
 *                validate="true" input = "/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="success" path="/manage_executor.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure" path="/manage_executor.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure_executor_does_not_exist"
 *                        path="/manage_executors.do" redirect = "true"
 */
public class UpdatePasswordAction extends Action {

    public static final String ACTION_PATH = "/updatePassword";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        UpdatePasswordForm form = (UpdatePasswordForm) actionForm;
        boolean executorExists = true;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            ExecutorService executorService = DelegateFactory.getExecutorService();
            Actor actor = executorService.getExecutor(subject, form.getId());
            executorService.setPassword(subject, actor, form.getPassword());
        } catch (ExecutorDoesNotExistException e) {
            ActionExceptionHelper.addException(errors, e);
            executorExists = false;
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
            if (executorExists) {
                return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, form.getId());
            }
            return mapping.findForward(WebResources.FORWARD_FAILURE_EXECUTOR_DOES_NOT_EXIST);
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, form.getId());
    }

}
