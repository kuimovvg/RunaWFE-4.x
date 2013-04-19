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
package ru.runa.wf.web.tag;

import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.action.LoadProcessDefinitionArchiveAction;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.TaskClassPresentation;

/**
 * Created on 22.07.2005
 * 
 * @jsp.tag name = "processDefinitionInfoForm" body-content = "JSP"
 */
public class ProcessDefinitionInfoFormTag extends ProcessDefinitionBaseFormTag {
    private static final long serialVersionUID = 7118850164438509260L;

    @Override
    protected boolean isVisible() {
        return true;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        WfDefinition definition = getDefinition();
        Table table = new Table();
        tdFormElement.addElement(table);
        table.setClass(Resources.CLASS_LIST_TABLE);

        TR nameTR = new TR();
        table.addElement(nameTR);
        String definitionName = Messages.getMessage(TaskClassPresentation.DEFINITION_NAME, pageContext);
        nameTR.addElement(new TD(definitionName).setClass(Resources.CLASS_LIST_TABLE_TD));
        nameTR.addElement(new TD(definition.getName()).setClass(Resources.CLASS_LIST_TABLE_TD));

        TR versionTR = new TR();
        table.addElement(versionTR);
        String versionName = Messages.getMessage(DefinitionClassPresentation.VERSION, pageContext);
        versionTR.addElement(new TD(versionName).setClass(Resources.CLASS_LIST_TABLE_TD));
        TD versionTD = new TD();
        versionTD.addElement(definition.getVersion() + " (");
        String downloadUrl = Commons.getActionUrl(LoadProcessDefinitionArchiveAction.ACTION_PATH, IdForm.ID_INPUT_NAME, definition.getId(),
                pageContext, PortletUrlType.Render);
        versionTD.addElement(new A(downloadUrl, Messages.getMessage("label.export", pageContext)));
        versionTD.addElement(")");
        versionTR.addElement(versionTD.setClass(Resources.CLASS_LIST_TABLE_TD));

        if (definition.getDeployedDate() != null) {
            TR deployedTR = new TR();
            table.addElement(deployedTR);
            String deploymentDate = Messages.getMessage(DefinitionClassPresentation.DEPLOYMENT_DATE, pageContext);
            deployedTR.addElement(new TD(deploymentDate).setClass(Resources.CLASS_LIST_TABLE_TD));
            deployedTR.addElement(new TD(CalendarUtil.formatDateTime(definition.getDeployedDate())).setClass(Resources.CLASS_LIST_TABLE_TD));
        }

        TR descriptionTR = new TR();
        table.addElement(descriptionTR);
        String description = Messages.getMessage(DefinitionClassPresentation.DESCRIPTION, pageContext);
        descriptionTR.addElement(new TD(description).setClass(Resources.CLASS_LIST_TABLE_TD));
        descriptionTR.addElement(new TD(definition.getDescription()).setClass(Resources.CLASS_LIST_TABLE_TD));
    }

    @Override
    protected Permission getPermission() {
        return DefinitionPermission.READ;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_PROCESS_DEFINITION, pageContext);
    }
}
