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
package ru.runa.wf.jbpm.delegation.action;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.ConfigurationException;
import ru.runa.af.Executor;
import ru.runa.af.Permission;
import ru.runa.af.SecuredObject;
import ru.runa.af.dao.PermissionDAO;
import ru.runa.af.dao.SecuredObjectDAO;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.bpm.graph.def.ActionHandler;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.dao.TmpDAO;
import ru.runa.wf.logic.InstancePermissionsHelper;

public class SetSubProcessPermissionsActionHandler implements ActionHandler {
    private static final long serialVersionUID = 1L;
    @Autowired
    private PermissionDAO permissionDAO;
    @Autowired
    private SecuredObjectDAO securedObjectDAO;
    @Autowired
    private TmpDAO tmpDAO;

    @Override
    public void setConfiguration(String configuration) throws ConfigurationException {
    }

    @Override
    public void execute(ExecutionContext context) throws Exception {
        ProcessInstance parentInstance = context.getToken().getProcessInstance();
        ProcessInstance pInstance = context.getToken().getSubProcessInstance();
        tmpDAO.save(pInstance);
        ProcessInstanceStub instanceStub = new ProcessInstanceStub(pInstance);
        ProcessInstanceStub parentInstanceStub = new ProcessInstanceStub(parentInstance);
        ProcessDefinition definitionStub = new ProcessDefinition(context.getProcessDefinition());
        SecuredObject instanceSO = securedObjectDAO.create(instanceStub);
        List<Executor> executorsWithPermission = securedObjectDAO.getExecutorsWithPermission(definitionStub,
                AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation());
        List<Executor> executorsWithParentPermission = securedObjectDAO.getExecutorsWithPermission(parentInstanceStub,
                AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation());
        Set<Executor> executors = new HashSet<Executor>();
        executors.addAll(executorsWithPermission);
        executors.addAll(executorsWithParentPermission);
        SecuredObject definitionSecuredObject = securedObjectDAO.get(definitionStub);
        SecuredObject parentSecuredObject = securedObjectDAO.get(parentInstanceStub);
        for (Executor executor : executors) {
            List<Permission> permissions = permissionDAO.getOwnPermissions(executor, parentSecuredObject);
            Set<Permission> permissionByDefinition = InstancePermissionsHelper.getInstancePermissions(executor, definitionSecuredObject,
                    permissionDAO);
            permissionDAO.setPermissions(executor, Permission.mergePermissions(permissions, permissionByDefinition), instanceSO);
        }
        Set<Executor> privelegedExecutors = securedObjectDAO.getPrivilegedExecutors(instanceStub);
        List<Permission> p = securedObjectDAO.getNoPermission(instanceStub).getAllPermissions();
        SecuredObject so = securedObjectDAO.get(instanceStub);
        permissionDAO.setPermissions(privelegedExecutors, p, so);
    }
}
