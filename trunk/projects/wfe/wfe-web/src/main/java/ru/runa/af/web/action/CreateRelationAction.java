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

import ru.runa.af.web.form.CreateRelationForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.RelationService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

/**
 * @struts:action path="/createRelation" name="createRelationForm"
 *                validate="true" input = "/WEB-INF/af/create_relation.jsp"
 * @struts.action-forward name="success" path="/manage_relation.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure" path="/create_relation.do" redirect =
 *                        "true"
 */
public class CreateRelationAction extends ActionBase {

    public static final String ACTION_PATH = "/createRelation";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        CreateRelationForm relationForm = (CreateRelationForm) form;
        try {
            ExecutorService executorService = Delegates.getExecutorService();
            RelationService relationService = Delegates.getRelationService();
            Executor executorFrom = executorService.getExecutorByName(getLoggedUser(request), relationForm.getRelationFrom());
            Executor executorTo = executorService.getExecutorByName(getLoggedUser(request), relationForm.getRelationTo());
            relationService.addRelationPair(getLoggedUser(request), relationForm.getRelationName(), executorFrom, executorTo);
        } catch (Exception e) {
            addError(request, e);
            return getFailureForward(mapping, relationForm);
        }
        return getSuccessForward(mapping, relationForm);
    }

    private ActionForward getSuccessForward(ActionMapping mapping, CreateRelationForm relationForm) {
        if (relationForm.getSuccess() == null) {
            return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), "relationName", relationForm.getRelationName());
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("relationName", relationForm.getRelationName());
        params.put("executorId", relationForm.getExecutorId());
        return Commons.forward(new ActionForward(relationForm.getSuccess()), params);
    }

    private ActionForward getFailureForward(ActionMapping mapping, CreateRelationForm relationForm) {
        if (relationForm.getFailure() == null) {
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), "relationName", relationForm.getRelationName());
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("relationName", relationForm.getRelationName());
        params.put("executorId", relationForm.getExecutorId());
        return Commons.forward(new ActionForward(relationForm.getFailure()), params);
    }
}
