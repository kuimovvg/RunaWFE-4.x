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

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.web.action.CreateRelationGroupAction;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.FormTag;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;

/**
 * @jsp.tag name = "createRelationGroupForm" body-content = "empty"
 */
public class CreateRelationGroupFormTag extends FormTag {
    private static final long serialVersionUID = 1L;

    @Override
    public String getAction() {
        return CreateRelationGroupAction.ACTION_PATH;
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage("button.create_relation_group", pageContext);
    }

    @Override
    protected List<String> getFormButtonNames() {
        return super.getFormButtonNames();
    }

    @Override
    protected boolean isFormButtonEnabled() {
        return true;
    }

    @Override
    protected boolean isFormButtonEnabled(Identifiable identifiable, Permission permission) {
        return super.isFormButtonEnabled(identifiable, permission);
    }

    @Override
    protected boolean isFormButtonVisible() {
        return true;
    }

    @Override
    protected boolean isMultipleSubmit() {
        return false;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        TR tr = new TR();
        tr.setClass(Resources.CLASS_LIST_TABLE_TH);
        tr.addElement(new TD(Messages.getMessage(Messages.LABEL_CREATE_RELATION_GROUP_NAME, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(new Input(Input.TEXT, "relationName")).setClass(Resources.CLASS_LIST_TABLE_TD));
        table.addElement(tr);
        tr = new TR();
        tr.setClass(Resources.CLASS_LIST_TABLE_TH);
        tr.addElement(new TD(Messages.getMessage(Messages.LABEL_CREATE_RELATION_GROUP_DESCRIPTION, pageContext))
                .setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(new Input(Input.TEXT, "relationDescription")).setClass(Resources.CLASS_LIST_TABLE_TD));
        table.addElement(tr);
        tdFormElement.addElement(table);
    }
}
