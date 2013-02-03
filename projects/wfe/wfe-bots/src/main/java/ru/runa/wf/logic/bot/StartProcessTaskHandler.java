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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.af.AuthorizationService;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wf.logic.bot.startprocess.StartProcessTask;
import ru.runa.wf.logic.bot.startprocess.StartProcessVariableMapping;
import ru.runa.wf.logic.bot.startprocess.StartProcessXmlParser;
import ru.runa.wfe.commons.sqltask.DatabaseTask;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.handler.bot.TaskHandlerBase;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.collect.Maps;

/**
 * @author Martin Gaido
 * @author Alexander Shevtsou 13.11.20008 - started process id property added.
 * 
 *         added 9.06.2009 by gavrusev_sergei from version 2
 */
public class StartProcessTaskHandler extends TaskHandlerBase {
    private static final Log log = LogFactory.getLog(StartProcessTaskHandler.class);

    private List<StartProcessTask> startProcessTasks;

    @Override
    public void setConfiguration(String configuration) {
        startProcessTasks = StartProcessXmlParser.parse(configuration);
    }

    @Override
    public Map<String, Object> handle(User user, IVariableProvider variableProvider, WfTask task) {
        ExecutionService executionService = Delegates.getExecutionService();
        Map<String, Object> outputVariables = Maps.newHashMap();

        Map<String, Object> variablesMap = Maps.newHashMap();
        for (StartProcessTask startProcessTask : startProcessTasks) {
            String processName = startProcessTask.getName();
            String startedProcessValueName = startProcessTask.getStartedProcessIdValueName();
            for (int j = 0; j < startProcessTask.getVariablesCount(); j++) {
                StartProcessVariableMapping startProcessVariableMapping = startProcessTask.getStartProcessVariableMapping(j);
                String from = startProcessVariableMapping.getFromName();
                String to = startProcessVariableMapping.getToName();
                Object value = variableProvider.getValue(from);
                if (DatabaseTask.INSTANCE_ID_VARIABLE_NAME.equals(from)) {
                    value = task.getProcessId();
                }
                if (DatabaseTask.CURRENT_DATE_VARIABLE_NAME.equals(from)) {
                    value = new Date();
                }
                variablesMap.put(to, value);
            }

            // Start process
            Long startedProcessId = executionService.startProcess(user, processName, variablesMap);

            // add startedProcessId to variables
            if (startedProcessValueName != null) {
                outputVariables.put(startedProcessValueName, startedProcessId);
            }

            try {
                AuthorizationService authorizationService = ru.runa.service.delegate.Delegates.getAuthorizationService();
                WfProcess process = executionService.getProcess(user, startedProcessId);
                WfProcess parentProcess = executionService.getProcess(user, task.getProcessId());
                BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
                List<Executor> executors = authorizationService.getExecutorsWithPermission(user, parentProcess, batchPresentation, true);
                for (Executor executor : executors) {
                    Set<Permission> permissions = new HashSet<Permission>();
                    for (Permission permission : authorizationService.getOwnPermissions(user, executor, parentProcess).keySet()) {
                        permissions.add(permission);
                    }
                    for (Permission permission : authorizationService.getOwnPermissions(user, executor, process).keySet()) {
                        permissions.add(permission);
                    }
                    authorizationService.setPermissions(user, executor, permissions, process);
                }
            } catch (Throwable th) {
                log.error("Error in permission copy to new subprocess (step is ignored).", th);
            }
        }
        return outputVariables;
    }
}
