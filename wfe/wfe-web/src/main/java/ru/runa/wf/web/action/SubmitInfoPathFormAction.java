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

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import ru.runa.common.web.Messages;
import ru.runa.common.web.action.ActionBase;
import ru.runa.service.DefinitionService;
import ru.runa.service.ExecutionService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.form.Interaction;

/**
 * @struts:action path="/submitInfoPathForm" name="processForm" validate="false"
 */
public class SubmitInfoPathFormAction extends ActionBase {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        boolean hasErrors = false;
        try {
            ExecutionService executionService = Delegates.getExecutionService();
            DefinitionService definitionService = Delegates.getDefinitionService();

            ActionMessage userMessage;
            String taskIdParam = request.getParameter("taskId");
            if (taskIdParam != null) {
                // execute task
                Long taskId = new Long(taskIdParam);
                Interaction interaction = definitionService.getTaskInteraction(getLoggedUser(request), taskId);
                HashMap<String, Object> variables = VariableExtractionHelper.extractVariables(request.getSession(), actionForm, interaction);
                executionService.completeTask(getLoggedUser(request), taskId, variables);
                userMessage = new ActionMessage(Messages.TASK_COMPLETED);
            } else {
                // start process
                Long definitionId = new Long(request.getParameter("definitionId"));
                Interaction interaction = definitionService.getStartInteraction(getLoggedUser(request), definitionId);
                HashMap<String, Object> variables = VariableExtractionHelper.extractVariables(request.getSession(), actionForm, interaction);
                String definitionName = definitionService.getProcessDefinition(getLoggedUser(request), definitionId).getName();
                Long processId = executionService.startProcess(getLoggedUser(request), definitionName, variables);
                userMessage = new ActionMessage(Messages.PROCESS_STARTED, processId.toString());
            }
            addMessage(request, userMessage);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            hasErrors = true;
        }
        if (hasErrors) {
            response.getOutputStream().write("ERROR".getBytes());
        } else {
            response.getOutputStream().write("OK".getBytes());
        }
        return null;
    }
}
