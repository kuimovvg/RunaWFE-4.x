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
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.DefinitionService;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wf.logic.bot.cancelprocess.CancelProcessTask;
import ru.runa.wf.logic.bot.cancelprocess.CancelProcessTaskXmlParser;
import ru.runa.wf.logic.bot.cancelprocess.CancelProcessTaskXmlParserException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.collect.Maps;

/**
 * Created on 18.04.2005
 * 
 */
public class CancelProcessTaskHandler implements TaskHandler {
    private static final Log log = LogFactory.getLog(CancelProcessTaskHandler.class);

    private CancelProcessTask processToCancelTask;

    @Override
    public void configure(String configurationPath) throws TaskHandlerException {
        try {
            processToCancelTask = CancelProcessTaskXmlParser.parse(this.getClass().getResource(configurationPath).toString());
        } catch (CancelProcessTaskXmlParserException e) {
            throw new TaskHandlerException(e);
        }
    }

    @Override
    public void configure(byte[] configuration) throws TaskHandlerException {
        try {
            processToCancelTask = CancelProcessTaskXmlParser.parse(new ByteArrayInputStream(configuration));
        } catch (CancelProcessTaskXmlParserException e) {
            throw new TaskHandlerException(e);
        }
    }

    @Override
    public void handle(Subject subject, IVariableProvider variableProvider, WfTask wfTask) throws TaskHandlerException {
        try {
            ExecutionService executionService = DelegateFactory.getExecutionService();
            Map<String, Object> outputVariables = Maps.newHashMap();
            Long processId = variableProvider.get(Long.class, processToCancelTask.getProcessIdVariableName());
            if (processId == null) {
                executionService.completeTask(subject, wfTask.getId(), outputVariables);
                return;
            }
            if (processId != 0) {
                executionService.cancelProcess(subject, processId);
                DefinitionService definitionService = DelegateFactory.getDefinitionService();
                WfDefinition definitionStub = definitionService.getProcessDefinitionByProcessId(subject, processId);
                String processDefinitionName = definitionStub.getName();
                Object configurationNameObject = processToCancelTask.getDatabaseTaskMap().get(processDefinitionName);
                if (configurationNameObject == null) {
                    throw new TaskHandlerException("Record for '" + processDefinitionName + " missed in task handler configuration");
                }
                String configurationName = (String) configurationNameObject;
                DatabaseTaskHandler databaseTaskHandler = new DatabaseTaskHandler();
                databaseTaskHandler.configure(configurationName);
                databaseTaskHandler.handle(subject, variableProvider, wfTask);
            } else {
                log.warn("Process ID = 0");
                executionService.completeTask(subject, wfTask.getId(), outputVariables);
            }
            log.info("CancelProcessTask completed, task name: " + wfTask.getName());
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }
}
