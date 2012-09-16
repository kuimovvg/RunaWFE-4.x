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

import java.util.List;

import javax.security.auth.Subject;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.GrantPermisionOnIdentifiableAction;
import ru.runa.common.web.form.IdForm;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessInstanceDoesNotExistException;

import com.google.common.collect.Lists;

/**
 * Created on 23.08.2004
 * 
 * @struts:action path="/grantReadPermissionOnProcessInstance" name="idsForm" validate="true" input = "/WEB-INF/wf/manage_process_instance.jsp"
 * @struts.action-forward name="success" path="/manage_process_instance_permissions.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_process_instance_permissions.do" redirect = "true"
 * @struts.action-forward name="failure_process_instance_does_not_exist" path="/manage_process_instances.do" redirect = "true"
 */
public class GrantReadPermissionOnProcessInstanceAction extends GrantPermisionOnIdentifiableAction {
    public static final String ACTION_PATH = "/grantReadPermissionOnProcessInstance";

    private static List<Permission> READ_PERMISSONS = Lists.newArrayList(Permission.READ);

    private boolean instanceExists;

    @Override
    protected List<Permission> getIdentifiablePermissions() {
        return READ_PERMISSONS;
    }

    @Override
    protected Identifiable getIdentifiable(Subject subject, Long identifiableId, ActionMessages errors) throws AuthenticationException,
            AuthorizationException {
        instanceExists = false;
        Identifiable result = null;
        try {
            result = DelegateFactory.getInstance().getExecutionService().getProcessInstanceStub(subject, identifiableId);
            instanceExists = true;
        } catch (ProcessInstanceDoesNotExistException e) {
            ActionExceptionHelper.addException(errors, e);
        }
        return result;
    }

    @Override
    public ActionForward getErrorForward(ActionMapping mapping, Long identifiableId) {
        if (instanceExists) {
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, String.valueOf(identifiableId));
        }
        return mapping.findForward(ru.runa.wf.web.Resources.FORWARD_FAILURE_PROCESS_INSTANCE_DOES_NOT_EXIST);
    }

    @Override
    public ActionForward getSuccessForward(ActionMapping mapping, Long identifiableId) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, String.valueOf(identifiableId));
    }
}
