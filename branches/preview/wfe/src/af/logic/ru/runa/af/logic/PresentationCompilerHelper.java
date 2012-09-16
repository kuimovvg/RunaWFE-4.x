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

import javax.security.auth.Subject;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Group;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.TmpApplicationContextFactory;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.dao.SecuredObjectDAO;
import ru.runa.af.dao.impl.ExecutorGroupRelation;
import ru.runa.af.dao.impl.PermissionMapping;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationHibernateCompiler;
import ru.runa.commons.ApplicationContextFactory;

/**
  * Contains method to create {@linkplain BatchPresentationHibernateCompiler}'s, used to load object list.
  * @author Konstantinov Aleksey 27.02.2012
  */
public final class PresentationCompilerHelper {

    /**
     * Classes, loaded by queries for loading all executors (all, group children and so on).
     */
    private static final Class<? extends Identifiable>[] ALL_EXECUTORS_CLASSES = new Class[] { Actor.class, Group.class };

    static ExecutorDAO executorDAO = TmpApplicationContextFactory.getExecutorDAO();
    static SecuredObjectDAO securedObjectDAO = TmpApplicationContextFactory.getSecuredObjectDAO();
    static CommonLogic commonLogic = TmpApplicationContextFactory.getCommonLogic();
    
    public PresentationCompilerHelper() {
        // TODO better way? 
        ApplicationContextFactory.getContext().getAutowireCapableBeanFactory().autowireBean(this);
    }

    /**
     * Create {@linkplain BatchPresentationHibernateCompiler} for loading all executors.
     * <b>Paging is enabled on executors loading.</b>
     * @param subject Current actor {@linkplain Subject}.
     * @param batchPresentation {@linkplain BatchPresentation} for loading all executors.
     * @return {@linkplain BatchPresentationHibernateCompiler} for loading all executors.
     */
    public static BatchPresentationHibernateCompiler createAllExecutorsCompiler(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException {
        List<Long> executorIds = executorDAO.getActorAndGroupsIds(SubjectPrincipalsHelper.getActor(subject));
        int[] securedObjectTypes = commonLogic.getSecuredObjectTypes(ALL_EXECUTORS_CLASSES);
        BatchPresentationHibernateCompiler compiler = new BatchPresentationHibernateCompiler(batchPresentation);
        compiler.setParameters(Executor.class, null, null, true, executorIds, ExecutorPermission.READ, securedObjectTypes, null);
        return compiler;
    }

    /**
     * Create {@linkplain BatchPresentationHibernateCompiler} for loading group children's (or executors, which not children's for now).
     * Only first level children are loading, not recursive.
     * <b>Paging is enabled on executors loading.</b>
     * @param subject Current actor {@linkplain Subject}.
     * @param group {@linkplain Group}, which children's must be loaded. 
     * @param batchPresentation {@linkplain BatchPresentation} for loading group children's.
     * @param daoHolder Helper object for DAO level access.
     * @param hasExecutor Flag, equals true, if loading executors already in group; false to load executors not in group. 
     * @return {@linkplain BatchPresentationHibernateCompiler} for loading group children's.
     */
    public static BatchPresentationHibernateCompiler createGroupChildrenCompiler(Subject subject, Group group, BatchPresentation batchPresentation,
            boolean hasExecutor) throws AuthenticationException {
        List<Long> executorIds = executorDAO.getActorAndGroupsIds(SubjectPrincipalsHelper.getActor(subject));
        String inClause = hasExecutor ? "IN" : "NOT IN";
        String notInRestriction = inClause + " (SELECT relation.executor.id FROM " + ExecutorGroupRelation.class.getName()
                + " as relation WHERE relation.group.id=" + group.getId() + ")";
        String[] idRestrictions = { notInRestriction, "<> " + group.getId() };
        int[] securedObjectTypes = commonLogic.getSecuredObjectTypes(ALL_EXECUTORS_CLASSES);
        BatchPresentationHibernateCompiler compiler = new BatchPresentationHibernateCompiler(batchPresentation);
        compiler.setParameters(Executor.class, null, null, true, executorIds, ExecutorPermission.READ, securedObjectTypes, idRestrictions);
        return compiler;
    }

    /**
     * Create {@linkplain BatchPresentationHibernateCompiler} for loading executor groups.
     * Loaded first level groups, not recursive.
     * <b>Paging is enabled on executors loading.</b>
     * @param subject Current actor {@linkplain Subject}.
     * @param group {@linkplain Group} for loading executor groups. 
     * @param batchPresentation {@linkplain BatchPresentation} for loading executor groups.
     * @param daoHolder Helper object for DAO level access.
     * @param hasGroup Flag equals true, if loading groups, which already contains executor; false to load groups, which doesn't contains executor. 
     * @return {@linkplain BatchPresentationHibernateCompiler} for loading executor groups.
     */
    public static BatchPresentationHibernateCompiler createExecutorGroupsCompiler(Subject subject, Executor executor,
            BatchPresentation batchPresentation, boolean hasGroup) throws AuthenticationException {
        BatchPresentationHibernateCompiler compiler = new BatchPresentationHibernateCompiler(batchPresentation);
        List<Long> executorIds = executorDAO.getActorAndGroupsIds(SubjectPrincipalsHelper.getActor(subject));
        String inClause = hasGroup ? "IN" : "NOT IN";
        String inRestriction = inClause + " (SELECT relation.group.id FROM " + ExecutorGroupRelation.class.getName()
                + " as relation WHERE relation.executor.id=" + executor.getId() + ")";
        String[] idRestrictions = { inRestriction, "<> " + executor.getId() };
        int[] securedObjectTypes = commonLogic.getSecuredObjectTypes(new Class[] { Group.class });
        compiler.setParameters(Executor.class, null, null, true, executorIds, ExecutorPermission.READ, securedObjectTypes, idRestrictions);
        return compiler;
    }

    /**
     * Create {@linkplain BatchPresentationHibernateCompiler} for loading executor's which already has (or not has) 
     * some permission on specified identifiable.
     * @param subject Current actor {@linkplain Subject}.
     * @param identifiable {@linkplain Identifiable} to load executors, which has (or not) permission on this identifiable.
     * @param batchPresentation {@linkplain BatchPresentation} for loading executors.
     * @param daoHolder Helper object for DAO level access.
     * @param hasPermission Flag equals true to load executors with permissions on {@linkplain Identifiable}; false to load executors without permissions. 
     * @return {@linkplain BatchPresentationHibernateCompiler} for loading executors.
     */
    public static BatchPresentationHibernateCompiler createExecutorWithPermissionCompiler(Subject subject, Identifiable identifiable,
            BatchPresentation batchPresentation, boolean hasPermission) throws SecuredObjectOutOfDateException,
            AuthenticationException {
        BatchPresentationHibernateCompiler compiler = new BatchPresentationHibernateCompiler(batchPresentation);
        List<Long> executorIds = executorDAO.getActorAndGroupsIds(SubjectPrincipalsHelper.getActor(subject));
        Long securedObjectId = securedObjectDAO.get(identifiable).getId();
        String inClause = hasPermission ? "IN" : "NOT IN";
        String idRestriction = inClause + " (SELECT pm.executor.id from " + PermissionMapping.class.getName() + " as pm where pm.securedObject.id="
                + securedObjectId + ")";
        int[] securedObjectTypes = commonLogic.getSecuredObjectTypes(ALL_EXECUTORS_CLASSES);
        compiler.setParameters(Executor.class, null, null, true, executorIds, Permission.READ, securedObjectTypes, new String[] { idRestriction });
        return compiler;
    }
}
