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

import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.TaskStub;
import ru.runa.wf.logic.bot.cancelprocess.CancelProcessTask;
import ru.runa.wf.logic.bot.cancelprocess.CancelProcessTaskXmlParser;
import ru.runa.wf.logic.bot.cancelprocess.CancelProcessTaskXmlParserException;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.ExecutionService;

/**
 * Created on 18.04.2005
 * 
 */
public class CancelProcessTaskHandler implements TaskHandler {
    private static final Log log = LogFactory.getLog(CancelProcessTaskHandler.class);

    private CancelProcessTask processToCancelTask;

    public void configure(String configurationPath) throws TaskHandlerException {
        try {
            processToCancelTask = CancelProcessTaskXmlParser.parse(this.getClass().getResource(configurationPath).toString());
        } catch (CancelProcessTaskXmlParserException e) {
            throw new TaskHandlerException(e);
        }
    }

    public void configure(byte[] configuration) throws TaskHandlerException {
        try {
            processToCancelTask = CancelProcessTaskXmlParser.parse(new ByteArrayInputStream(configuration));
        } catch (CancelProcessTaskXmlParserException e) {
            throw new TaskHandlerException(e);
        }
    }

    public void handle(Subject subject, TaskStub taskStub) throws TaskHandlerException {
        try {
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            Map<String, Object> variables = executionService.getVariables(subject, taskStub.getId());
            Object processInstanceIdVar = variables.get(processToCancelTask.getProcessIdVariableName());
            if (processInstanceIdVar == null) {
                executionService.completeTask(subject, taskStub.getId(), taskStub.getName(), taskStub.getTargetActor().getId(), variables);
                return;
            }
            Long instanceId;
            if (processInstanceIdVar instanceof String) {
                instanceId = Long.parseLong((String) processInstanceIdVar);
            } else if (processInstanceIdVar instanceof Number) {
                instanceId = ((Number) processInstanceIdVar).longValue();
            } else {
                throw new TaskHandlerException(processInstanceIdVar + " is not instance of Long or String Class.");
            }
            if (instanceId != 0) {
                executionService.cancelProcessInstance(subject, instanceId);
                DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
                ProcessDefinition definitionStub = definitionService.getProcessDefinitionStubByProcessId(subject, instanceId);
                String processDefinitionName = definitionStub.getName();
                Object configurationNameObject = processToCancelTask.getDatabaseTaskMap().get(processDefinitionName);
                if (configurationNameObject == null) {
                    throw new TaskHandlerException("Record for '" + processDefinitionName + " missed in task handler configuration");
                }
                String configurationName = (String) configurationNameObject;
                DatabaseTaskHandler databaseTaskHandler = new DatabaseTaskHandler();
                databaseTaskHandler.configure(configurationName);
                databaseTaskHandler.handle(subject, taskStub);
            } else {
                log.warn("Process ID = 0");
                executionService.completeTask(subject, taskStub.getId(), taskStub.getName(), taskStub.getTargetActor().getId(), variables);
            }
            log.info("CancelProcessTask completed, task name: " + taskStub.getName());
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }
}
