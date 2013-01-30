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

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.TD;

import ru.runa.af.web.action.UpdatePermissionOnRelation;
import ru.runa.common.web.Messages;
import ru.runa.common.web.html.PermissionTableBuilder;
import ru.runa.common.web.tag.IdentifiableFormTag;
import ru.runa.service.af.RelationService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.relation.RelationPermission;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;

/**
 * @jsp.tag name = "updatePermissionsOnRelationForm" body-content = "JSP"
 */
public class UpdatePermissionOnRelationFormTag extends IdentifiableFormTag {

    private static final long serialVersionUID = 1L;
    private String relationName;

    @Override
    protected void fillFormData(TD tdFormElement) throws JspException {
        PermissionTableBuilder tableBuilder = new PermissionTableBuilder(getIdentifiable(), getUser(), pageContext);
        tdFormElement.addElement(tableBuilder.buildTable());
    }

    @Override
    protected Identifiable getIdentifiable() throws JspException {
        try {
            RelationService relationService = Delegates.getRelationService();
            return relationService.getRelation(getUser(), getRelationName());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected Permission getPermission() throws JspException {
        return RelationPermission.READ;
    }

    @Override
    public String getAction() {
        return UpdatePermissionOnRelation.ACTION_PATH_NAME;
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_APPLY, pageContext);
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_PERMISSION_OWNERS, pageContext);
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public String getRelationName() {
        return relationName;
    }
}
