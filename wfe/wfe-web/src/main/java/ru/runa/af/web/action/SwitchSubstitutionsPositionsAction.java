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

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdsForm;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.ss.Substitution;

/**
 * Created on 03.02.2006
 * 
 * @struts:action path="/switchSubstitutionsPositions" name="idsForm"
 *                validate="true" input = "/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="success" path="/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="failure" path="/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="failure_executor_does_not_exist"
 *                        path="/WEB-INF/af/manage_executors.jsp"
 */

public class SwitchSubstitutionsPositionsAction extends ActionBase {

    public static final String ACTION_PATH = "/switchSubstitutionsPositions";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        IdsForm form = (IdsForm) actionForm;
        try {
            Substitution substitution1 = Delegates.getSubstitutionService().getSubstitution(getLoggedUser(request), form.getIds()[0]);
            Substitution substitution2 = Delegates.getSubstitutionService().getSubstitution(getLoggedUser(request), form.getIds()[1]);
            substitution1.setPosition(substitution2.getPosition());
            Delegates.getSubstitutionService().updateSubstitution(getLoggedUser(request), substitution1);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

}
