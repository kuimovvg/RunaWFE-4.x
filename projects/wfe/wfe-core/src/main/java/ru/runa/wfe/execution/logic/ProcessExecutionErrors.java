package ru.runa.wfe.execution.logic;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.task.dto.WfTask;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ProcessExecutionErrors { // TODO nodeID instead of taskName?
    // TODO "Date"
    private static Map<BotTaskIdentifier, Throwable> botTaskConfigurationErrors = Maps.newHashMap();
    private static Map<Long, Map<String, Throwable>> processErrors = Maps.newHashMap();

    public static synchronized Map<BotTaskIdentifier, Throwable> getBotTaskConfigurationErrors() {
        return Maps.newHashMap(botTaskConfigurationErrors);
    }

    public static synchronized Map<Long, Map<String, Throwable>> getProcessErrors() {
        return Maps.newHashMap(processErrors);
    }

    public static synchronized void addBotTaskConfigurationError(String botName, String botTaskName, Throwable throwable) {
        botTaskConfigurationErrors.put(new BotTaskIdentifier(botName, botTaskName), throwable);
    }

    public static synchronized void removeBotTaskConfigurationError(String botName, String botTaskName) {
        botTaskConfigurationErrors.remove(new BotTaskIdentifier(botName, botTaskName));
    }

    public static synchronized void addBotTaskNotFoundProcessError(WfTask task, String botName, String botTaskName) {
        Throwable ce = botTaskConfigurationErrors.get(new BotTaskIdentifier(botName, botTaskName));
        Exception throwable;
        if (ce != null) {
            throwable = new ProcessExecutionException(ProcessExecutionException.BOT_TASK_CONFIGURATION_ERROR, botTaskName, ce.getMessage());
        } else {
            throwable = new ProcessExecutionException(ProcessExecutionException.BOT_TASK_MISSED, botTaskName, botName);
        }
        Map<String, Throwable> map = processErrors.get(task.getProcessId());
        if (map == null) {
            map = Maps.newHashMap();
            processErrors.put(task.getProcessId(), map);
        }
        map.put(task.getName(), throwable);
    }

    public static synchronized void addProcessError(Long processId, String taskName, Throwable throwable) {
        Map<String, Throwable> map = processErrors.get(processId);
        if (map == null) {
            map = Maps.newHashMap();
            processErrors.put(processId, map);
        }
        map.put(taskName, throwable);
    }

    public static synchronized void removeProcessError(Long processId, String taskName) {
        Map<String, Throwable> map = processErrors.get(processId);
        if (map != null) {
            map.remove(taskName);
            if (map.isEmpty()) {
                processErrors.remove(processId);
            }
        }
    }

    public static synchronized void removeProcessErrors(Long processId) {
        processErrors.remove(processId);
    }

    public static synchronized List<Throwable> getProcessErrorsAsList(Long processId) {
        Map<String, Throwable> map = processErrors.get(processId);
        if (map != null) {
            return Lists.newArrayList(map.values());
        }
        return null;
    }

    public static class BotTaskIdentifier {
        private final String botName;
        private final String botTaskName;

        public BotTaskIdentifier(String botName, String botTaskName) {
            this.botName = botName;
            this.botTaskName = botTaskName;
        }

        public String getBotName() {
            return botName;
        }

        public String getBotTaskName() {
            return botTaskName;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(botName, botTaskName);
        }

        @Override
        public boolean equals(Object obj) {
            BotTaskIdentifier bti = (BotTaskIdentifier) obj;
            return Objects.equal(botName, bti.botName) && Objects.equal(botTaskName, bti.botTaskName);
        }
    }
}
