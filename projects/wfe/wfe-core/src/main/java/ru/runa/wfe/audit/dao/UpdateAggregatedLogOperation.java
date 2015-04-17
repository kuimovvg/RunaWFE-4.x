package ru.runa.wfe.audit.dao;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;

import ru.runa.wfe.audit.ActionLog;
import ru.runa.wfe.audit.AdminActionLog;
import ru.runa.wfe.audit.CreateTimerActionLog;
import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.audit.ProcessCancelLog;
import ru.runa.wfe.audit.ProcessEndLog;
import ru.runa.wfe.audit.ProcessLogVisitor;
import ru.runa.wfe.audit.ProcessStartLog;
import ru.runa.wfe.audit.ReceiveMessageLog;
import ru.runa.wfe.audit.SendMessageLog;
import ru.runa.wfe.audit.SubprocessEndLog;
import ru.runa.wfe.audit.SubprocessStartLog;
import ru.runa.wfe.audit.SwimlaneAssignLog;
import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.TaskCancelledLog;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.audit.TaskEndBySubstitutorLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.audit.TaskEscalationLog;
import ru.runa.wfe.audit.TaskExpiredLog;
import ru.runa.wfe.audit.TaskRemovedOnProcessEndLog;
import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.audit.VariableCreateLog;
import ru.runa.wfe.audit.VariableDeleteLog;
import ru.runa.wfe.audit.VariableUpdateLog;
import ru.runa.wfe.audit.aggregated.ProcessInstanceAggregatedLog;
import ru.runa.wfe.audit.aggregated.TaskAggregatedLog;
import ru.runa.wfe.audit.aggregated.TaskAggregatedLog.EndReason;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

public class UpdateAggregatedLogOperation implements ProcessLogVisitor {

    private final HibernateTemplate hibernateTemplate;
    private final Process process;
    private final Token token;
    private final IProcessDefinitionLoader processDefinitionLoader;

    public UpdateAggregatedLogOperation(HibernateTemplate hibernateTemplate, IProcessDefinitionLoader processDefinitionLoader, Process process,
            Token token) {
        this.hibernateTemplate = hibernateTemplate;
        this.processDefinitionLoader = processDefinitionLoader;
        this.process = process;
        this.token = token;
    }

    @Override
    public void OnProcessStartLog(ProcessStartLog processStartLog) {
        if (getProcessInstanceLog(processStartLog.getProcessId()) != null) {
            return;
        }
        hibernateTemplate.save(new ProcessInstanceAggregatedLog(processStartLog, process, token));
    }

    @Override
    public void OnProcessEndLog(ProcessEndLog processEndLog) {
        ProcessInstanceAggregatedLog logEntry = getProcessInstanceLog(processEndLog.getProcessId());
        if (logEntry == null) {
            return;
        }
        logEntry.update(processEndLog);
        hibernateTemplate.merge(logEntry);
    }

    @Override
    public void OnProcessCancelLog(ProcessCancelLog processCancelLog) {
        ProcessInstanceAggregatedLog logEntry = getProcessInstanceLog(processCancelLog.getProcessId());
        if (logEntry == null) {
            return;
        }
        logEntry.update(processCancelLog);
        hibernateTemplate.merge(logEntry);
    }

    @Override
    public void OnNodeEnterLog(NodeEnterLog nodeEnterLog) {
    }

    @Override
    public void OnNodeLeaveLog(NodeLeaveLog nodeLeaveLog) {
    }

    @Override
    public void OnReceiveMessageLog(ReceiveMessageLog receiveMessageLog) {
    }

    @Override
    public void OnSendMessageLog(SendMessageLog sendMessageLog) {
    }

    @Override
    public void OnSubprocessStartLog(SubprocessStartLog subprocessStartLog) {
    }

    @Override
    public void OnSubprocessEndLog(SubprocessEndLog subprocessEndLog) {
    }

    @Override
    public void OnActionLog(ActionLog actionLog) {
    }

    @Override
    public void OnCreateTimerActionLog(CreateTimerActionLog createTimerActionLog) {
    }

    @Override
    public void OnTaskCreateLog(TaskCreateLog taskCreateLog) {
        if (getTaskLog(taskCreateLog.getTaskId()) != null) {
            return;
        }
        hibernateTemplate.save(new TaskAggregatedLog(taskCreateLog, processDefinitionLoader, process, token));
    }

    @Override
    public void OnTaskAssignLog(TaskAssignLog taskAssignLog) {
        TaskAggregatedLog logEntry = getTaskLog(taskAssignLog.getTaskId());
        if (logEntry == null) {
            return;
        }
        logEntry.updateAssignment(taskAssignLog);
        hibernateTemplate.merge(logEntry);
    }

    @Override
    public void OnTaskEndLog(TaskEndLog taskEndLog) {
        onTaskEnd(taskEndLog, EndReason.COMPLETED);
    }

    @Override
    public void OnTaskEscalationLog(TaskEscalationLog taskEscalationLog) {
    }

    @Override
    public void OnTaskRemovedOnProcessEndLog(TaskRemovedOnProcessEndLog taskRemovedOnProcessEndLog) {
        onTaskEnd(taskRemovedOnProcessEndLog, EndReason.PROCESS_END);
    }

    @Override
    public void OnTaskExpiredLog(TaskExpiredLog taskExpiredLog) {
        onTaskEnd(taskExpiredLog, EndReason.TIMEOUT);
    }

    @Override
    public void OnTaskEndBySubstitutorLog(TaskEndBySubstitutorLog taskEndBySubstitutorLog) {
        onTaskEnd(taskEndBySubstitutorLog, EndReason.SUBSTITUTOR_END);
    }

    @Override
    public void OnTaskCancelledLog(TaskCancelledLog taskCancelledLog) {
        onTaskEnd(taskCancelledLog, EndReason.CANCELLED);
    }

    @Override
    public void OnSwimlaneAssignLog(SwimlaneAssignLog swimlaneAssignLog) {
    }

    @Override
    public void OnTransitionLog(TransitionLog transitionLog) {
    }

    @Override
    public void OnVariableCreateLog(VariableCreateLog variableCreateLog) {
    }

    @Override
    public void OnVariableDeleteLog(VariableDeleteLog variableDeleteLog) {
    }

    @Override
    public void OnVariableUpdateLog(VariableUpdateLog variableUpdateLog) {
    }

    @Override
    public void OnAdminActionLog(AdminActionLog adminActionLog) {
    }

    private ProcessInstanceAggregatedLog getProcessInstanceLog(long processId) {
        String query = "from ProcessInstanceAggregatedLog where processInstanceId=? order by processInstanceId desc";
        List<ProcessInstanceAggregatedLog> existing = hibernateTemplate.find(query, processId);
        if (existing != null && !existing.isEmpty()) {
            return existing.get(0);
        }
        return null;
    }

    private TaskAggregatedLog getTaskLog(long taskId) {
        String query = "from TaskAggregatedLog where taskId=? order by taskId desc";
        List<TaskAggregatedLog> existing = hibernateTemplate.find(query, taskId);
        if (existing != null && !existing.isEmpty()) {
            return existing.get(0);
        }
        return null;
    }

    private void onTaskEnd(TaskEndLog taskEndLog, EndReason endReason) {
        TaskAggregatedLog logEntry = getTaskLog(taskEndLog.getTaskId());
        if (logEntry == null) {
            return;
        }
        logEntry.updateOnEnd(taskEndLog.getCreateDate(), taskEndLog.getActorName(), endReason);
        hibernateTemplate.merge(logEntry);
    }
}
