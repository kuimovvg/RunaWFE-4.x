package ru.runa.wfe.execution.logic;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.execution.dto.ProcessError;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

public class ProcessExecutionErrors {
    private static Map<BotTaskIdentifier, Throwable> botTaskConfigurationErrors = Maps.newHashMap();
    private static Map<Long, List<ProcessError>> processErrors = Maps.newHashMap();

    public static synchronized Map<BotTaskIdentifier, Throwable> getBotTaskConfigurationErrors() {
        return Maps.newHashMap(botTaskConfigurationErrors);
    }

    public static BotTaskIdentifier getBotTaskIdentifierNotNull(Long botId, String botTaskName) {
        for (BotTaskIdentifier botTaskIdentifier : botTaskConfigurationErrors.keySet()) {
            if (botTaskIdentifier.equals(botId, botTaskName)) {
                return botTaskIdentifier;
            }
        }
        throw new InternalApplicationException("No bot task identifier found for " + botId + ", " + botTaskName);
    }

    public static synchronized Map<Long, List<ProcessError>> getProcessErrors() {
        return Maps.newHashMap(processErrors);
    }

    public static synchronized List<ProcessError> getProcessErrors(Long processId) {
        return processErrors.get(processId);
    }

    public static synchronized void addBotTaskConfigurationError(Bot bot, BotTask botTask, Throwable throwable) {
        BotTaskIdentifier botTaskIdentifier = new BotTaskIdentifier(bot, botTask);
        boolean alreadyExists = botTaskConfigurationErrors.containsKey(botTaskIdentifier);
        botTaskConfigurationErrors.put(botTaskIdentifier, throwable);
        if (!alreadyExists) {
            sendEmailNotification(botTaskIdentifier, null);
        }
    }

    public static synchronized void removeBotTaskConfigurationError(Bot bot, BotTask botTask) {
        botTaskConfigurationErrors.remove(new BotTaskIdentifier(bot, botTask));
    }

    public static synchronized void addProcessError(Long processId, String nodeId, String taskName, BotTask botTask, Throwable throwable) {
        List<ProcessError> errors = processErrors.get(processId);
        if (errors == null) {
            errors = Lists.newArrayList();
            processErrors.put(processId, errors);
        }
        ProcessError processError = new ProcessError(nodeId, taskName, botTask, throwable);
        boolean alreadyExists = errors.remove(processError);
        errors.add(processError);
        if (!alreadyExists) {
            sendEmailNotification(null, processError);
        }
    }

    public static synchronized void addProcessError(WfTask task, BotTask botTask, Throwable throwable) {
        addProcessError(task.getProcessId(), task.getNodeId(), task.getName(), botTask, throwable);
    }

    public static synchronized void addProcessError(Task task, Throwable throwable) {
        addProcessError(task.getProcess().getId(), task.getNodeId(), task.getName(), null, throwable);
    }

    public static synchronized void removeProcessError(Long processId, String nodeId) {
        List<ProcessError> processError = processErrors.get(processId);
        if (processError != null) {
            processError.remove(new ProcessError(nodeId));
            if (processError.isEmpty()) {
                processErrors.remove(processId);
            }
        }
    }

    public static synchronized void removeProcessErrors(Long processId) {
        processErrors.remove(processId);
    }

    private static synchronized void sendEmailNotification(BotTaskIdentifier botTaskIdentifier, ProcessError processError) {
        try {
            if (SystemProperties.isErrorEmailNotificationEnabled()) {
                InputStream in = ClassLoaderUtil.getAsStreamNotNull(SystemProperties.getErrorEmailNotificationConfiguration(),
                        ProcessExecutionErrors.class);
                byte[] configBytes = ByteStreams.toByteArray(in);
                EmailConfig config = EmailConfigParser.parse(configBytes);
                Map<String, Object> map = Maps.newHashMap();
                map.put("botTaskIdentifier", botTaskIdentifier);
                map.put("processError", processError);
                IVariableProvider variableProvider = new MapDelegableVariableProvider(map, null);
                config.applySubstitutions(variableProvider);
                EmailUtils.sendMessage(config, null);
            }
        } catch (Exception e) {
            LogFactory.getLog(ProcessExecutionErrors.class).error("Unable to send email notification about error", e);
        }
    }

    public static class BotTaskIdentifier {
        private static final String ANY_TASK = "*";
        private final Bot bot;
        private final BotTask botTask;

        public BotTaskIdentifier(Bot bot, BotTask botTask) {
            this.bot = bot;
            this.botTask = botTask;
        }

        public Bot getBot() {
            return bot;
        }

        public BotTask getBotTask() {
            return botTask;
        }

        public String getBotTaskName() {
            if (botTask != null) {
                return botTask.getName();
            }
            return ANY_TASK;
        }

        public Long getUniqueId() {
            if (botTask != null) {
                return botTask.getId();
            }
            return bot.getId();
        }

        public boolean equals(Long botId, String botTaskName) {
            return Objects.equal(bot.getId(), botId) && Objects.equal(getBotTaskName(), botTaskName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(bot.getUsername(), botTask);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BotTaskIdentifier) {
                BotTaskIdentifier bti = (BotTaskIdentifier) obj;
                return Objects.equal(bot, bti.bot) && Objects.equal(botTask, bti.botTask);
            }
            return super.equals(obj);
        }
    }
}
