package ru.runa.common.web.action;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.form.IdNameForm;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors.BotTaskIdentifier;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors.TokenErrorDetail;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;

public class ErrorDetailsAction extends ActionBase {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        try {
            IdNameForm form = (IdNameForm) actionForm;
            String action = form.getAction();
            String stackTrace = null;
            if ("getBotTaskConfigurationError".equals(action)) {
                for (Map.Entry<BotTaskIdentifier, Throwable> entry : ProcessExecutionErrors.getBotTaskConfigurationErrors().entrySet()) {
                    if (Objects.equal(entry.getKey().getBot().getId(), form.getId())
                            && Objects.equal(entry.getKey().getBotTaskName(), form.getName())) {
                        stackTrace = Throwables.getStackTraceAsString(entry.getValue());
                        break;
                    }
                }
            } else if ("getProcessError".equals(action)) {
                List<TokenErrorDetail> errorDetails = ProcessExecutionErrors.getProcessErrors(form.getId());
                if (errorDetails != null) {
                    for (TokenErrorDetail detail : errorDetails) {
                        if (Objects.equal(detail.getTaskName(), form.getName())) {
                            stackTrace = Throwables.getStackTraceAsString(detail.getThrowable());
                        }
                    }
                }
            } else {
                log.error("Unknown action: " + action);
            }
            if (stackTrace == null) {
                stackTrace = "No details found";
            }
            OutputStream os = response.getOutputStream();
            os.write(stackTrace.getBytes(Charsets.UTF_8));
            os.flush();
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

}
