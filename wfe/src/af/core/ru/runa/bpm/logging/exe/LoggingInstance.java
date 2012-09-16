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
package ru.runa.bpm.logging.exe;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ru.runa.bpm.logging.log.CompositeLog;
import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.bpm.module.exe.ModuleInstance;

/**
 * non persisted class that collects {@link ru.runa.bpm.logging.log.ProcessLog}s
 * during process execution.  When the process instance gets saved, the 
 * process logs will be saved by the {@link ru.runa.bpm.db.LoggingDAO}.
 */
public class LoggingInstance extends ModuleInstance {
    private static final long serialVersionUID = 1L;

    private List<ProcessLog> logs = new ArrayList<ProcessLog>();
    private transient LinkedList<CompositeLog> compositeLogStack = new LinkedList<CompositeLog>();

    public LoggingInstance() {
    }

    public void startCompositeLog(CompositeLog compositeLog) {
        addLog(compositeLog);
        compositeLogStack.addFirst(compositeLog);
    }

    public void endCompositeLog() {
        compositeLogStack.removeFirst();
    }

    public void addLog(ProcessLog processLog) {
        if (!compositeLogStack.isEmpty()) {
            CompositeLog currentCompositeLog = (CompositeLog) compositeLogStack.getFirst();
            processLog.setParent(currentCompositeLog);
            currentCompositeLog.addChild(processLog);
        }
        processLog.setDate(new Date());
        logs.add(processLog);
    }

    public List<ProcessLog> getLogs() {
        return logs;
    }

}
