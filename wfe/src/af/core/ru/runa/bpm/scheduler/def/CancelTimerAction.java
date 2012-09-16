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

import org.dom4j.Element;

import ru.runa.bpm.db.JobDAO;
import ru.runa.bpm.graph.def.Action;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.jpdl.xml.JpdlXmlReader;
import ru.runa.commons.ApplicationContextFactory;
import ru.runa.wf.ProcessDefinitionXMLFormatException;

public class CancelTimerAction extends Action {
    private static final long serialVersionUID = 1L;
    private String timerName = null;

    public void read(ExecutableProcessDefinition processDefinition, Element actionElement, JpdlXmlReader jpdlReader) {
        timerName = actionElement.attributeValue("name");
        if (timerName == null) {
            throw new ProcessDefinitionXMLFormatException("no 'name' specified in CancelTimerAction '" + actionElement.asXML() + "'");
        }
    }

    public void execute(ExecutionContext executionContext) throws Exception {
        JobDAO jobDAO = ApplicationContextFactory.getJobSession();
        jobDAO.deleteTimersByName(timerName, executionContext.getToken());
    }

    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }
}
