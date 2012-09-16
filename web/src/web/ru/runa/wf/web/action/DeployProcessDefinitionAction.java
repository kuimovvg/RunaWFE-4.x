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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.security.auth.Subject;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionAlreadyExistsException;
import ru.runa.wf.ProcessDefinitionArchiveException;
import ru.runa.wf.service.DefinitionService;

/**
 * Created on 06.10.2004
 * 
 * @struts:action path="/deployProcessDefinition" name="fileForm" validate="false"
 * @struts.action-forward name="success" path="/manage_process_definitions.do" redirect = "true"
 * @struts.action-forward name="failure" path="/deploy_process_definition.do" redirect = "false"
 */
public class DeployProcessDefinitionAction extends BaseDeployProcessDefinitionAction {
    public static final String ACTION_PATH = "/deployProcessDefinition";

    @Override
    protected void doAction(Subject subject, FileForm fileForm, List<String> processType, ActionMessages errors) throws FileNotFoundException,
            AuthenticationException, AuthorizationException, ProcessDefinitionArchiveException, IOException {
        DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
        try {
            definitionService.deployProcessDefinition(subject, fileForm.getFile().getFileData(), processType);
        } catch (ProcessDefinitionAlreadyExistsException e) {
            ActionExceptionHelper.addException(errors, e);
        }
    }

    @Override
    protected ActionForward getSuccessAction(ActionMapping mapping) {
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

    @Override
    protected ActionForward getErrorForward(ActionMapping mapping) {
        return mapping.findForward(Resources.FORWARD_FAILURE);
    }

    @Override
    protected void prepare(FileForm fileForm) {
    }
}
