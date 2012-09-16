package ru.runa.bpm.job;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.bpm.calendar.BusinessCalendar;
import ru.runa.bpm.calendar.impl.Duration;
import ru.runa.bpm.graph.def.Action;
import ru.runa.bpm.graph.def.Event;
import ru.runa.bpm.graph.def.GraphElement;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.commons.ApplicationContextFactory;

import com.google.common.base.Objects;

@Entity
@DiscriminatorValue(value = "T")
public class Timer extends Job {
    private static Log log = LogFactory.getLog(Timer.class);

    private String name;
    private String repeat;
    private String transitionName;
    private Action action;
    private GraphElement graphElement;

    public Timer() {
    }

    public Timer(Token token) {
        super(token);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getTransitionName() {
        return transitionName;
    }

    public void setTransitionName(String transitionName) {
        this.transitionName = transitionName;
    }

    @Transient
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Transient
    public GraphElement getGraphElement() {
        return graphElement;
    }

    public void setGraphElement(GraphElement graphElement) {
        this.graphElement = graphElement;
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws Exception {
        // add processInstance to autoSave field (For details refer to
        // https://jira.jboss.org/jira/browse/JBPM-1015)
        // TODO jbpmContext.addAutoSaveToken(token);
        // then execute the action if there is one
        if (action != null) {
            try {
                log.debug("executing '" + this + "'");
                if (executionContext.getNode() != null) {
                    executionContext.getNode().fireAndPropagateEvent(Event.EVENTTYPE_TIMER, executionContext);
                    executionContext.getNode().executeAction(action, executionContext);
                } else {
                    action.execute(executionContext);
                }
            } catch (Exception actionException) {
                // NOTE that Error's are not caught because that might halt the
                // JVM and mask the original Error.
                log.warn("timer action threw exception", actionException);
                // if there is a graphElement connected to this timer...
                throw actionException;
            }
        }
        // then take a transition if one is specified
        if (transitionName != null && token.getNode().hasLeavingTransition(transitionName)) {
            token.signal(executionContext, token.getNode().getLeavingTransition(transitionName));
        }
        // save the token
        // TODO jbpmContext.save(processInstance);
        // if repeat is specified, reschedule the job
        if (repeat != null) {
            // suppose that it took the timer runner thread a
            // very long time to execute the timers.
            // then the repeat action dueDate could already have passed.
            Duration duration = new Duration(repeat);
            if (duration.getMilliseconds() > 0) {
                while (dueDate.getTime() <= System.currentTimeMillis()) {
                    BusinessCalendar businessCalendar = ApplicationContextFactory.getBusinessCalendar();
                    dueDate = businessCalendar.add(dueDate, duration);
                }
                log.debug("updated '" + this + "' for repetition on '" + formatDueDate(dueDate) + "'");
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", name).add("dueDate", dueDate).add("processInstance", processInstance).toString();
    }

    private final static String dateFormat = "yyyy-MM-dd HH:mm:ss,SSS"; // TODO
                                                                        // commons

    private static String formatDueDate(Date date) {
        return new SimpleDateFormat(dateFormat).format(date);
    }

}
