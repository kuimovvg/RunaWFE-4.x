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
package ru.runa.af.logic;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import ru.runa.InternalApplicationException;
import ru.runa.af.ASystem;
import ru.runa.af.Actor;
import ru.runa.af.ActorPermission;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.ExecutorAlreadyInGroupException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.GroupPermission;
import ru.runa.af.Relation;
import ru.runa.af.RelationPermission;
import ru.runa.af.RelationsGroupSecure;
import ru.runa.af.SecuredObject;
import ru.runa.af.SecuredObjectAlreadyExistsException;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.SystemExecutors;
import ru.runa.af.SystemPermission;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.WeakPasswordException;

import com.google.common.collect.Lists;

/**
 * Created on 22.03.2005
 */
public class AFInitializerLogic extends InitializerLogic {

    protected Actor admin;

    protected Group adminGroup;

    protected SecuredObject aaSystemSecuredObject;

    protected SecuredObject relationsGroupSecuredObject;

    private String administratorName;
    private String administratorDescription;
    private String administratorPassword;
    private String administratorGroupName;
    private String administratorGroupDescription;

    @Required
    public void setAdministratorName(String administratorName) {
        this.administratorName = administratorName;
    }

    @Required
    public void setAdministratorDescription(String administratorDescription) {
        this.administratorDescription = administratorDescription;
    }

    @Required
    public void setAdministratorPassword(String administratorPassword) {
        this.administratorPassword = administratorPassword;
    }

    @Required
    public void setAdministratorGroupName(String administratorGroupName) {
        this.administratorGroupName = administratorGroupName;
    }

    @Required
    public void setAdministratorGroupDescription(String administratorGroupDescription) {
        this.administratorGroupDescription = administratorGroupDescription;
    }

    @Override
    protected void createTablesInternal() {
        initializerDAO.createTables();
    }

    @Override
    protected void exportDataInternal() {
        try {
            // create privileged Executors
            createPrivelegedExecutors();
            // create system
            createSystem();
            // define executor permissions
            defineExecutorPermissions();
            // define system permissions
            defineSystemPermissions();
            defineRelationsGroupPermissions();
            defineAdditionalTypesPermissions();
            // grant default permissions on Executors
            grantDefaultExecutorsPermissions();
            // grant default permissions on system
            grantDefaultSystemPermissions();
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    protected void grantDefaultSystemPermissions() throws ExecutorOutOfDateException, SecuredObjectOutOfDateException,
            UnapplicablePermissionException {
        permissionDAO.setPermissions(adminGroup, new SystemPermission().getAllPermissions(), aaSystemSecuredObject);
        permissionDAO.setPermissions(adminGroup, new RelationPermission().getAllPermissions(), relationsGroupSecuredObject);
    }

    private void defineExecutorPermissions() throws ExecutorOutOfDateException {
        List<Executor> privilegedExecutor = Lists.newArrayList((Executor) adminGroup);
        securedObjectDAO.addType(Actor.class, privilegedExecutor, ActorPermission.class);
        securedObjectDAO.addType(Group.class, privilegedExecutor, GroupPermission.class);
    }

    private void createSystem() throws SecuredObjectAlreadyExistsException {
        aaSystemSecuredObject = securedObjectDAO.create(ASystem.SYSTEM);
        relationsGroupSecuredObject = securedObjectDAO.create(RelationsGroupSecure.INSTANCE);
    }

    protected void createPrivelegedExecutors() throws ExecutorAlreadyExistsException, ExecutorOutOfDateException, ExecutorAlreadyInGroupException,
            WeakPasswordException {
        admin = new Actor(administratorName, administratorDescription);
        admin = executorDAO.create(admin);
        executorDAO.setPassword(admin, administratorPassword);
        adminGroup = new Group(administratorGroupName, administratorGroupDescription);
        adminGroup = executorDAO.create(adminGroup);
        List<Executor> executors = Lists.newArrayList((Executor) admin);
        executorDAO.addExecutorsToGroup(executors, adminGroup);
        executorDAO.create(new Actor(SystemExecutors.PROCESS_STARTER_NAME, SystemExecutors.PROCESS_STARTER_DESCRIPTION));
    }

    /**
     * This method must me override (without super.method call) if you want to
     * associate another permission class with system.
     */
    protected void defineSystemPermissions() throws ExecutorOutOfDateException {
        List<Executor> privilegedExecutor = Lists.newArrayList((Executor) adminGroup);
        securedObjectDAO.addType(ASystem.class, privilegedExecutor, SystemPermission.class);
    }

    protected void defineRelationsGroupPermissions() throws ExecutorOutOfDateException {
        List<Executor> privilegedExecutor = Lists.newArrayList((Executor) adminGroup);
        securedObjectDAO.addType(RelationsGroupSecure.class, privilegedExecutor, RelationPermission.class);
        securedObjectDAO.addType(Relation.class, privilegedExecutor, RelationPermission.class);
    }

    protected void grantDefaultExecutorsPermissions() throws ExecutorOutOfDateException, SecuredObjectOutOfDateException,
            UnapplicablePermissionException {
        List<Executor> adminGroupOwners = Lists.newArrayList((Executor) adminGroup);
        permissionDAO.setPermissions(adminGroupOwners, new GroupPermission().getAllPermissions(), securedObjectDAO.get(adminGroup));
        List<Executor> adminOwners = Lists.newArrayList(adminGroup, admin);
        permissionDAO.setPermissions(adminOwners, new ActorPermission().getAllPermissions(), securedObjectDAO.get(admin));
    }

    protected void defineAdditionalTypesPermissions() throws ExecutorOutOfDateException, SecuredObjectAlreadyExistsException {
    }
}
