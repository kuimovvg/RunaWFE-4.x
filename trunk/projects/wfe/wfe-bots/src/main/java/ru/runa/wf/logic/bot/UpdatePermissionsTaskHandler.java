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

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.af.AuthorizationService;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wf.logic.bot.updatepermission.UpdatePermissionsSettings;
import ru.runa.wf.logic.bot.updatepermission.UpdatePermissionsXmlParser;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.os.OrgFunctionException;
import ru.runa.wfe.os.OrgFunctionHelper;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.collect.Lists;

/**
 * Created on 18.05.2006
 * 
 */
public class UpdatePermissionsTaskHandler extends AbstractOrgFunctionTaskHandler {
    private static final Log log = LogFactory.getLog(UpdatePermissionsTaskHandler.class);

    private UpdatePermissionsSettings settings;

    @Override
    public void configure(String configurationName) throws TaskHandlerException {
        settings = UpdatePermissionsXmlParser.read(getClass().getResourceAsStream(configurationName));
    }

    @Override
    public void configure(byte[] configuration) throws TaskHandlerException {
        settings = UpdatePermissionsXmlParser.read(new ByteArrayInputStream(configuration));
    }

    @Override
    public void handle(Subject subject, IVariableProvider variableProvider, WfTask wfTask) throws TaskHandlerException {
        try {
            ExecutionService executionService = DelegateFactory.getExecutionService();
            boolean allowed = true;
            if (settings.isConditionExists()) {
                String conditionVar = variableProvider.get(String.class, settings.getConditionVarName());
                if (!settings.getConditionVarValue().equals(conditionVar)) {
                    allowed = false;
                }
            }
            if (allowed) {
                List<? extends Executor> executors = evaluateOrgFunctions(variableProvider, settings.getOrgFunctions(),
                        getActorToSubstituteCode(subject));
                AuthorizationService authorizationService = ru.runa.service.delegate.DelegateFactory.getAuthorizationService();
                List<Collection<Permission>> allPermissions = Lists.newArrayListWithExpectedSize(executors.size());
                Identifiable identifiable = DelegateFactory.getExecutionService().getProcess(subject, wfTask.getProcessId());
                String method = settings.getMethod();
                List<Long> executorIds = Lists.newArrayList();
                for (Executor executor : executors) {
                    Collection<Permission> oldPermissions = authorizationService.getPermissions(subject, executor, identifiable);
                    allPermissions.add(getNewPermissions(oldPermissions, settings.getPermissions(), method));
                    executorIds.add(executor.getId());
                }
                authorizationService.setPermissions(subject, executorIds, allPermissions, identifiable);
            }
            executionService.completeTask(subject, wfTask.getId(), new HashMap<String, Object>());
            log.debug("UpdatePermissionsTaskHandler finished task " + wfTask);
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }

    private List<? extends Executor> evaluateOrgFunctions(IVariableProvider variableProvider, String[] orgFunctions, Long actorToSubstituteCode)
            throws OrgFunctionException {
        List<Executor> executors = Lists.newArrayList();
        for (int i = 0; i < orgFunctions.length; i++) {
            executors.addAll(OrgFunctionHelper.evaluateOrgFunction(variableProvider, orgFunctions[i], actorToSubstituteCode));
        }
        return executors;
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
