package ru.runa.common.web.action;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.form.IdNameForm;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors.BotTaskIdentifier;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;

public class ErrorDetailsAction extends ActionBase {
    private static final Log log = LogFactory.getLog(AdminkitScriptsAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        IdNameForm form = (IdNameForm) actionForm;
        String action = form.getAction();
        String errorDetails = null;
        if ("getBotTaskConfigurationError".equals(action)) {
            for (Map.Entry<BotTaskIdentifier, Throwable> entry : ProcessExecutionErrors.getBotTaskConfigurationErrors().entrySet()) {
                if (Objects.equal(entry.getKey().getBot().getId(), form.getId()) && Objects.equal(entry.getKey().getBotTaskName(), form.getName())) {
                    errorDetails = Throwables.getStackTraceAsString(entry.getValue());
                    break;
                }
            }
        } else if ("getProcessError".equals(action)) {
            for (Map.Entry<Long, Map<String, Throwable>> processEntry : ProcessExecutionErrors.getProcessErrors().entrySet()) {
                if (Objects.equal(processEntry.getKey(), form.getId())) {
                    for (Map.Entry<String, Throwable> taskEntry : processEntry.getValue().entrySet()) {
                        if (Objects.equal(taskEntry.getKey(), form.getName())) {
                            errorDetails = Throwables.getStackTraceAsString(taskEntry.getValue());
                        }
                    }
                    if (errorDetails != null) {
                        break;
                    }
                }
            }
        } else {
            log.error("Unknown action: " + action);
        }
        if (errorDetails == null) {
            errorDetails = "No details found";
        }
        try {
            OutputStream os = response.getOutputStream();
            os.write(errorDetails.getBytes(Charsets.UTF_8));
            os.flush();
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

}
