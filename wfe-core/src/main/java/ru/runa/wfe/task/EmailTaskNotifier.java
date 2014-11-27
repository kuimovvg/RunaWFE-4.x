package ru.runa.wfe.task;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

public class EmailTaskNotifier implements ITaskNotifier {
    private static final Log log = LogFactory.getLog(EmailTaskNotifier.class);
    @Autowired
    private ExecutorDAO executorDAO;
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
        String emails = "";
        if (task.getExecutor() == null) {
            return;
        }
        if (task.getExecutor() instanceof Actor) {
            Actor actor = (Actor) task.getExecutor();
            if (actor.getEmail() != null && actor.getEmail().trim().length() > 0) {
                emails = actor.getEmail().trim();
            }
        } else {
            Collection<Actor> actors = executorDAO.getGroupActors((Group) task.getExecutor());
            for (Actor actor : actors) {
                if (actor.getEmail() != null && actor.getEmail().trim().length() > 0) {
                    if (emails.length() > 0) {
                        emails += ", ";
                    }
                    emails += actor.getEmail().trim();
                }
            }
        }
        if (onlyIfTaskActorEmailDefined && Strings.isNullOrEmpty(emails)) {
            log.debug("Notification was not sent about task assigned to executor with empty email: " + task);
            return;
        }
        Interaction interaction = executionContext.getProcessDefinition().getInteractionNotNull(task.getNodeId());
        Map<String, Object> map = Maps.newHashMap();
        map.put("interaction", interaction);
        map.put("task", task);
        map.put("emails", emails);
        IVariableProvider variableProvider = new MapDelegableVariableProvider(map, executionContext.getVariableProvider());
        EmailUtils.sendTaskMessage(UserHolder.get(), config, interaction, variableProvider);
        // TODO add process logs about notification
    }
}
