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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wf.logic.bot.mswordreport.MSWordReportBuilder;
import ru.runa.wf.logic.bot.mswordreport.MSWordReportBuilderFactory;
import ru.runa.wf.logic.bot.mswordreport.MSWordReportTaskSettings;
import ru.runa.wf.logic.bot.mswordreport.WordReportSettingsXmlParser;
import ru.runa.wfe.handler.bot.TaskHandlerBase;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

/**
 * 
 * Reads template word document. Replaces all bookmarks by rules provided in
 * configuration.
 * 
 * Created on 23.11.2006
 * 
 */
public class MSWordReportTaskHandler extends TaskHandlerBase {
    private static final Log log = LogFactory.getLog(MSWordReportTaskHandler.class);
    private static final String CONTENT_TYPE = "application/vnd.ms-word";

    private MSWordReportTaskSettings settings;

    @Override
    public void setConfiguration(String configuration) {
        settings = WordReportSettingsXmlParser.read(configuration);
    }

    @Override
    public synchronized Map<String, Object> handle(Subject subject, IVariableProvider variableProvider, WfTask task) throws IOException {
        File reportTemporaryFile = null;
        try {
            reportTemporaryFile = File.createTempFile("prefix", ".doc");
            MSWordReportBuilder wordReportBuilder = MSWordReportBuilderFactory.createMSWordReportBuilder();
            log.debug("Using template " + settings.getTemplateFileLocation());
            wordReportBuilder.build(reportTemporaryFile.getAbsolutePath(), variableProvider, settings);
            FileInputStream fis = new FileInputStream(reportTemporaryFile);
            byte[] fileContent = ByteStreams.toByteArray(fis);
            fis.close();
            FileVariable fileVariable = new FileVariable(settings.getReportFileName(), fileContent, CONTENT_TYPE);
            Map<String, Object> result = Maps.newHashMapWithExpectedSize(1);
            result.put(settings.getReportVariableName(), fileVariable);
            return result;
        } finally {
            if (reportTemporaryFile != null) {
                if (!reportTemporaryFile.delete()) {
                    log.warn("Unable to delete " + reportTemporaryFile.getAbsolutePath());
                }
            }
        }
    }
}
