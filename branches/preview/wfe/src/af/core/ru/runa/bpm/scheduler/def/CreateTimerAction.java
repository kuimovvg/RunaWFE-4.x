/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.bpm.scheduler.def;

import java.util.Date;

import org.dom4j.Element;

import ru.runa.bpm.db.JobDAO;
import ru.runa.bpm.graph.def.Action;
import ru.runa.bpm.graph.def.Event;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.job.Timer;
import ru.runa.bpm.jpdl.xml.JpdlXmlReader;
import ru.runa.bpm.taskmgmt.exe.TaskMgmtInstance;
import ru.runa.commons.ApplicationContextFactory;
import ru.runa.commons.ftl.ExpressionEvaluator;

/**
 * Modified on 06.03.2009 by gavrusev_sergei
 * 
 * 
 */
public class CreateTimerAction extends Action {
    private static final long serialVersionUID = 1L;

    String timerName = null;
    String dueDate = null;
    String repeat = null;
    String transitionName = null;
    Action timerAction = null;

    private String defaultDueDate = null;

    public void setDefaultDueDate(String d) {
        this.defaultDueDate = d;
    }

    @Override
    public void read(ExecutableProcessDefinition processDefinition, Element actionElement, JpdlXmlReader jpdlReader) {
        timerName = actionElement.attributeValue("name");
        timerAction = jpdlReader.readSingleAction(processDefinition, actionElement);

        dueDate = actionElement.attributeValue("duedate");
        if (dueDate == null) {
            dueDate = defaultDueDate;
        }
        repeat = actionElement.attributeValue("repeat");
        if ("true".equalsIgnoreCase(repeat) || "yes".equalsIgnoreCase(repeat)) {
            repeat = dueDate;
        }
        transitionName = actionElement.attributeValue("transition");
    }

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        Timer timer = createTimer(executionContext);
        JobDAO jobDAO = ApplicationContextFactory.getJobSession();
        jobDAO.saveJob(timer, true);
    }

    private Date getDueDateDate(ExecutionContext executionContext) {
        if (dueDate == null || dueDate.length() == 0) {
            dueDate = TaskMgmtInstance.getDefaultTaskDeadline();
        }
        return ExpressionEvaluator.evaluateDuration(dueDate, executionContext);
    }

    protected Timer createTimer(ExecutionContext executionContext) {
        Timer timer = new Timer(executionContext.getToken());
        timer.setName(timerName);
        timer.setRepeat(repeat != "" ? repeat : TaskMgmtInstance.getDefaultTaskDeadline());
        timer.setDueDate(getDueDateDate(executionContext));
        timer.setAction(timerAction);
        timer.setTransitionName(transitionName);
        timer.setTaskInstance(executionContext.getTaskInstance());

        // if this action was executed for a graph element
        if (getEvent() != null && getEvent().getGraphElement() != null) {
            getEvent().getGraphElement().fireEvent(Event.EVENTTYPE_TIMER_CREATE, executionContext);
        }

        return timer;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDateDuration) {
        this.dueDate = dueDateDuration;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeatDuration) {
        this.repeat = repeatDuration;
    }

    public String getTransitionName() {
        return transitionName;
    }

    public void setTransitionName(String transitionName) {
        this.transitionName = transitionName;
    }

    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public Action getTimerAction() {
        return timerAction;
    }

    public void setTimerAction(Action timerAction) {
        this.timerAction = timerAction;
    }
}
