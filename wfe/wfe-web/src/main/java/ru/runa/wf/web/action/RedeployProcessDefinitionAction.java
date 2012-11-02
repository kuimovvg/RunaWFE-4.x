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
package ru.runa.wf.web.action;

import java.util.List;

import javax.security.auth.Subject;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.DefinitionService;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dto.WfDefinition;

/**
 * Created on 06.10.2004
 * 
 * @struts:action path="/redeployProcessDefinition" name="fileForm" validate="false"
 * @struts.action-forward name="success" path="/manage_process_definition.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_process_definition.do" redirect = "false"
 * @struts.action-forward name="failure_process_definition_does_not_exist" path="/manage_process_definitions.do" redirect = "true"
 */
public class RedeployProcessDefinitionAction extends BaseDeployProcessDefinitionAction {
    public static final String ACTION_PATH = "/redeployProcessDefinition";

    private boolean definitionExists = false;

    private Long definitionId;

    @Override
    protected void doAction(Subject subject, FileForm fileForm, List<String> processType, ActionMessages errors) {
        DefinitionService definitionService = DelegateFactory.getDefinitionService();
        try {
            WfDefinition processDefinitionDescriptor = definitionService.getProcessDefinition(subject, fileForm.getId());
            definitionService.redeployProcessDefinition(subject, fileForm.getId(), "".equals(fileForm.getFile().getFileName()) ? null : fileForm
                    .getFile().getFileData(), processType);
            WfDefinition newProcessDefinitionStub = definitionService.getLatestProcessDefinition(subject, processDefinitionDescriptor.getName());
            definitionId = newProcessDefinitionStub.getId();
        } catch (DefinitionDoesNotExistException e) {
            ActionExceptionHelper.addException(errors, e);
            definitionExists = false;
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }
    }

    @Override
    protected ActionForward getSuccessAction(ActionMapping mapping) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, definitionId);
    }

    @Override
    protected ActionForward getErrorForward(ActionMapping mapping) {
        if (definitionExists) {
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, definitionId);
        }
        return mapping.findForward(ru.runa.common.WebResources.FORWARD_FAILURE_PROCESS_DEFINITION_DOES_NOT_EXIST);
    }

    @Override
    protected void prepare(FileForm fileForm) {
        definitionExists = true;
        definitionId = fileForm.getId();
    }
}
