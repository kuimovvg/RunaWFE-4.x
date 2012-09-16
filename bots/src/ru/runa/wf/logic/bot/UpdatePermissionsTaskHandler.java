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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.InternalApplicationException;
import ru.runa.af.Executor;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.organizationfunction.FunctionParserException;
import ru.runa.af.organizationfunction.OrgFunctionHelper;
import ru.runa.af.organizationfunction.OrganizationFunctionException;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.ExecutorService;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskStub;
import ru.runa.wf.logic.bot.updatepermission.UpdatePermissionsSettings;
import ru.runa.wf.logic.bot.updatepermission.UpdatePermissionsXmlParser;
import ru.runa.wf.service.ExecutionService;

import com.google.common.collect.Lists;

/**
 * Created on 18.05.2006
 * 
 */
public class UpdatePermissionsTaskHandler extends AbstractOrgFunctionTaskHandler {
    private static final Log log = LogFactory.getLog(UpdatePermissionsTaskHandler.class);

    private UpdatePermissionsSettings settings;

    public void configure(String configurationName) throws TaskHandlerException {
        settings = UpdatePermissionsXmlParser.read(getClass().getResourceAsStream(configurationName));
    }

    public void configure(byte[] configuration) throws TaskHandlerException {
        settings = UpdatePermissionsXmlParser.read(new ByteArrayInputStream(configuration));
    }

    public void handle(Subject subject, TaskStub taskStub) throws TaskHandlerException {
        try {
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();

            Map<String, Object> variables = executionService.getVariables(subject, taskStub.getId());

            boolean allowed = true;
            if (settings.isConditionExists()) {
                String conditionVar = (String) variables.get(settings.getConditionVarName());
                if (!settings.getConditionVarValue().equals(conditionVar)) {
                    allowed = false;
                }
            }

            if (allowed) {
                List<Long> executorIds = evaluateOrgFunctions(variables, settings.getOrgFunctions(), getActorToSubstituteCode(subject));

                AuthorizationService authorizationService = ru.runa.delegate.DelegateFactory.getInstance()
                        .getAuthorizationService();
                ExecutorService executorService = ru.runa.delegate.DelegateFactory.getInstance().getExecutorService();

                List<Collection<Permission>> allPermissions = Lists.newArrayListWithExpectedSize(executorIds.size());
                Identifiable identifiable = DelegateFactory.getInstance().getExecutionService().getProcessInstanceStub(
                        subject, taskStub.getProcessInstanceId());
                String method = settings.getMethod();
                List<Executor> executors = executorService.getExecutors(subject, executorIds);
                for (Executor executor : executors) {
                    Collection<Permission> oldPermissions = authorizationService.getPermissions(subject, executor, identifiable);
                    allPermissions.add(getNewPermissions(oldPermissions, settings.getPermissions(), method));
                }

                authorizationService.setPermissions(subject, executorIds, allPermissions, identifiable);
            }
            executionService.completeTask(subject, taskStub.getId(), taskStub.getName(), taskStub.getTargetActor().getId(), new HashMap<String, Object>());
            log.debug("UpdatePermissionsTaskHandler finished task " + taskStub);
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }

    private List<Long> evaluateOrgFunctions(Map<String, Object> variablesMap, String[] orgFunctions, Long actorToSubstituteCode)
            throws FunctionParserException, OrganizationFunctionException {
        List<Long> ids = Lists.newArrayList();
        for (int i = 0; i < orgFunctions.length; i++) {
            List<Long> executorIds = OrgFunctionHelper.evaluateOrgFunction(variablesMap, orgFunctions[i], actorToSubstituteCode);
            ids.addAll(executorIds);
        }

        return ids;
    }

    private Collection<Permission> getNewPermissions(Collection<Permission> oldPermissions, Collection<Permission> permissions, String method) {
        if (UpdatePermissionsSettings.METHOD_ADD_NAME.equals(method)) {
            return Permission.mergePermissions(oldPermissions, permissions);
        } else if (UpdatePermissionsSettings.METHOD_SET_NAME.equals(method)) {
            return permissions;
        } else if (UpdatePermissionsSettings.METHOD_DELETE_NAME.equals(method)) {
            return Permission.subtractPermissions(oldPermissions, permissions);
        } else {
            // should never happend
            throw new InternalApplicationException("Unknown method provided: " + method);
        }
    }
}
