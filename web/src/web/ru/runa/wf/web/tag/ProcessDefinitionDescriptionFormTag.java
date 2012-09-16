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

import org.apache.ecs.html.IFrame;
import org.apache.ecs.html.TD;

import ru.runa.af.Permission;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.presentation.ProcessDefinitionClassPresentation;
import ru.runa.wf.web.action.ProcessDefinitionDescriptionAction;

/**
 * Created on 30.08.2004
 * 
 * @jsp.tag name = "processDefinitionDescriptionForm" body-content = "empty"
 */
public class ProcessDefinitionDescriptionFormTag extends ProcessDefinitionBaseFormTag {

    private static final long serialVersionUID = -1952116790519897334L;

    @Override
    protected void fillFormData(final TD tdFormElement) throws JspException {
        Long nativeId = ((ProcessDefinition) getIdentifiable()).getNativeId();
        String url = Commons.getActionUrl(ProcessDefinitionDescriptionAction.ACTION_PATH, IdForm.ID_INPUT_NAME, String.valueOf(nativeId),
                pageContext, PortletUrl.Action);
        tdFormElement.addElement(new IFrame().setSrc(url).setWidth("100%"));
    }

    @Override
    protected boolean isVisible() throws JspException {
//        boolean result = false;
//        try {
//            DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
//            result = definitionService.hasFile(getSubject(), getIdentifiableId(), ProcessDefinitionDescriptionAction.DESCRIPTION_FILE_NAME);
//        } catch (Exception e) {
//            // TODO this exception handling is
//        }
        return super.isVisible();
    }

    @Override
    protected Permission getPermission() {
        return ProcessDefinitionPermission.READ;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(ProcessDefinitionClassPresentation.PROCESS_DEFINITION_BATCH_PRESENTATION_DESCRIPTION, pageContext);
    }

}
