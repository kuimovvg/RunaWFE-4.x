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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.af.AuthorizationService;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wf.logic.bot.startprocess.StartProcessTask;
import ru.runa.wf.logic.bot.startprocess.StartProcessVariableMapping;
import ru.runa.wf.logic.bot.startprocess.StartProcessXmlParser;
import ru.runa.wfe.commons.sqltask.DatabaseTask;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.handler.bot.TaskHandler;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.collect.Maps;

/**
 * @author Martin Gaido
 * @author Alexander Shevtsou 13.11.20008 - started process id property added.
 * 
 *         added 9.06.2009 by gavrusev_sergei from version 2
 */
public class StartProcessTaskHandler implements TaskHandler {

    private static final Log log = LogFactory.getLog(StartProcessTaskHandler.class);

    private StartProcessTask[] startProcessTasks;

    @Override
    public void setConfiguration(byte[] configuration) {
        startProcessTasks = StartProcessXmlParser.parse(new ByteArrayInputStream(configuration));
    }

    @Override
    public Map<String, Object> handle(Subject subject, IVariableProvider variableProvider, WfTask wfTask) {
        ExecutionService executionService = DelegateFactory.getExecutionService();
        Map<String, Object> outputVariables = Maps.newHashMap();

        Map<String, Object> variablesMap = Maps.newHashMap();
        for (int i = 0; i < startProcessTasks.length; i++) {
            Long startedProcessId = null;
            StartProcessTask startProcessTask = startProcessTasks[i];
            String processName = startProcessTask.getName();
            String startedProcessValueName = startProcessTask.getStartedProcessIdValueName();
            for (int j = 0; j < startProcessTask.getVariablesCount(); j++) {
                StartProcessVariableMapping startProcessVariableMapping = startProcessTask.getStartProcessVariableMapping(j);
                String from = startProcessVariableMapping.getFromName();
                String to = startProcessVariableMapping.getToName();
                Object value = variableProvider.getValue(from);
                if (DatabaseTask.INSTANCE_ID_VARIABLE_NAME.equals(from)) {
                    value = wfTask.getProcessId();
                }
                if (DatabaseTask.CURRENT_DATE_VARIABLE_NAME.equals(from)) {
                    value = new Date();
                }
                variablesMap.put(to, value);
            }

            // Start process
            startedProcessId = executionService.startProcess(subject, processName, variablesMap);

            // add startedProcessId to variables
            if (startedProcessValueName != null) {
                outputVariables.put(startedProcessValueName, startedProcessId);
            }

            try {
                AuthorizationService authorizationService = ru.runa.service.delegate.DelegateFactory.getAuthorizationService();
                WfProcess wfProcess = executionService.getProcess(subject, startedProcessId);
                WfProcess superWfProcess = executionService.getProcess(subject, wfTask.getProcessId());
                BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
                List<Executor> executors = authorizationService.getExecutorsWithPermission(subject, superWfProcess, batchPresentation, true);
                for (Executor executor : executors) {
                    Set<Permission> permissions = new HashSet<Permission>();
                    for (Permission perm : authorizationService.getOwnPermissions(subject, executor, superWfProcess)) {
                        permissions.add(perm);
                    }
                    for (Permission perm : authorizationService.getOwnPermissions(subject, executor, wfProcess)) {
                        permissions.add(perm);
                    }
                    authorizationService.setPermissions(subject, executor, permissions, wfProcess);
                }
            } catch (Throwable e) {
                log.error("Error on permission copy to new subprocess (step is ignored).", e);
            }
        }
        return outputVariables;
    }
}
