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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.form.SubstitutionCriteriaForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.service.af.SubstitutionService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.ss.SubstitutionCriteria;

/**
 * @struts:action path="/updateSubstitutionCriteria"
 *                name="substitutionCriteriaForm" validate="true" input =
 *                "/WEB-INF/af/edit_substitution_criteria.jsp"
 * @struts.action-forward name="success" path="/WEB-INF/af/manage_system.jsp"
 * @struts.action-forward name="failure"
 *                        path="/WEB-INF/af/edit_substitution_criteria.jsp"
 */
public class UpdateSubstitutionCriteriaAction extends ActionBase {
    public static final String UPDATE_ACTION = "/updateSubstitutionCriteria";
    public static final String EDIT_ACTION = "/editSubstitutionCriteria";
    public static final String RETURN_ACTION = "/manage_system.do";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        try {
            SubstitutionCriteriaForm form = (SubstitutionCriteriaForm) actionForm;
            SubstitutionService substitutionService = Delegates.getSubstitutionService();
            SubstitutionCriteria substitutionCriteria;
            if (form.getId() == 0) {
                substitutionCriteria = ClassLoaderUtil.instantiate(form.getType());
                substitutionCriteria.setConfiguration(form.getConf());
            } else {
                substitutionCriteria = substitutionService.getSubstitutionCriteria(getLoggedUser(request), form.getId());
            }
            substitutionCriteria.setName(form.getName());
            if (form.getId() == 0) {
                substitutionService.createSubstitutionCriteria(getLoggedUser(request), substitutionCriteria);
            } else {
                substitutionService.store(getLoggedUser(request), substitutionCriteria);
            }
            return new ActionForward(RETURN_ACTION, true);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        saveErrors(request, errors);
        return mapping.findForward(Resources.FORWARD_FAILURE);
    }
}
