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

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.WebResources;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.UpdatePermissionOnIdentifiableAction;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

/**
 * Created on 25.08.2004
 * 
 * @struts:action path="/updatePermissionsOnExecutor" name="updatePermissionsOnIdentifiableForm" validate="true" input = "/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="success" path="/manage_executor_permissions.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_executor_permissions.do" redirect = "true"
 * @struts.action-forward name="failure_executor_does_not_exist" path="/manage_executors.do" redirect = "true"
 */
public class UpdatePermissionsOnExecutorAction extends UpdatePermissionOnIdentifiableAction {

    public static final String ACTION_PATH = "/updatePermissionsOnExecutor";

    private boolean executorExists;

    @Override
    protected Identifiable getIdentifiable(Subject subject, Long identifiableId, ActionMessages errors) throws AuthorizationException,
            AuthenticationException {
        executorExists = false;
        Identifiable result = null;
        try {
            result = Delegates.getExecutorService().getExecutor(subject, identifiableId);
            executorExists = true;
        } catch (ExecutorDoesNotExistException e) {
            ActionExceptionHelper.addException(errors, e);
        }
        return result;
    }

    @Override
    public ActionForward getErrorForward(ActionMapping mapping, Long identifiableId) {
        if (executorExists) {
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, identifiableId);
        }
        return mapping.findForward(WebResources.FORWARD_FAILURE_EXECUTOR_DOES_NOT_EXIST);
    }

    @Override
    public ActionForward getSuccessForward(ActionMapping mapping, Long identifiableId) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, identifiableId);
    }

}
