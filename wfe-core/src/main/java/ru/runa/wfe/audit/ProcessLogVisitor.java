package ru.runa.wfe.audit;

public interface ProcessLogVisitor {

    void OnProcessStartLog(ProcessStartLog processStartLog);

    void OnProcessEndLog(ProcessEndLog processEndLog);

    void OnProcessCancelLog(ProcessCancelLog processCancelLog);

    void OnNodeEnterLog(NodeEnterLog nodeEnterLog);

    void OnNodeLeaveLog(NodeLeaveLog nodeLeaveLog);

    void OnReceiveMessageLog(ReceiveMessageLog receiveMessageLog);

    void OnSendMessageLog(SendMessageLog sendMessageLog);

    void OnSubprocessStartLog(SubprocessStartLog subprocessStartLog);

    void OnSubprocessEndLog(SubprocessEndLog subprocessEndLog);

    void OnActionLog(ActionLog actionLog);

    void OnCreateTimerActionLog(CreateTimerActionLog createTimerActionLog);

    void OnTaskCreateLog(TaskCreateLog taskCreateLog);

    void OnTaskAssignLog(TaskAssignLog taskAssignLog);

    void OnTaskEndLog(TaskEndLog taskEndLog);

    void OnTaskEscalationLog(TaskEscalationLog taskEscalationLog);

    void OnTaskRemovedOnProcessEndLog(TaskRemovedOnProcessEndLog taskRemovedOnProcessEndLog);

    void OnTaskExpiredLog(TaskExpiredLog taskExpiredLog);

    void OnTaskEndBySubstitutorLog(TaskEndBySubstitutorLog taskEndBySubstitutorLog);

    void OnTaskCancelledLog(TaskCancelledLog taskCancelledLog);

    void OnSwimlaneAssignLog(SwimlaneAssignLog swimlaneAssignLog);

    void OnTransitionLog(TransitionLog transitionLog);

    void OnVariableCreateLog(VariableCreateLog variableCreateLog);

    void OnVariableDeleteLog(VariableDeleteLog variableDeleteLog);

    void OnVariableUpdateLog(VariableUpdateLog variableUpdateLog);

    void OnAdminActionLog(AdminActionLog adminActionLog);
}
