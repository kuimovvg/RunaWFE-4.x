/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.logic.bot;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wf.logic.bot.mswordreport.MSWordReportBuilder;
import ru.runa.wf.logic.bot.mswordreport.MSWordReportBuilderFactory;
import ru.runa.wf.logic.bot.mswordreport.MSWordReportTaskSettings;
import ru.runa.wf.logic.bot.mswordreport.WordReportSettingsXmlParser;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.io.ByteStreams;

/**
 * 
 * Reads template word document. Replaces all bookmarks by rules provided in configuration.
 * 
 * Created on 23.11.2006
 * 
 */

public class MSWordReportTaskHandler implements TaskHandler {
    private static final Log log = LogFactory.getLog(MSWordReportTaskHandler.class);
    private static final String CONTENT_TYPE = "application/vnd.ms-word";

    private MSWordReportTaskSettings settings;

    @Override
    public void configure(String configurationPath) throws TaskHandlerException {
        settings = WordReportSettingsXmlParser.read(configurationPath);
    }

    @Override
    public void configure(byte[] configuration) throws TaskHandlerException {
        settings = WordReportSettingsXmlParser.read(new ByteArrayInputStream(configuration));
    }

    @Override
    public synchronized void handle(Subject subject, IVariableProvider variableProvider, WfTask wfTask) throws TaskHandlerException {
        File reportTemporaryFile = null;
        try {
            log.info("Starting task " + wfTask.getName() + " in process " + wfTask.getProcessId());
            reportTemporaryFile = File.createTempFile("prefix", ".doc");
            MSWordReportBuilder wordReportBuilder = MSWordReportBuilderFactory.createMSWordReportBuilder();
            log.debug("Using template " + settings.getTemplateFileLocation());
            wordReportBuilder.build(reportTemporaryFile.getAbsolutePath(), variableProvider, settings);
            FileInputStream fis = new FileInputStream(reportTemporaryFile);
            byte[] fileContent = ByteStreams.toByteArray(fis);
            fis.close();
            Map<String, Object> taskVariables = new HashMap<String, Object>();
            FileVariable fileVariable = new FileVariable(settings.getReportFileName(), fileContent, CONTENT_TYPE);
            taskVariables.put(settings.getReportVariableName(), fileVariable);
            DelegateFactory.getExecutionService().completeTask(subject, wfTask.getId(), taskVariables);
            log.info("Ended task " + wfTask.getName() + " in process " + wfTask.getProcessId());
        } catch (Exception e) {
            log.error("", e);
            throw new TaskHandlerException(e);
        } finally {
            if (reportTemporaryFile != null) {
                if (!reportTemporaryFile.delete()) {
                    log.warn("Unable to delete " + reportTemporaryFile.getAbsolutePath());
                }
            }
        }
    }
}
