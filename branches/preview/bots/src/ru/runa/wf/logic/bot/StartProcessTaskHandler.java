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
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.af.Executor;
import ru.runa.af.Permission;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.service.AuthorizationService;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.TaskStub;
import ru.runa.wf.logic.bot.startprocess.StartProcessTask;
import ru.runa.wf.logic.bot.startprocess.StartProcessTaskXmlParserException;
import ru.runa.wf.logic.bot.startprocess.StartProcessXmlParser;
import ru.runa.wf.logic.bot.startprocess.Variable;
import ru.runa.wf.service.ExecutionService;

import com.google.common.collect.Maps;

/**
 * Created on 26.05.2006
 * 
 * @author Martin Gaido
 * @author Alexander Shevtsou 13.11.20008 - started process id property added.
 * 
 *         added 9.06.2009 by gavrusev_sergei from version 2
 */

public class StartProcessTaskHandler implements TaskHandler {

    private static final Log log = LogFactory.getLog(StartProcessTaskHandler.class);

    private final static String INSTANCE_ID_VARIABLE_NAME = "instanceId";

    private final static String CURRENT_DATE_VARIABLE_NAME = "currentDate";

    private StartProcessTask[] startProcessTasks;

    @Override
    public void configure(String configurationName) throws TaskHandlerException {
        try {
            URL configurationURL = StartProcessTaskHandler.class.getResource(configurationName);
            if (configurationURL == null) {
                throw new TaskHandlerException("Unable to find configuration " + configurationName);
            }
            startProcessTasks = StartProcessXmlParser.parse(configurationURL.toString());
        } catch (StartProcessTaskXmlParserException e) {
            throw new TaskHandlerException(e);
        }
    }

    @Override
    public void configure(byte[] configuration) throws TaskHandlerException {
        try {
            startProcessTasks = StartProcessXmlParser.parse(new ByteArrayInputStream(configuration));
        } catch (StartProcessTaskXmlParserException e) {
            throw new TaskHandlerException(e);
        }
    }

    @Override
    public void handle(Subject subject, TaskStub taskStub) throws TaskHandlerException {
        try {
            log.info("StartProcessTask started, task " + taskStub);
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            Map<String, Object> variables = executionService.getVariables(subject, taskStub.getId());
            variables.put(INSTANCE_ID_VARIABLE_NAME, new Long(taskStub.getProcessInstanceId()));
            variables.put(CURRENT_DATE_VARIABLE_NAME, new Date());

            Map<String, Object> variablesMap = Maps.newHashMap();
            for (int i = 0; i < startProcessTasks.length; i++) {
                Long startedProcessId = null;
                StartProcessTask startProcessTask = startProcessTasks[i];
                String processName = startProcessTask.getName();
                String startedProcessValueName = startProcessTask.getStartedProcessIdValueName();
                for (int j = 0; j < startProcessTask.getVariablesCount(); j++) {
                    Variable variable = startProcessTask.getVariable(j);
                    String from = variable.getFromName();
                    String to = variable.getToName();
                    variablesMap.put(to, variables.get(from));
                }

                // Start process
                startedProcessId = executionService.startProcessInstance(subject, processName, variablesMap);

                // add startedProcessId to variables
                if (startedProcessValueName != null) {
                    variables.put(startedProcessValueName, startedProcessId);
                }

                try {
                    AuthorizationService authorizationService = ru.runa.delegate.DelegateFactory.getInstance().getAuthorizationService();
                    ProcessInstanceStub subprocessStub = executionService.getProcessInstanceStub(subject, startedProcessId);
                    ProcessInstanceStub superprocessStub = executionService.getProcessInstanceStub(subject, taskStub.getProcessInstanceId());
                    BatchPresentation batchPresentation = AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY
                            .getDefaultBatchPresentation();
                    batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
                    List<Executor> executors = authorizationService.getExecutorsWithPermission(subject, superprocessStub, batchPresentation, true);
                    for (Executor executor : executors) {
                        Set<Permission> permissions = new HashSet<Permission>();
                        for (Permission perm : authorizationService.getOwnPermissions(subject, executor, superprocessStub)) {
                            permissions.add(perm);
                        }
                        for (Permission perm : authorizationService.getOwnPermissions(subject, executor, subprocessStub)) {
                            permissions.add(perm);
                        }
                        authorizationService.setPermissions(subject, executor, permissions, subprocessStub);
                    }
                } catch (Throwable e) {
                    log.warn("Error on permission copy to new subprocess.", e);
                }
            }
            variables.remove(INSTANCE_ID_VARIABLE_NAME);
            variables.remove(CURRENT_DATE_VARIABLE_NAME);
            executionService.completeTask(subject, taskStub.getId(), taskStub.getName(), taskStub.getTargetActor().getId(), variables);
            log.info("DatabaseTask finished, task " + taskStub);
        } catch (ProcessDefinitionDoesNotExistException e) {
            log.error("StartProcessTask failed, task " + taskStub, e);
        } catch (Exception e) {
            log.error("StartProcessTask failed, task " + taskStub, e);
            throw new TaskHandlerException(e);
        }
    }
}
