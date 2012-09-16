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
import ru.runa.af.Substitution;
import ru.runa.af.SubstitutionCriteria;
import ru.runa.af.TerminatorSubstitution;
import ru.runa.af.service.SubstitutionService;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.af.web.form.SubstitutionForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Resources;
import ru.runa.delegate.DelegateFactory;

/**
 * @struts:action path="/updateSubstitution" name="substitutionForm" validate="true" input = "/WEB-INF/af/edit_substitution.jsp"
 * @struts.action-forward name="success" path="/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="failure" path="/WEB-INF/af/edit_substitution.jsp"
 */
public class UpdateSubstitutionAction extends Action {
    public static final String UPDATE_ACTION = "/updateSubstitution";
    public static final String EDIT_ACTION = "/editSubstitution";
    public static final String RETURN_ACTION = "/manage_executor.do?id=";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        try {
            SubstitutionForm form = (SubstitutionForm) actionForm;
            SubstitutionService substitutionService = DelegateFactory.getInstance().getSubstitutionService();
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            Substitution substitution;
            if (form.getId() == 0) {
                if (form.isTerminator()) {
                    substitution = new TerminatorSubstitution();
                    substitution.setSubstitutionOrgFunction("");
                } else {
                    substitution = new Substitution();
                }
            } else {
                substitution = substitutionService.getSubstitution(subject, form.getId());
            }
            SubstitutionCriteria criteria = null;
            if (form.getCriteriaId() != 0) {
                criteria = substitutionService.getSubstitutionCriteria(subject, form.getCriteriaId());
            }
            substitution.setCriteria(criteria);
            substitution.setEnabled(form.isEnabled());
            if (!(substitution instanceof TerminatorSubstitution)) {
                substitution.setSubstitutionOrgFunction(form.buildOrgFunction());
            }
            if (form.getId() == 0) {
                substitutionService.createSubstitution(subject, form.getActorId(), substitution);
            } else {
                substitutionService.store(subject, substitution);
            }
            return new ActionForward(RETURN_ACTION + substitution.getActorId(), true);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        saveErrors(request, errors);
        return mapping.findForward(Resources.FORWARD_FAILURE);
    }
}
