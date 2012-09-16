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
package ru.runa.common.web.action;

import java.util.List;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.AuthenticationException;
import ru.runa.af.Identifiable;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.form.IdsForm;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

/**
 * Created on 23.08.2004
 */
abstract public class GrantPermisionOnIdentifiableAction extends IdentifiableAction {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        IdsForm listExecutorsForm = (IdsForm) form;
        List<Long> selectedIds = Lists.newArrayList(listExecutorsForm.getIds());
        try {
            AuthorizationService authorizationService = DelegateFactory.getInstance().getAuthorizationService();
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            Identifiable identifiable = getIdentifiable(subject, listExecutorsForm.getId(), errors);
            if (identifiable != null) {
                authorizationService.setPermissions(subject, selectedIds, getIdentifiablePermissions(), identifiable);
            }
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
            return getErrorForward(mapping, listExecutorsForm.getId());
        }
        return getSuccessForward(mapping, listExecutorsForm.getId());
    }

    public abstract ActionForward getErrorForward(ActionMapping mapping, Long identifiableId);

    public abstract ActionForward getSuccessForward(ActionMapping mapping, Long identifiableId);
}
