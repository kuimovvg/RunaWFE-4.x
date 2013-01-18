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

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.af.web.action.RemoveRelationAction;
import ru.runa.common.WebResources;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.IdentifiableCheckboxTDBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.service.af.AuthorizationService;
import ru.runa.service.af.RelationService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.RelationPermission;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;

/**
 * @jsp.tag name = "listRelationMembersForm" body-content = "JSP"
 */
public class ListRelationMembersFormTag extends BatchReturningTitledFormTag {

    private static final long serialVersionUID = 1L;

    private String relationName;

    boolean isFormButtonVisible;

    @Override
    protected void fillFormElement(TD tdFormElement) throws JspException {
        try {
            RelationService relationService = Delegates.getRelationService();
            AuthorizationService authorizationService = Delegates.getAuthorizationService();
            Relation currentRelation = relationService.getRelation(getSubject(), getRelationName());
            isFormButtonVisible = authorizationService.isAllowed(getSubject(), RelationPermission.UPDATE_RELATION, currentRelation);
            BatchPresentation batchPresentation = getBatch();
            List<RelationPair> relations = relationService.getRelationPairs(getSubject(), relationName, batchPresentation);

            TableBuilder tableBuilder = new TableBuilder();

            TDBuilder checkboxBuilder = new IdentifiableCheckboxTDBuilder(RelationPermission.UPDATE_RELATION) {

                @Override
                protected boolean isEnabled(Object object, Env env) {
                    return isFormButtonVisible;
                }
            };

            TDBuilder[] builders = getBuilders(new TDBuilder[] { checkboxBuilder }, batchPresentation, new TDBuilder[] {});

            RowBuilder rowBuilder = new ReflectionRowBuilder(relations, batchPresentation, pageContext, WebResources.ACTION_MAPPING_UPDATE_EXECUTOR,
                    getReturnAction(), IdForm.ID_INPUT_NAME, builders);
            HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 1, 0, getReturnAction(), pageContext);

            tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
            tdFormElement.addElement(new Input(Input.HIDDEN, "relationName", getRelationName()));
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    public String getRelationName() {
        return relationName;
    }

    @Override
    protected String getTitle() {
        return relationName;
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
    public String getAction() {
        return RemoveRelationAction.ACTION_PATH;
    }

    BatchPresentation getBatch() {
        BatchPresentation result = getBatchPresentation();
        return result;
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_REMOVE, pageContext);
    }
}
