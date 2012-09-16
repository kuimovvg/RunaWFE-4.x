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
package ru.runa.wf.logic;

import java.util.List;

import ru.runa.af.ASystem;
import ru.runa.af.BotStation;
import ru.runa.af.BotStationConfigurePermission;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.SecuredObject;
import ru.runa.af.SecuredObjectAlreadyExistsException;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.logic.AFInitializerLogic;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessInstancePermission;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.WorkflowSystemPermission;

import com.google.common.collect.Lists;

/**
 * Created on 23.09.2004
 */
public class WFInitializerLogic extends AFInitializerLogic {
    protected SecuredObject botStationSecuredObject;

    @Override
    protected void defineSystemPermissions() throws ExecutorOutOfDateException {
        securedObjectDAO.addType(ASystem.class, Lists.newArrayList(adminGroup), WorkflowSystemPermission.class);
    }

    @Override
    protected void defineAdditionalTypesPermissions() throws ExecutorOutOfDateException, SecuredObjectAlreadyExistsException {
        super.defineAdditionalTypesPermissions();
        botStationSecuredObject = securedObjectDAO.create(BotStation.SECURED_INSTANCE);
        securedObjectDAO.addType(BotStation.class, Lists.newArrayList(adminGroup, admin), BotStationConfigurePermission.class);
        List<? extends Executor> privilegedExecutor = Lists.newArrayList(adminGroup);
        securedObjectDAO.addType(ProcessDefinition.class, privilegedExecutor, ProcessDefinitionPermission.class);
        securedObjectDAO.addType(ProcessInstanceStub.class, privilegedExecutor, ProcessInstancePermission.class);
    }

    @Override
    protected void grantDefaultExecutorsPermissions() throws ExecutorOutOfDateException, SecuredObjectOutOfDateException,
            UnapplicablePermissionException {
        super.grantDefaultExecutorsPermissions();
        permissionDAO.setPermissions(Lists.newArrayList(adminGroup, admin), new BotStationConfigurePermission().getAllPermissions(),
                botStationSecuredObject);
    }

    @Override
    protected void grantDefaultSystemPermissions() throws ExecutorOutOfDateException, SecuredObjectOutOfDateException,
            UnapplicablePermissionException {
        super.grantDefaultSystemPermissions();
        permissionDAO.setPermissions(adminGroup, new WorkflowSystemPermission().getAllPermissions(), aaSystemSecuredObject);
    }

}
