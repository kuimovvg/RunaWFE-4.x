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
import java.util.Set;

import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.ExecutorAlreadyInGroupException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.GroupPermission;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.SecuredObject;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.SystemExecutors;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.dao.PermissionDAO;
import ru.runa.af.dao.SecuredObjectDAO;
import ru.runa.af.presentation.BatchPresentation;

import com.google.common.collect.Lists;

/**
 * Created on 14.03.2005
 * 
 */
public class CommonLogic {
    @Autowired
    protected PermissionDAO permissionDAO;
    @Autowired
    protected ExecutorDAO executorDAO;
    @Autowired
    protected SecuredObjectDAO securedObjectDAO;

    protected <T extends Executor> T checkPermissionsOnExecutor(Subject subject, T executor, Permission permission) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        executor = checkIsExecutorValid(executor);
        if (executor.getName().equals(SystemExecutors.PROCESS_STARTER_NAME) && permission.equals(Permission.READ)) {
            return executor;
        }
        checkPermissionAllowed(subject, executor, permission);
        return executor;
    }

    protected <T extends Executor> List<T> checkPermissionsOnExecutors(Subject subject, List<T> executors, Permission permission)
            throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        List<T> newExecutors = checkIsExecutorsValid(executors);
        List<SecuredObject> securedObjects = securedObjectDAO.get(newExecutors);
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        boolean[] allowed = permissionDAO.isAllowed(actor, permission, securedObjects);
        for (int i = 0; i < allowed.length; i++) {
            if (!allowed[i] && !executors.get(i).getName().equals(SystemExecutors.PROCESS_STARTER_NAME)) {
                throw new AuthorizationException(actor + " does not have permission " + permission + " to perform operation with object of class "
                        + newExecutors.get(i).getClass() + " and id " + newExecutors.get(i).getId());
            }
        }
        return newExecutors;
    }

    protected void checkPermissionAllowed(Subject subject, Identifiable identifiable, Permission permission) throws AuthorizationException,
            AuthenticationException {
        if (!isPermissionAllowed(subject, identifiable, permission)) {
            throw new AuthorizationException(SubjectPrincipalsHelper.getActor(subject) + " does not have permission " + permission
                    + " to perform operation with object of class " + identifiable.getClass() + " and id " + identifiable.getId());
        }
    }

    public boolean isPermissionAllowed(Subject subject, Identifiable identifiable, Permission permission) throws AuthenticationException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        SecuredObject securedObject = securedObjectDAO.get(identifiable);
        return permissionDAO.isAllowed(actor, permission, securedObject);
    }

    protected <T extends Identifiable> List<T> filterIdentifiable(Subject subject, List<T> identifiables, Permission permission)
            throws AuthenticationException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        boolean[] allowedArray = permissionDAO.isAllowed(actor, permission, securedObjectDAO.get(identifiables));
        List<T> identifiableList = Lists.newArrayListWithExpectedSize(identifiables.size());
        for (int i = 0; i < allowedArray.length; i++) {
            if (allowedArray[i]) {
                identifiableList.add(identifiables.get(i));
            }
        }
        return identifiableList;
    }

    private <T extends Executor> T checkIsExecutorValid(T executor) throws ExecutorOutOfDateException {
        // TODO really need?
        return executorDAO.getExecutor(executor);
    }

    private <T extends Executor> List<T> checkIsExecutorsValid(List<T> executors) throws ExecutorOutOfDateException {
        List<T> newExecutors = Lists.newArrayListWithExpectedSize(executors.size());
        for (T executor : executors) {
            newExecutors.add(checkIsExecutorValid(executor));
        }
        return newExecutors;
    }

    protected void setPrivelegedExecutorsPermissionsOnIdentifiable(Identifiable identifiable) throws ExecutorOutOfDateException,
            SecuredObjectOutOfDateException, UnapplicablePermissionException {
        Set<Executor> privelegedExecutors = securedObjectDAO.getPrivilegedExecutors(identifiable);
        List<Permission> p = securedObjectDAO.getNoPermission(identifiable).getAllPermissions();
        SecuredObject so = securedObjectDAO.get(identifiable);
        permissionDAO.setPermissions(privelegedExecutors, p, so);
    }

    public int[] getSecuredObjectTypes(Class<? extends Identifiable>[] securedObjectClasses) {
        int[] securedObjectTypes = new int[securedObjectClasses.length];
        for (int i = 0; i < securedObjectTypes.length; i++) {
            securedObjectTypes[i] = securedObjectDAO.getType(securedObjectClasses[i]);
        }
        return securedObjectTypes;
    }

    /**
     * Load objects list according to {@linkplain BatchPresentation} with permission check for subject.
     * @param subject Current actor {@linkplain Subject}.
     * @param batchPresentation {@linkplain BatchPresentation} to load objects.
     * @param permission {@linkplain Permission}, which current actor must have on loaded objects.
     * @param securedObjectClasses 
     *              Classes, loaded by query. Must be subset of classes, loaded by {@linkplain BatchPresentation}.
     *              For example {@linkplain Actor} for {@linkplain BatchPresentation}, which loads {@linkplain Executor}.
     * @param enablePaging Flag, equals true, if paging must be enabled; false to load all objects.
     * @return Loaded according to {@linkplain BatchPresentation} objects list.
     */
    public <T extends Object> List<T> getPersistentObjects(Subject subject, BatchPresentation batchPresentation, Permission permission,
            Class<? extends Identifiable>[] securedObjectClasses, boolean enablePaging) throws AuthenticationException {
        int[] securedObjectTypes = getSecuredObjectTypes(securedObjectClasses);
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        List<Long> actorAndGroupsIds = executorDAO.getActorAndGroupsIds(actor);
        return (List<T>) securedObjectDAO.getPersistentObjects(actorAndGroupsIds, batchPresentation, permission, securedObjectTypes, enablePaging);
    }

    /**
     * Load objects count according to {@linkplain BatchPresentation} with permission check for subject.
     * @param subject Current actor {@linkplain Subject}.
     * @param batchPresentation {@linkplain BatchPresentation} to load objects count.
     * @param permission {@linkplain Permission}, which current actor must have on loaded objects.
     * @param securedObjectClasses 
     *              Classes, loaded by query. Must be subset of classes, loaded by {@linkplain BatchPresentation}.
     *              For example {@linkplain Actor} for {@linkplain BatchPresentation}, which loads {@linkplain Executor}.
     * @return Objects count, which will be loaded according to {@linkplain BatchPresentation}.
     */
    public int getPersistentObjectCount(Subject subject, BatchPresentation batchPresentation, Permission permission,
            Class<? extends Identifiable>[] securedObjectClasses) throws AuthenticationException {
        int[] securedObjectTypes = getSecuredObjectTypes(securedObjectClasses);
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        List<Long> actorAndGroupsIds = executorDAO.getActorAndGroupsIds(actor);
        return securedObjectDAO.getPersistentObjectCount(actorAndGroupsIds, batchPresentation, permission, securedObjectTypes);
    }

    public Group getTemporaryGroup(Group tokenGroup, Set<Executor> assignedExecutors) throws ExecutorAlreadyExistsException,
            ExecutorOutOfDateException, ExecutorAlreadyInGroupException {
        if (executorDAO.isExecutorExist(tokenGroup.getName())) {
            tokenGroup = (Group) executorDAO.getExecutor(tokenGroup.getName());
            executorDAO.clearGroup(tokenGroup.getId());
        } else {
            tokenGroup = executorDAO.create(tokenGroup);
        }
        executorDAO.addExecutorsToGroup(assignedExecutors, tokenGroup);
        try {
            SecuredObject so = securedObjectDAO.get(tokenGroup);
            permissionDAO.setPermissions(assignedExecutors, Lists.newArrayList(GroupPermission.LIST_GROUP, GroupPermission.READ), so);
        } catch (Exception e) {
            throw new InternalApplicationException("Can't set permission to dynamic group");
        }
        return tokenGroup;
    }

    public Group getTemporaryGroup(Long processId, String swimlane, Set<Executor> assignedExecutors) throws ExecutorAlreadyExistsException,
            ExecutorOutOfDateException, ExecutorAlreadyInGroupException {
        Group tokenGroup = Group.createTemporaryGroup(processId + "_" + swimlane);
        return getTemporaryGroup(tokenGroup, assignedExecutors);
    }

    public String encodeExecutors(Set<Executor> assignedExecutors) throws ExecutorAlreadyExistsException, ExecutorAlreadyInGroupException,
            ExecutorOutOfDateException {
        StringBuilder result = new StringBuilder();
        for (Executor executor : assignedExecutors) {
            result.append(executor.getId()).append(';');
        }
        return result.toString();
    }
}
