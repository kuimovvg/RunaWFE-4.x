package ru.runa.wfe.task;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.security.auth.SubjectHolder;
import ru.runa.wfe.task.logic.ITaskNotifier;

import com.google.common.io.ByteStreams;

public class EmailTaskNotifier implements ITaskNotifier {
    private static final Log log = LogFactory.getLog(EmailTaskNotifier.class);

    private boolean enabled = true;
    private byte[] configBytes;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Required
    public void setConfigLocation(String path) {
        try {
            InputStream in = ClassLoaderUtil.getResourceAsStream(path, getClass());
            configBytes = ByteStreams.toByteArray(in);
        } catch (Exception e) {
            log.error("Configuration error", e);
        }
    }

    @Override
    public void onNewTask(ExecutionContext executionContext) throws Exception {
        if (!enabled || configBytes == null) {
            return;
        }
        EmailConfig config = EmailConfigParser.parse(configBytes);
        Task task = executionContext.getTask();
        Interaction interaction = executionContext.getProcessDefinition().getInteractionNotNull(task.getNodeId());
        EmailUtils.sendTaskMessage(SubjectHolder.get(), config, interaction, executionContext.getVariableProvider(),
                executionContext.getProcessDefinition());
    }

}
