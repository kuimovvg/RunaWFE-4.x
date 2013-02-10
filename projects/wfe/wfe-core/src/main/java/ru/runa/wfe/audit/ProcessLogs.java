package ru.runa.wfe.audit;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.WfException;
import ru.runa.wfe.lang.NodeType;

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
        logs.addAll(processLogs);
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

    public <T extends ProcessLog> T getFirstOrNull(Class<T> logClass) {
        for (ProcessLog log : logs) {
            if (log.getClass() == logClass) {
                return (T) log;
            }
        }
        return null;
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
                    throw new WfException("No TaskCreateLog for " + log);
                }
                result.put(taskCreateLog, (TaskEndLog) log);
            }
            if (log instanceof NodeLeaveLog) {
                NodeLeaveLog nodeLeaveLog = (NodeLeaveLog) log;
                if (NodeType.StartState.name().equals(nodeLeaveLog.getNodeType())) {
                    ProcessStartLog processStartLog = getFirstOrNull(ProcessStartLog.class);
                    if (processStartLog == null) {
                        continue;
                    }
                    TaskCreateLog taskCreateLog = new TaskCreateLog();
                    taskCreateLog.setId(processStartLog.getId());
                    taskCreateLog.setDate(nodeLeaveLog.getDate());
                    taskCreateLog.setProcessId(nodeLeaveLog.getProcessId());
                    taskCreateLog.setSeverity(nodeLeaveLog.getSeverity());
                    taskCreateLog.setTokenId(nodeLeaveLog.getTokenId());
                    taskCreateLog.addAttribute(IAttributes.ATTR_TASK_NAME, nodeLeaveLog.getNodeName());
                    TaskEndLog taskEndLog = new TaskEndLog();
                    taskEndLog.setId(processStartLog.getId());
                    taskEndLog.setDate(nodeLeaveLog.getDate());
                    taskEndLog.setProcessId(nodeLeaveLog.getProcessId());
                    taskEndLog.setSeverity(nodeLeaveLog.getSeverity());
                    taskEndLog.setTokenId(nodeLeaveLog.getTokenId());
                    taskEndLog.addAttribute(IAttributes.ATTR_TASK_NAME, nodeLeaveLog.getNodeName());
                    taskEndLog.addAttribute(IAttributes.ATTR_ACTOR_NAME, processStartLog.getActorName());
                    result.put(taskCreateLog, taskEndLog);
                }
            }
        }
        // unfinished tasks
        for (TaskCreateLog taskCreateLog : tmp.values()) {
            result.put(taskCreateLog, null);
        }
        return result;
    }
}
