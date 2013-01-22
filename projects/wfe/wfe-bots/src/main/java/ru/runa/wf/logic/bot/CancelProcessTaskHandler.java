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
import java.io.InputStream;
import java.util.Map;

import javax.security.auth.Subject;

import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.DefinitionService;
import ru.runa.wf.logic.bot.cancelprocess.CancelProcessTask;
import ru.runa.wf.logic.bot.cancelprocess.CancelProcessTaskXmlParser;
import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.handler.bot.TaskHandler;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.io.ByteStreams;

/**
 * Cancels process by id and executes arbitrary SQL.
 * 
 * @author dofs
 * @since 3.0
 */
public class CancelProcessTaskHandler implements TaskHandler {
    private CancelProcessTask processToCancelTask;

    @Override
    public void setConfiguration(byte[] configuration) throws Exception {
        processToCancelTask = CancelProcessTaskXmlParser.parse(new ByteArrayInputStream(configuration));
    }

    @Override
    public Object getConfiguration() {
        return processToCancelTask;
    }

    @Override
    public Map<String, Object> handle(Subject subject, IVariableProvider variableProvider, WfTask wfTask) throws Exception {
        Long processId = variableProvider.getValue(Long.class, processToCancelTask.getProcessIdVariableName());
        if (processId != null && processId != 0) {
            Delegates.getExecutionService().cancelProcess(subject, processId);
            DefinitionService definitionService = Delegates.getDefinitionService();
            WfDefinition definitionStub = definitionService.getProcessDefinitionByProcessId(subject, processId);
            String processDefinitionName = definitionStub.getName();
            String configurationName = processToCancelTask.getDatabaseTaskMap().get(processDefinitionName);
            if (configurationName == null) {
                throw new ConfigurationException("Record for '" + processDefinitionName + " missed in task handler configuration");
            }
            InputStream inputStream = ClassLoaderUtil.getResourceAsStream(configurationName, DatabaseTaskHandler.class);
            if (inputStream == null) {
                throw new ConfigurationException("Unable to find configuration " + configurationName);
            }
            byte[] configuration = ByteStreams.toByteArray(inputStream);
            DatabaseTaskHandler databaseTaskHandler = new DatabaseTaskHandler();
            databaseTaskHandler.setConfiguration(configuration);
            databaseTaskHandler.handle(subject, variableProvider, wfTask);
        }
        return null;
    }
}
