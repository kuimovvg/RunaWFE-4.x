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

import java.util.List;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.form.IdsForm;
import ru.runa.service.af.ExecutorService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.security.AuthenticationException;

import com.google.common.collect.Lists;

/**
 * Created on 18.08.2004
 * 
 * @struts:action path="/removeExecutors" name="idsForm" validate="false"
 * @struts.action-forward name="success" path="/manage_executors.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_executors.do" redirect = "true"
 */
public class RemoveExecutorsAction extends Action {

    public static final String ACTION_PATH = "/removeExecutors";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        try {
            ExecutorService executorService = Delegates.getExecutorService();
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            List<Long> ids = Lists.newArrayList(((IdsForm) form).getIds());
            executorService.remove(subject, ids);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
            return mapping.findForward(ru.runa.common.web.Resources.FORWARD_FAILURE);
        }
        return mapping.findForward(ru.runa.common.web.Resources.FORWARD_SUCCESS);
    }

}
