package ru.runa.wfe.audit;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ProcessLogs implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<ProcessLog> logs = Lists.newArrayList();
    private final Map<Long, Integer> processIdLevels = Maps.newHashMap();

    public ProcessLogs(Long processId) {
        processIdLevels.put(processId, 0);
    }

    public void addLogs(List<ProcessLog> processLogs) {
        this.logs.addAll(processLogs);
        for (ProcessLog log : processLogs) {
            if (log instanceof SubprocessStartLog) {
                Long subprocessId = ((SubprocessStartLog) log).getSubprocessId();
                int superProcessLevel = getLevel(log);
                processIdLevels.put(subprocessId, superProcessLevel + 1);
            }
        }
    }

    public int getMaxSubprocessLevel() {
        int level = 0;
        for (Integer l : processIdLevels.values()) {
            if (l > level) {
                level = l;
            }
        }
        return level;
    }

    public int getLevel(ProcessLog processLog) {
        return processIdLevels.get(processLog.getProcessId());
    }

    public List<ProcessLog> getLogs() {
        return logs;
    }

    public Map<TaskCreateLog, TaskEndLog> getTaskLogs() {
        Map<String, TaskCreateLog> tmp = Maps.newHashMap();
        Map<TaskCreateLog, TaskEndLog> result = Maps.newHashMap();
        for (ProcessLog log : logs) {
            if (log instanceof TaskCreateLog) {
                String key = log.getProcessId() + ((TaskLog) log).getTaskName();
                tmp.put(key, (TaskCreateLog) log);
            }
            if (log instanceof TaskEndLog) {
                String key = log.getProcessId() + ((TaskLog) log).getTaskName();
                TaskCreateLog taskCreateLog = tmp.remove(key);
                if (taskCreateLog == null) {
                    throw new InternalApplicationException("No TaskCreateLog for " + log);
                }
                result.put(taskCreateLog, (TaskEndLog) log);
            }
        }
        // unfinished tasks
        for (TaskCreateLog taskCreateLog : tmp.values()) {
            result.put(taskCreateLog, null);
        }
        return result;
    }
}
