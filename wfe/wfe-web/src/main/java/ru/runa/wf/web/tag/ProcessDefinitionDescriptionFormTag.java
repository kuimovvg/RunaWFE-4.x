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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ecs.html.IFrame;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.DefinitionService;
import ru.runa.wf.web.action.ProcessDefinitionDescriptionAction;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.Permission;

/**
 * Created on 30.08.2004
 * 
 * @jsp.tag name = "processDefinitionDescriptionForm" body-content = "empty"
 */
public class ProcessDefinitionDescriptionFormTag extends ProcessDefinitionBaseFormTag {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ProcessDefinitionDescriptionFormTag.class);

    @Override
    protected void fillFormData(final TD tdFormElement) throws JspException {
        Long id = ((WfDefinition) getIdentifiable()).getId();
        String url = Commons.getActionUrl(ProcessDefinitionDescriptionAction.ACTION_PATH, IdForm.ID_INPUT_NAME, id, pageContext, PortletUrlType.Action);
        tdFormElement.addElement(new IFrame().setSrc(url).setWidth("100%"));
    }

    @Override
    protected boolean isVisible() throws JspException {
        boolean result = false;
        try {
            DefinitionService definitionService = Delegates.getDefinitionService();
            result = definitionService.getFile(getSubject(), getIdentifiableId(), ProcessDefinitionDescriptionAction.DESCRIPTION_FILE_NAME) != null;
        } catch (Exception e) {
            log.error(e.toString());
        }
        return result;
        // return super.isVisible();
    }

    @Override
    protected Permission getPermission() {
        return DefinitionPermission.READ;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(DefinitionClassPresentation.PROCESS_DEFINITION_BATCH_PRESENTATION_DESCRIPTION, pageContext);
    }

}
