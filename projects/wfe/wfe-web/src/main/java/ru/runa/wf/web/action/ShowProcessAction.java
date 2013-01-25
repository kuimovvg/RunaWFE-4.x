package ru.runa.wf.web.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.ForwardAction;

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;

public class ShowProcessAction extends ForwardAction {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ActionForward forward = super.execute(mapping, actionForm, request, response);
        Long processId = Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME));
        List<Throwable> throwables = ProcessExecutionErrors.getProcessErrorsAsList(processId);
        if (throwables != null) {
            ActionMessages errors = getErrors(request);
            for (Throwable throwable : throwables) {
                ActionExceptionHelper.addException(errors, throwable);
            }
            saveErrors(request.getSession(), errors);
        }
        return forward;
    }

}
