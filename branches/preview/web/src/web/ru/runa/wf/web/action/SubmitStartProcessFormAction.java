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

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import ru.runa.af.presentation.Profile;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.AutomaticFormOpenerResources;
import ru.runa.wf.web.form.CommonProcessForm;

/**
 * Created on 18.08.2004
 * 
 * @struts:action path="/submitStartProcessForm" name="commonProcessForm"
 *                validate="true" input =
 *                "/WEB-INF/wf/manage_process_definitions.jsp"
 * @struts.action-forward name="success" path="/manage_process_definitions.do"
 *                        redirect = "true"
 * @struts.action-forward name="failure"
 *                        path="/submit_start_process_instance.do" redirect =
 *                        "false"
 * @struts.action-forward name="submitTask" path="/submit_task.do" redirect =
 *                        "false"
 * @struts.action-forward name="tasksList" path="/manage_tasks.do" redirect =
 *                        "true"
 */
public class SubmitStartProcessFormAction extends BaseProcessFormAction {
    protected Long processId = null;

    @Override
    protected ActionForward executeProcessFromAction(HttpServletRequest request, ActionForm actionForm, ActionMapping mapping, Subject subject,
            Profile profile) throws Exception {
        ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
        DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
        Long definitionId = ((CommonProcessForm) actionForm).getId();
        Interaction interaction = definitionService.getStartInteraction(subject, definitionId);
        Map<String, Object> variables = getFormVariables(request, actionForm, interaction);
        String transitionName = ((CommonProcessForm) actionForm).getSubmitButton();
        variables.put("startTransition", transitionName);
        processId = executionService.startProcessInstance(subject, definitionService.getProcessDefinitionStub(subject, definitionId).getName(),
                variables);

        AutomaticFormOpenerResources formOpenerRes = new AutomaticFormOpenerResources(AutoShowFormHelper.AutoShowFormPropertyFile);
        if (formOpenerRes.isAutoShowForm()) {
            ActionForward forward = AutoShowFormHelper.getNextActionForward(subject, mapping, profile, processId);
            if (forward != null) {
                return forward;
            }
        }

        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

    @Override
    protected ActionForward getErrorForward(ActionMapping mapping, ActionForm actionForm) {
        IdForm form = (IdForm) actionForm;
        return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, String.valueOf(form.getId()));
    }

    @Override
    protected ActionMessage getMessage() {
        return new ActionMessage(Messages.PROCESS_INSTANCE_STARTED, processId.toString());
    }

    protected ActionForward getForward(ActionMapping mapping) {
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
