package ru.runa.wfe.execution.logic;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.execution.dto.ProcessError;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dto.WfTask;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
        botTaskConfigurationErrors.put(new BotTaskIdentifier(bot, botTask), throwable);
    }

    public static synchronized void removeBotTaskConfigurationError(Bot bot, BotTask botTask) {
        botTaskConfigurationErrors.remove(new BotTaskIdentifier(bot, botTask));
    }

    public static synchronized void addProcessError(Long processId, String nodeId, String taskName, BotTask botTask, Throwable throwable) {
        List<ProcessError> processError = processErrors.get(processId);
        if (processError == null) {
            processError = Lists.newArrayList();
            processErrors.put(processId, processError);
        }
        ProcessError details = new ProcessError(nodeId, taskName, botTask, throwable);
        processError.remove(details);
        processError.add(details);
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
