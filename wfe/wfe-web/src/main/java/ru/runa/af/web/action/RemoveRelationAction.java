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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.form.RelationIdsForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.service.af.RelationService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.security.AuthenticationException;

/**
 * @struts:action path="/removeRelation" name="relationIdsForm" validate="false"
 *                input = "/WEB-INF/af/manage_relation_members.jsp"
 * @struts.action-forward name="success" path="/manage_relation.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure" path="/manage_relation.do" redirect =
 *                        "true"
 */
public class RemoveRelationAction extends ActionBase {
    public static final String ACTION_PATH = "/removeRelation";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        RelationIdsForm relationForm = (RelationIdsForm) form;
        try {
            RelationService relationService = Delegates.getRelationService();
            for (Long relationId : relationForm.getIds()) {
                relationService.removeRelationPair(getLoggedUser(request), relationId);
            }
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
            return getFailureForward(mapping, relationForm);
        }
        return getSucessForward(mapping, relationForm);
    }

    private ActionForward getSucessForward(ActionMapping mapping, RelationIdsForm relationForm) {
        if (relationForm.getSuccess() == null) {
            return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), "relationName", relationForm.getRelationName());
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("relationName", relationForm.getRelationName());
        params.put("executorId", relationForm.getExecutorId());
        return Commons.forward(new ActionForward(relationForm.getSuccess()), params);
    }

    private ActionForward getFailureForward(ActionMapping mapping, RelationIdsForm relationForm) {
        if (relationForm.getFailure() == null) {
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), "relationName", relationForm.getRelationName());
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("relationName", relationForm.getRelationName());
        params.put("executorId", relationForm.getExecutorId());
        return Commons.forward(new ActionForward(relationForm.getFailure()), params);
    }
}
