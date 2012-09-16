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
package ru.runa.af.web.tag;

import java.util.List;

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.TD;

import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.Relation;
import ru.runa.af.RelationPermission;
import ru.runa.af.RelationsGroupSecure;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.RelationService;
import ru.runa.af.web.action.RemoveRelationGroupAction;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.Messages;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.IdentifiableCheckboxTDBuilder;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.delegate.DelegateFactory;

/**
 * @jsp.tag name = "listRelationsForm" body-content = "JSP"
 */
public class ListRelationsFormTag extends BatchReturningTitledFormTag {

    private static final long serialVersionUID = 1L;

    boolean isFormButtonVisible;

    @Override
    protected void fillFormElement(TD tdFormElement) throws JspException {
        try {
            RelationService relationService = DelegateFactory.getInstance().getRelationService();
            AuthorizationService authorizationService = DelegateFactory.getInstance().getAuthorizationService();
            isFormButtonVisible = authorizationService.isAllowed(getSubject(), RelationPermission.UPDATE_RELATION, RelationsGroupSecure.INSTANCE);
            List<Relation> relationGroups = relationService.getRelations(getSubject(), getBatchPresentation());

            TableBuilder tableBuilder = new TableBuilder();

            TDBuilder checkboxBuilder = new IdentifiableCheckboxTDBuilder(RelationPermission.UPDATE_RELATION) {

                @Override
                protected boolean isEnabled(Object object, Env env) throws JspException {
                    return isFormButtonVisible;
                }
            };

            TDBuilder[] builders = getBuilders(new TDBuilder[] { checkboxBuilder }, getBatchPresentation(), new TDBuilder[] {});

            RowBuilder rowBuilder = new ReflectionRowBuilder(relationGroups, getBatchPresentation(), pageContext,
                    ru.runa.af.web.Resources.ACTION_MAPPING_MANAGE_RELATION, getReturnAction(), new RelationURLStrategy(), builders);
            HeaderBuilder headerBuilder = new SortingHeaderBuilder(getBatchPresentation(), 1, 0, getReturnAction(), pageContext);

            tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_RELATIONS, pageContext);
    }

    @Override
    protected boolean isFormButtonEnabled() throws JspException {
        return isFormButtonVisible;
    }

    @Override
    protected boolean isFormButtonEnabled(Identifiable identifiable, Permission permission) throws JspException {
        return isFormButtonVisible;
    }

    @Override
    protected boolean isFormButtonVisible() throws JspException {
        return isFormButtonVisible;
    }

    @Override
    protected boolean isMultipleSubmit() {
        return false;
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_REMOVE, pageContext);
    }

    @Override
    public String getAction() {
        return RemoveRelationGroupAction.ACTION_PATH;
    }

    class RelationURLStrategy implements ItemUrlStrategy {

        @Override
        public String getUrl(String baseUrl, Object item) {
            return Commons.getActionUrl(baseUrl, "relationName", ((Relation) item).getName(), pageContext, PortletUrl.Action);
        }

    }
}
