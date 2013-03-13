package ru.runa.wfe.execution.logic;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.bot.Bot;
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

    public static synchronized Map<Long, List<TokenErrorDetail>> getProcessErrors() {
        return Maps.newHashMap(processErrors);
    }

    public static synchronized List<TokenErrorDetail> getProcessErrors(Long processId) {
        return processErrors.get(processId);
    }

    public static synchronized void addBotTaskConfigurationError(Bot bot, String botTaskName, Throwable throwable) {
        botTaskConfigurationErrors.put(new BotTaskIdentifier(bot, botTaskName), throwable);
    }

    public static synchronized void removeBotTaskConfigurationError(Bot bot, String botTaskName) {
        botTaskConfigurationErrors.remove(new BotTaskIdentifier(bot, botTaskName));
    }

    // TODO unused in AsyncWorkflowBot
    public static synchronized void addBotTaskNotFoundProcessError(WfTask task, Bot bot, String botTaskName) {
        Throwable ce = botTaskConfigurationErrors.get(new BotTaskIdentifier(bot, botTaskName));
        Exception throwable;
        if (ce != null) {
            throwable = new ProcessExecutionException(ProcessExecutionException.BOT_TASK_CONFIGURATION_ERROR, botTaskName, ce.getMessage());
        } else {
            throwable = new ProcessExecutionException(ProcessExecutionException.BOT_TASK_MISSED, botTaskName, bot.getUsername());
        }
        addProcessError(task.getProcessId(), task.getName(), throwable);
    }

    public static synchronized void addProcessError(Long processId, String taskName, Throwable throwable) {
        List<TokenErrorDetail> tokenErrorDetail = processErrors.get(processId);
        if (tokenErrorDetail == null) {
            tokenErrorDetail = Lists.newArrayList();
            processErrors.put(processId, tokenErrorDetail);
        }
        TokenErrorDetail details = new TokenErrorDetail(taskName, throwable);
        tokenErrorDetail.remove(details);
        tokenErrorDetail.add(details);
    }

    public static synchronized void removeProcessError(Long processId, String taskName) {
        List<TokenErrorDetail> tokenErrorDetail = processErrors.get(processId);
        if (tokenErrorDetail != null) {
            tokenErrorDetail.remove(new TokenErrorDetail(taskName, null));
            if (tokenErrorDetail.isEmpty()) {
                processErrors.remove(processId);
            }
        }
    }

    public static synchronized void removeProcessErrors(Long processId) {
        processErrors.remove(processId);
    }

    public static class BotTaskIdentifier {
        private final Bot bot;
        private final String botTaskName;

        public BotTaskIdentifier(Bot bot, String botTaskName) {
            this.bot = bot;
            this.botTaskName = botTaskName;
        }

        public Bot getBot() {
            return bot;
        }

        public String getBotTaskName() {
            return botTaskName;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(bot.getUsername(), botTaskName);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BotTaskIdentifier) {
                BotTaskIdentifier bti = (BotTaskIdentifier) obj;
                return Objects.equal(bot.getUsername(), bti.bot.getUsername()) && Objects.equal(botTaskName, bti.botTaskName);
            }
            return super.equals(obj);
        }
    }

    public static class TokenErrorDetail {
        private final String taskName;
        private final Date occuredDate = new Date();
        private final Throwable throwable;

        public TokenErrorDetail(String taskName, Throwable throwable) {
            this.taskName = taskName;
            this.throwable = throwable;
        }

        public String getTaskName() {
            return taskName;
        }

        public Date getOccuredDate() {
            return occuredDate;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(taskName);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TokenErrorDetail) {
                TokenErrorDetail bti = (TokenErrorDetail) obj;
                return Objects.equal(taskName, bti.taskName);
            }
            return super.equals(obj);
        }
    }
}
