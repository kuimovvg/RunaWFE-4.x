/*
 * This file is part of the RUNA WFE project.
 * Copyright (C) 2004-2006, Joint stock company "RUNA Technology"
 * All rights reserved.
 * 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ru.runa.wf.logic.bot;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wf.logic.bot.textreport.TextReportGenerator;
import ru.runa.wf.logic.bot.textreport.TextReportSettings;
import ru.runa.wf.logic.bot.textreport.TextReportSettingsXmlParser;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.IVariableProvider;

/**
 * @since 10.08.2006
 */
public class TextReportTaskHandler implements TaskHandler {
    private static final Log log = LogFactory.getLog(TextReportTaskHandler.class);

    private TextReportSettings settings;

    @Override
    public void configure(String configurationName) throws TaskHandlerException {
        settings = TextReportSettingsXmlParser.read(ClassLoaderUtil.getResourceAsStream(configurationName, getClass()));
    }

    @Override
    public void handle(Subject subject, IVariableProvider variableProvider, WfTask wfTask) throws TaskHandlerException {
        try {
            byte[] fileContent = TextReportGenerator.getReportContent(settings, variableProvider);
            Map<String, Object> vars = new HashMap<String, Object>();
            FileVariable fileVariable = new FileVariable(settings.getReportFileName(), fileContent, settings.getReportContentType());
            vars.put(settings.getReportVariableName(), fileVariable);
            DelegateFactory.getExecutionService().completeTask(subject, wfTask.getId(), vars);
            log.info("TextReportTaskHandler completed, task " + wfTask);
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }

    @Override
    public void configure(byte[] configuration) throws TaskHandlerException {
        settings = TextReportSettingsXmlParser.read(new ByteArrayInputStream(configuration));
    }
}
