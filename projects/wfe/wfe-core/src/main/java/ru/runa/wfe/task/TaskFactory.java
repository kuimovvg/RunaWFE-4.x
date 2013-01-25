package ru.runa.wfe.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.Event;
import ru.runa.wfe.lang.StartState;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.task.logic.ITaskNotifier;

public class TaskFactory {
    private static final Log log = LogFactory.getLog(TaskFactory.class);

    private String defaultTaskDeadline;
    private ITaskNotifier taskNotifier;

    public void setTaskNotifier(ITaskNotifier taskNotifier) {
        this.taskNotifier = taskNotifier;
    }

    @Required
    public void setDefaultTaskDeadline(String defaultTaskDeadline) {
        this.defaultTaskDeadline = defaultTaskDeadline;
    }

    public String getDeadlineDuration(TaskDefinition taskDefinition) {
        if (taskDefinition.getDeadlineDuration() != null) {
            return taskDefinition.getDeadlineDuration();
        }
        return defaultTaskDeadline;
    }

    /**
     * creates a new task on the given task, in the given execution context.
     */
    public Task create(ExecutionContext executionContext, TaskDefinition taskDefinition) {
        Process process = executionContext.getProcess();
        Task task = new Task(taskDefinition);
        Token token = executionContext.getToken();
        task.setToken(token);
        task.setProcess(process);
        executionContext.addLog(new TaskCreateLog(task));
        task.setDeadlineDate(ExpressionEvaluator.evaluateDuration(executionContext, getDeadlineDuration(taskDefinition)));
        taskDefinition.fireEvent(executionContext, Event.EVENTTYPE_TASK_CREATE);
        process.getTasks().add(task);
        return task;
    }

    /**
     * assigns a task based on swimlane definition.
     */
    public void assign(ExecutionContext executionContext, TaskDefinition taskDefinition, Task task) {
        Process process = executionContext.getProcess();
        SwimlaneDefinition swimlaneDefinition = taskDefinition.getSwimlane();
        Swimlane swimlane = null;
        // if this is a task assignment for a start-state
        if (taskDefinition.getNode() instanceof StartState) {
            swimlane = process.getSwimlane(swimlaneDefinition.getName());
            if (swimlane == null) {
                // swimlane.setExecutor(executionContext,
                // SubjectPrincipalHolder.actors.get(), true);
                throw new UnsupportedOperationException("use ru.runa.wfe.execution.TaskFactory.createStart(ExecutionContext, Actor)");
            }
        } else {
            try {
                swimlane = process.getInitializedSwimlaneNotNull(executionContext, swimlaneDefinition, taskDefinition.isReassignSwimlane());
            } catch (Exception e) {
                log.error("Unable to assign in process id = " + executionContext.getProcess().getId(), e);
            }
        }
        // copy the swimlane assignment into the task
        task.setSwimlane(swimlane);
        task.assignExecutor(executionContext, swimlane.getExecutor(), false);
    }

    /**
     * invokes task notifier
     */
    public void notify(ExecutionContext executionContext, Task task) {
        try {
            if (taskNotifier != null) {
                taskNotifier.onNewTask(new ExecutionContext(executionContext.getProcessDefinition(), task));
            }
        } catch (Exception e) {
            log.info("Task notifier error", e);
        }
    }

}
