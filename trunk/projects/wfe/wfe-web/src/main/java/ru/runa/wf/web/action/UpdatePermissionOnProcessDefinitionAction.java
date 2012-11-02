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

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.UpdatePermissionOnIdentifiableAction;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;

/**
 * Created on 30.08.2004
 * 
 * @struts:action path="/updatePermissionOnProcessDefinition" name="updatePermissionsOnIdentifiableForm" validate="true" input = "/WEB-INF/wf/manage_process_definition.jsp"
 * @struts.action-forward name="success" path="/manage_process_definition_permissions.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_process_definition_permissions.do" redirect = "true"
 * @struts.action-forward name="failure_process_definition_does_not_exist" path="/manage_process_definitions.do" redirect = "true"
 */
public class UpdatePermissionOnProcessDefinitionAction extends UpdatePermissionOnIdentifiableAction {
    public static final String ACTION_PATH = "/updatePermissionOnProcessDefinition";

    private boolean definitionExists;

    @Override
    protected Identifiable getIdentifiable(Subject subject, Long identifiableId, ActionMessages errors) throws AuthenticationException,
            AuthorizationException {
        definitionExists = false;
        Identifiable result = null;
        try {
            result = DelegateFactory.getDefinitionService().getProcessDefinition(subject, identifiableId);
            definitionExists = true;
        } catch (DefinitionDoesNotExistException e) {
            ActionExceptionHelper.addException(errors, e);
        }
        return result;
    }

    @Override
    public ActionForward getErrorForward(ActionMapping mapping, Long identifiableId) {
        if (definitionExists) {
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, identifiableId);
        }
        return mapping.findForward(ru.runa.common.WebResources.FORWARD_FAILURE_PROCESS_DEFINITION_DOES_NOT_EXIST);
    }

    @Override
    public ActionForward getSuccessForward(ActionMapping mapping, Long identifiableId) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, identifiableId);
    }

}
