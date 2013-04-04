package ru.runa.wf.web.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.ForwardAction;

import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors.TokenErrorDetail;

public class ShowProcessAction extends ForwardAction {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // TODO report sending by email
        ActionForward forward = super.execute(mapping, actionForm, request, response);
        Long processId = Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME));
        List<TokenErrorDetail> errorDetails = ProcessExecutionErrors.getProcessErrors(processId);
        if (errorDetails != null) {
            // ActionMessages errors = getErrors(request);
            // for (TokenErrorDetail detail : errorDetails) {
            // ActionExceptionHelper.addProcessError(errors,
            // detail.getThrowable());
            // }
            // saveErrors(request.getSession(), errors);
            String processErrors = "";
            for (TokenErrorDetail detail : errorDetails) {
                String url = "javascript:showProcessError(" + processId + ", '" + detail.getTaskName() + "')";
                processErrors += "<a href=\"" + url + "\">" + detail.getTaskName() + " (" + CalendarUtil.formatDateTime(detail.getOccuredDate())
                        + ")</a><br>";
            }
            request.setAttribute("processErrors", processErrors);
        }
        return forward;
    }
}
