package ru.runa.wfe.task;

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.security.auth.UserHolder;
import ru.runa.wfe.task.logic.ITaskNotifier;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

public class EmailTaskNotifier implements ITaskNotifier {
    private static final Log log = LogFactory.getLog(EmailTaskNotifier.class);

    private boolean enabled = true;
    private boolean onlyIfTaskActorEmailDefined = false;
    private byte[] configBytes;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setOnlyIfTaskActorEmailDefined(boolean onlyIfTaskActorEmailDefined) {
        this.onlyIfTaskActorEmailDefined = onlyIfTaskActorEmailDefined;
    }

    @Required
    public void setConfigLocation(String path) {
        try {
            InputStream in = ClassLoaderUtil.getAsStreamNotNull(path, getClass());
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
        if (onlyIfTaskActorEmailDefined) {
            if (task.getExecutor() instanceof Actor) {
                if (Strings.isNullOrEmpty(((Actor) task.getExecutor()).getEmail())) {
                    log.debug("Notification was not sent about task assigned to actor without email: " + task);
                    return;
                }
            } else {
                log.debug("Notification was not sent about task assigned to group: " + task);
                return;
            }
        }
        Interaction interaction = executionContext.getProcessDefinition().getInteractionNotNull(task.getNodeId());
        Map<String, Object> map = Maps.newHashMap();
        map.put("interaction", interaction);
        map.put("task", task);
        IVariableProvider variableProvider = new MapDelegableVariableProvider(map, executionContext.getVariableProvider());
        EmailUtils.sendTaskMessage(UserHolder.get(), config, interaction, variableProvider);
    }
}
