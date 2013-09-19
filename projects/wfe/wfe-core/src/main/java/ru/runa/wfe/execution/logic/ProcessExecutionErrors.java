package ru.runa.wfe.execution.logic;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dto.WfTask;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ProcessExecutionErrors {
    private static Map<BotTaskIdentifier, Throwable> botTaskConfigurationErrors = Maps.newHashMap();
    private static Map<Long, List<TokenErrorDetail>> processErrors = Maps.newHashMap();

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

    public static synchronized Map<Long, List<TokenErrorDetail>> getProcessErrors() {
        return Maps.newHashMap(processErrors);
    }

    public static synchronized List<TokenErrorDetail> getProcessErrors(Long processId) {
        return processErrors.get(processId);
    }

    public static synchronized void addBotTaskConfigurationError(Bot bot, BotTask botTask, Throwable throwable) {
        botTaskConfigurationErrors.put(new BotTaskIdentifier(bot, botTask), throwable);
    }

    public static synchronized void removeBotTaskConfigurationError(Bot bot, BotTask botTask) {
        botTaskConfigurationErrors.remove(new BotTaskIdentifier(bot, botTask));
    }

    public static synchronized void addProcessError(Long processId, String nodeId, String taskName, BotTask botTask, Throwable throwable) {
        List<TokenErrorDetail> tokenErrorDetail = processErrors.get(processId);
        if (tokenErrorDetail == null) {
            tokenErrorDetail = Lists.newArrayList();
            processErrors.put(processId, tokenErrorDetail);
        }
        TokenErrorDetail details = new TokenErrorDetail(nodeId, taskName, botTask, throwable);
        tokenErrorDetail.remove(details);
        tokenErrorDetail.add(details);
    }

    public static synchronized void addProcessError(WfTask task, BotTask botTask, Throwable throwable) {
        addProcessError(task.getProcessId(), task.getNodeId(), task.getName(), botTask, throwable);
    }

    public static synchronized void addProcessError(Task task, Throwable throwable) {
        addProcessError(task.getProcess().getId(), task.getNodeId(), task.getName(), null, throwable);
    }

    public static synchronized void removeProcessError(Long processId, String nodeId) {
        List<TokenErrorDetail> tokenErrorDetail = processErrors.get(processId);
        if (tokenErrorDetail != null) {
            tokenErrorDetail.remove(new TokenErrorDetail(nodeId, null, null, null));
            if (tokenErrorDetail.isEmpty()) {
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

    public static class TokenErrorDetail {
        private final String nodeId;
        private final String taskName;
        private final BotTask botTask;
        private final Date occuredDate = new Date();
        private final Throwable throwable;

        public TokenErrorDetail(String nodeId, String taskName, BotTask botTask, Throwable throwable) {
            this.nodeId = nodeId;
            this.taskName = taskName;
            this.botTask = botTask;
            this.throwable = throwable;
        }

        public String getNodeId() {
            return nodeId;
        }

        public String getTaskName() {
            return taskName;
        }

        public BotTask getBotTask() {
            return botTask;
        }

        public Date getOccuredDate() {
            return occuredDate;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(nodeId);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TokenErrorDetail) {
                TokenErrorDetail bti = (TokenErrorDetail) obj;
                return Objects.equal(nodeId, bti.nodeId);
            }
            return super.equals(obj);
        }
    }
}
