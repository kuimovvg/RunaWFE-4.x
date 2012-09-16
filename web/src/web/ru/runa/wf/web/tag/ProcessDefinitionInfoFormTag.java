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

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.Permission;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.presentation.ProcessDefinitionClassPresentation;
import ru.runa.wf.presentation.TaskClassPresentation;
import ru.runa.wf.web.action.LoadProcessDefinitionArchiveAction;

/**
 * Created on 22.07.2005
 * 
 * @jsp.tag name = "processDefinitionInfoForm" body-content = "JSP"
 */
public class ProcessDefinitionInfoFormTag extends ProcessDefinitionBaseFormTag {
    private static final long serialVersionUID = 7118850164438509260L;

    protected boolean isVisible() {
        // TODO think maybe we should check permissions to read here in case
        // this class would be reused
        return true;
    }

    protected boolean isFormButtonVisible() {
        return false;
    }

    protected void fillFormData(TD tdFormElement) throws JspException {
        ProcessDefinition processDefinitonStub = getDefinition();
        Table table = new Table();
        tdFormElement.addElement(table);
        table.setClass(Resources.CLASS_LIST_TABLE);

        TR nameTR = new TR();
        table.addElement(nameTR);
        String definitionName = Messages.getMessage(TaskClassPresentation.TASK_BATCH_PRESENTATION_DEFINITION_NAME, pageContext);
        nameTR.addElement(new TD(definitionName).setClass(Resources.CLASS_LIST_TABLE_TD));
        nameTR.addElement(new TD(processDefinitonStub.getName()).setClass(Resources.CLASS_LIST_TABLE_TD));

        TR versionTR = new TR();
        table.addElement(versionTR);
        String versionName = Messages.getMessage(ProcessDefinitionClassPresentation.PROCESS_DEFINITION_BATCH_PRESENTATION_VERSION, pageContext);
        versionTR.addElement(new TD(versionName).setClass(Resources.CLASS_LIST_TABLE_TD));
        TD versionTD = new TD();
        versionTD.addElement(processDefinitonStub.getVersion() + " (");
        String downloadUrl = Commons.getActionUrl(LoadProcessDefinitionArchiveAction.ACTION_PATH, IdForm.ID_INPUT_NAME, String
                .valueOf(processDefinitonStub.getNativeId()), pageContext, PortletUrl.Render);
        versionTD.addElement(new A(downloadUrl, Messages.getMessage("label.export", pageContext)));
        versionTD.addElement(")");
        versionTR.addElement(versionTD.setClass(Resources.CLASS_LIST_TABLE_TD));

        TR descriptionTR = new TR();
        table.addElement(descriptionTR);
        String descruption = Messages.getMessage(ProcessDefinitionClassPresentation.PROCESS_DEFINITION_BATCH_PRESENTATION_DESCRIPTION, pageContext);
        descriptionTR.addElement(new TD(descruption).setClass(Resources.CLASS_LIST_TABLE_TD));
        descriptionTR.addElement(new TD(processDefinitonStub.getDescription()).setClass(Resources.CLASS_LIST_TABLE_TD));
    }

    protected Permission getPermission() {
        return ProcessDefinitionPermission.READ;
    }

    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_PROCESS_DEFINITION, pageContext);
    }
}
