package ru.runa.wf.web.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Created on 24.06.2014
 * 
 * @struts:action path="/updateProcessVariable" name="commonProcessForm" validate="false"
 * @struts.action-forward name="success" path="/manage_process.do" redirect = "true"
 * @struts.action-forward name="failure" path="/update_process_variables.do" redirect = "false"
 */
public class UpdateProcessVariableAction extends ActionBase {

    public static final String ACTION_PATH = "/updateProcessVariable";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {

        User user = Commons.getUser(request.getSession());
        Long processId = Long.valueOf(request.getParameter("id"));

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ProcessForm.ID_INPUT_NAME, processId);

        try {
            String variableName = request.getParameter("variableSelect");
            VariableDefinition variableDefinition = null;
            WfProcess process = Delegates.getExecutionService().getProcess(user, processId);
            for (VariableDefinition variable : Delegates.getDefinitionService().getVariableDefinitions(user, process.getDefinitionId())) {
                if (variable.getName().equals(variableName)) {
                    variableDefinition = variable;
                }
            }

            boolean nullValue = "on".equals(request.getParameter("isNullValue"));

            Object variableValue = FormSubmissionUtils.extractVariable(request, form, variableDefinition);
            HashMap<String, Object> variable = new HashMap<String, Object>();
            variableValue = nullValue ? null : variableValue;
            variable.put(variableName, variableValue);
            Delegates.getExecutionService().updateVariables(user, processId, variable);
        } catch (Exception e) {
            addError(request, e);
            return getErrorForward(mapping, params);
        }
        FormSubmissionUtils.getUploadedFilesMap(request).clear();
        return getSuccessAction(mapping, params);
    }

    protected ActionForward getSuccessAction(ActionMapping mapping, Map<String, Object> params) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), params);
    }

    protected ActionForward getErrorForward(ActionMapping mapping, Map<String, Object> params) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), params);
    }

}
