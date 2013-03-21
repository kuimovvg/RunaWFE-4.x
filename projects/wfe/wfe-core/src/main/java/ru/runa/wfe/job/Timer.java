package ru.runa.wfe.job;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.calendar.BusinessCalendar;
import ru.runa.wfe.commons.calendar.impl.Duration;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.execution.logic.ProcessExecutionException;
import ru.runa.wfe.lang.Action;
import ru.runa.wfe.lang.Event;
import ru.runa.wfe.task.Task;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;

@Entity
@DiscriminatorValue(value = "T")
public class Timer extends Job {
    private static Log log = LogFactory.getLog(Timer.class);
    public static final String ESCALATION_NAME = "__ESCALATION";
    public static final String STOP_RE_EXECUTION = "STOP_RE_EXECUTION";

    private String repeatDurationString;
    private String outTransitionName;

    public Timer() {
    }

    public Timer(Token token) {
        super(token);
    }

    @Column(name = "REPEAT_DURATION")
    public String getRepeatDurationString() {
        return repeatDurationString;
    }

    public void setRepeatDurationString(String repeatDurationString) {
        this.repeatDurationString = repeatDurationString;
    }

    @Column(name = "TRANSITION_NAME")
    public String getOutTransitionName() {
        return outTransitionName;
    }

    public void setOutTransitionName(String outTransitionName) {
        this.outTransitionName = outTransitionName;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        try {
            Event event = executionContext.getNode().getEvent(Event.EVENTTYPE_TIMER);
            if (event != null) {
                for (Action timerAction : event.getActions()) {
                    // in case of multiple timers on node we discriminate
                    // actions by name
                    if (Objects.equal(getName(), timerAction.getName())) {
                        timerAction.execute(executionContext);
                    }
                }

            }
            if (outTransitionName != null) {
                // CancelTimerAction should cancel this timer
                Task task = executionContext.getTask();
                if (task != null) {
                    Swimlane swimlane = task.getSwimlane();
                    if (swimlane != null) {
                        // mark task completed by timer
                        swimlane.setExecutor(null);
                        // AssignmentHelper assignmentHelper =
                        // ApplicationContextFactory.getAssignmentHelper();
                        // assignmentHelper.reassignTask(executionContext, task,
                        // swimlane.getExecutor(), false);
                    }
                } else {
                    log.warn("Task is null in timer node '" + getToken().getNodeId() + "' when leaving by transition: " + outTransitionName);
                }
                log.info("Leaving " + this + " by transition " + outTransitionName);
                getToken().signal(executionContext, executionContext.getNode().getLeavingTransitionNotNull(outTransitionName));
            } else if (Boolean.TRUE == executionContext.getTransientVariable(STOP_RE_EXECUTION)) {
                log.info("Deleting " + this + " due to STOP_RE_EXECUTION");
                ApplicationContextFactory.getJobDAO().deleteTimersByName(getName(), getToken());
            } else if (repeatDurationString != null) {
                // restart timer
                Duration repeatDuration = new Duration(repeatDurationString);
                if (repeatDuration.getMilliseconds() > 0) {
                    BusinessCalendar businessCalendar = ApplicationContextFactory.getBusinessCalendar();
                    setDueDate(businessCalendar.add(getDueDate(), repeatDuration));
                    log.info("Restarting " + this + " for repeat execution at " + CalendarUtil.formatDateTime(getDueDate()));
                }
            } else {
                log.info("Deleting " + this + " after execution");
                ApplicationContextFactory.getJobDAO().deleteTimersByName(getName(), getToken());
            }
            ProcessExecutionErrors.removeProcessError(getProcess().getId(), getToken().getNodeId());
        } catch (Throwable th) {
            ProcessExecutionException pee = new ProcessExecutionException(ProcessExecutionException.TIMER_EXECUTION_FAILED, th, th.getMessage());
            ProcessExecutionErrors.addProcessError(getProcess().getId(), getToken().getNodeId(), pee);
            throw Throwables.propagate(th);
        }
    }

}
