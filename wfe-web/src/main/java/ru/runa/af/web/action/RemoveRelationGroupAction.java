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
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdsForm;
import ru.runa.service.af.RelationService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.security.AuthenticationException;

/**
 * @struts:action path="/removeRelationGroup" name="idsForm" validate="false"
 * @struts.action-forward name="success" path="/manage_relations.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure" path="/manage_relations.do" redirect =
 *                        "true"
 */
public class RemoveRelationGroupAction extends ActionBase {
    public static final String ACTION_PATH = "/removeRelationGroup";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        try {
            RelationService relationService = Delegates.getRelationService();
            IdsForm listAllForm = (IdsForm) form;
            for (Long groupId : listAllForm.getIds()) {
                relationService.removeRelation(getLoggedUser(request), groupId);
            }
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
