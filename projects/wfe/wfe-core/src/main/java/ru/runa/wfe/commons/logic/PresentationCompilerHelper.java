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
package ru.runa.wfe.commons.logic;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.BatchPresentationHibernateCompiler;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.dao.PermissionMapping;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

/**
 * Contains method to create {@linkplain BatchPresentationHibernateCompiler}'s,
 * used to load object list.
 * 
 * @author Konstantinov Aleksey 27.02.2012
 */
public final class PresentationCompilerHelper {

    /**
     * Classes, loaded by queries for loading all executors (all, group children
     * and so on).
     */
    private static final SecuredObjectType[] ALL_EXECUTORS_CLASSES = { SecuredObjectType.ACTOR, SecuredObjectType.GROUP };

    /**
     * Create {@linkplain BatchPresentationHibernateCompiler} for loading all
     * executors. <b>Paging is enabled on executors loading.</b>
     * 
     * @param user
     *            Current actor {@linkplain user}.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading all executors.
     * @return {@linkplain BatchPresentationHibernateCompiler} for loading all
     *         executors.
     */
    public static BatchPresentationHibernateCompiler createAllExecutorsCompiler(User user, BatchPresentation batchPresentation) {
        BatchPresentationHibernateCompiler compiler = new BatchPresentationHibernateCompiler(batchPresentation);
        compiler.setParameters(null, true, user, ExecutorPermission.READ, ALL_EXECUTORS_CLASSES, null);
        return compiler;
    }

    public static BatchPresentationHibernateCompiler createAllSystemLogsCompiler(User user, BatchPresentation batchPresentation) {
        BatchPresentationHibernateCompiler compiler = new BatchPresentationHibernateCompiler(batchPresentation);
        compiler.setParameters(true);
        return compiler;
    }

    /**
     * Create {@linkplain BatchPresentationHibernateCompiler} for loading group
     * children's (or executors, which not children's for now). Only first level
     * children are loading, not recursive. <b>Paging is enabled on executors
     * loading.</b>
     * 
     * @param user
     *            Current actor {@linkplain user}.
     * @param group
     *            {@linkplain Group}, which children's must be loaded.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading group children's.
     * @param daoHolder
     *            Helper object for DAO level access.
     * @param hasExecutor
     *            Flag, equals true, if loading executors already in group;
     *            false to load executors not in group.
     * @return {@linkplain BatchPresentationHibernateCompiler} for loading group
     *         children's.
     */
    public static BatchPresentationHibernateCompiler createGroupChildrenCompiler(User user, Group group, BatchPresentation batchPresentation,
            boolean hasExecutor) {
        String inClause = hasExecutor ? "IN" : "NOT IN";
        String notInRestriction = inClause + " (SELECT relation.executor.id FROM " + ExecutorGroupMembership.class.getName()
                + " as relation WHERE relation.group.id=" + group.getId() + ")";
        String[] idRestrictions = { notInRestriction, "<> " + group.getId() };
        BatchPresentationHibernateCompiler compiler = new BatchPresentationHibernateCompiler(batchPresentation);
        compiler.setParameters(null, true, user, ExecutorPermission.READ, ALL_EXECUTORS_CLASSES, idRestrictions);
        return compiler;
    }

    /**
     * Create {@linkplain BatchPresentationHibernateCompiler} for loading
     * executor groups. Loaded first level groups, not recursive. <b>Paging is
     * enabled on executors loading.</b>
     * 
     * @param user
     *            Current actor {@linkplain user}.
     * @param group
     *            {@linkplain Group} for loading executor groups.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executor groups.
     * @param daoHolder
     *            Helper object for DAO level access.
     * @param hasGroup
     *            Flag equals true, if loading groups, which already contains
     *            executor; false to load groups, which doesn't contains
     *            executor.
     * @return {@linkplain BatchPresentationHibernateCompiler} for loading
     *         executor groups.
     */
    public static BatchPresentationHibernateCompiler createExecutorGroupsCompiler(User user, Executor executor, BatchPresentation batchPresentation,
            boolean hasGroup) {
        BatchPresentationHibernateCompiler compiler = new BatchPresentationHibernateCompiler(batchPresentation);
        String inClause = hasGroup ? "IN" : "NOT IN";
        String inRestriction = inClause + " (SELECT relation.group.id FROM " + ExecutorGroupMembership.class.getName()
                + " as relation WHERE relation.executor.id=" + executor.getId() + ")";
        String[] idRestrictions = { inRestriction, "<> " + executor.getId() };
        compiler.setParameters(Group.class, true, user, ExecutorPermission.READ, new SecuredObjectType[] { SecuredObjectType.GROUP }, idRestrictions);
        return compiler;
    }

    /**
     * Create {@linkplain BatchPresentationHibernateCompiler} for loading
     * executor's which already has (or not has) some permission on specified
     * identifiable.
     * 
     * @param user
     *            Current actor {@linkplain user}.
     * @param identifiable
     *            {@linkplain Identifiable} to load executors, which has (or
     *            not) permission on this identifiable.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param daoHolder
     *            Helper object for DAO level access.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on
     *            {@linkplain Identifiable}; false to load executors without
     *            permissions.
     * @return {@linkplain BatchPresentationHibernateCompiler} for loading
     *         executors.
     */
    public static BatchPresentationHibernateCompiler createExecutorWithPermissionCompiler(User user, Identifiable identifiable,
            BatchPresentation batchPresentation, boolean hasPermission) {
        BatchPresentationHibernateCompiler compiler = new BatchPresentationHibernateCompiler(batchPresentation);
        String inClause = hasPermission ? "IN" : "NOT IN";
        String idRestriction = inClause + " (SELECT pm.executor.id from " + PermissionMapping.class.getName() + " as pm where pm.identifiableId="
                + identifiable.getIdentifiableId() + " and pm.type=" + identifiable.getSecuredObjectType().ordinal() + ")";
        compiler.setParameters(null, true, user, Permission.READ, ALL_EXECUTORS_CLASSES, new String[] { idRestriction });
        return compiler;
    }
}
