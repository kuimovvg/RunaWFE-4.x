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

import ru.runa.af.AuthenticationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.IdsForm;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

/**
 * Created on 23.08.2004
 * 
 * @struts:action path="/addExecutorToGroups" name="idsForm" validate="true" input = "/WEB-INF/af/manage_executor.jsp"
 * @struts:action-forward name="success" path="/manage_executor.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_executor.do" redirect = "true"
 * @struts.action-forward name="failure_executor_does_not_exist" path="/manage_executors.do" redirect = "true"
 */
public class AddExecutorToGroupsAction extends Action {

    public static final String ACTION_PATH = "/addExecutorToGroups";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce)
            throws AuthenticationException {

        ActionMessages errors = new ActionMessages();
        IdsForm groupsForm = (IdsForm) form;
        boolean executorExists = true;
        try {
            ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            executorService.addExecutorToGroups(subject, groupsForm.getId(), Lists.newArrayList(groupsForm.getIds()));
        } catch (ExecutorOutOfDateException e) {
            ActionExceptionHelper.addException(errors, e);
            executorExists = false;
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
            if (executorExists) {
                return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, String.valueOf(groupsForm.getId()));
            }
            return mapping.findForward(ru.runa.af.web.Resources.FORWARD_FAILURE_EXECUTOR_DOES_NOT_EXIST);
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, String.valueOf(groupsForm.getId()));
    }

}
