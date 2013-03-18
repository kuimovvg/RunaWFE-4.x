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
package ru.runa.wfe.presentation.hibernate;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

/**
 * Creates {@link Query} to load data according to {@link BatchPresentation}.
 */
public class BatchPresentationHibernateCompiler {

    /**
     * {@link BatchPresentation}, used to load data.
     */
    private final BatchPresentation batchPresentation;

    /**
     * Parameters, used to create last hibernate query or set explicitly to
     * compiler.
     */
    private HibernateCompilerParameters parameters;

    /**
     * Creates component to build loading data {@link Query}.
     * 
     * @param batchPresentation
     *            {@link BatchPresentation}, used to load data.
     */
    public BatchPresentationHibernateCompiler(BatchPresentation batchPresentation) {
        this.batchPresentation = batchPresentation;
    }

    /**
     * Creates query to load data according to {@link BatchPresentation}.
     * 
     * @return {@link Query} to load data.
     */
    public <T extends Object> List<T> getBatch() {
        return getBatchQuery(new HibernateCompilerParameters(parameters, false)).list();
    }

    /**
     * Creates query to load data count according to {@link BatchPresentation}.
     * 
     * @return {@link Query} to load data.
     */
    public int getCount() {
        Number number = (Number) getBatchQuery(new HibernateCompilerParameters(parameters, true)).uniqueResult();
        return number.intValue();
    }

    /**
     * Creates query to load data according to {@link BatchPresentation}.
     * 
     * @param enablePaging
     *            Flag, equals true, if paging must be used in query; false
     *            otherwise.
     * @return {@link Query} to load data.
     */
    public <T extends Object> List<T> getBatch(boolean enablePaging) {
        parameters = new HibernateCompilerParameters(null, null, enablePaging, false, null);
        return getBatchQuery(parameters).list();
    }

    /**
     * Creates query to load data according to {@link BatchPresentation}.
     * Restrictions may not be set (if null).
     * 
     * @param owners
     *            Collection of owners id (Long for example).
     * @param ownersDBPath
     *            HQL path from root object to calculate object owner (actorId
     *            for {@link Task} for example).
     * @param enablePaging
     *            Flag, equals true, if paging must be used in query; false
     *            otherwise.
     * @return {@link Query} to load data.
     */
    public <T extends Object> List<T> getBatch(Collection<?> owners, String ownersDBPath, boolean enablePaging) {
        parameters = new HibernateCompilerParameters(owners, ownersDBPath, enablePaging, false, null);
        return getBatchQuery(parameters).list();
    }

    /**
     * Creates query to load data according to {@link BatchPresentation}.
     * Restrictions may not be set (if null).
     * 
     * @param enablePaging
     *            Flag, equals true, if paging must be used in query; false
     *            otherwise.
     * @return {@link Query} to load data. TODO unused
     */
    public List<Number> getIdentities(boolean enablePaging) {
        parameters = new HibernateCompilerParameters(null, null, enablePaging, false, true);
        return getBatchQuery(parameters).list();
    }

    /**
     * Creates query to load data according to {@link BatchPresentation} with
     * owners and permission restriction.
     * 
     * @param enablePaging
     *            Flag, equals true, if paging must be used in query; false
     *            otherwise.
     * @param user
     *            User which must has permission on queried objects.
     * @param permission
     *            Permission, which at least one executors must has on queried
     *            objects.
     * @param securedObjectTypes
     *            Type of secured object for queried objects.
     * @return {@link Query} to load data.
     */
    public <T extends Object> List<T> getBatch(boolean enablePaging, User user, Permission permission, SecuredObjectType[] securedObjectTypes) {
        parameters = new HibernateCompilerParameters(null, null, enablePaging, false, user, permission, securedObjectTypes, null, null);
        return getBatchQuery(parameters).list();
    }

    /**
     * Creates query to load data count according to {@link BatchPresentation}
     * with owners and permission restriction.
     * 
     * @param user
     *            User which must has permission on queried objects.
     * @param permission
     *            Permission, which at least one executors must has on queried
     *            objects.
     * @param securedObjectTypes
     *            Type of secured object for queried objects.
     * @return {@link Query} to load data.
     */
    public int getCount(User user, Permission permission, SecuredObjectType[] securedObjectTypes) {
        parameters = new HibernateCompilerParameters(null, null, false, true, user, permission, securedObjectTypes, null, null);
        Number number = (Number) getBatchQuery(parameters).uniqueResult();
        return number.intValue();
    }

    /**
     * Creates query to load data according to {@link BatchPresentation}.
     * Restrictions may not be set (if null).
     * 
     * @param concretteClass
     *            Subclass of root persistent class to be loaded by query.
     * @param enablePaging
     *            Flag, equals true, if paging must be used in query; false
     *            otherwise.
     * @return {@link Query} to load data.
     */
    public <T extends Object> List<T> getBatch(Class<T> concretteClass, boolean enablePaging) {
        parameters = new HibernateCompilerParameters(null, null, enablePaging, false, concretteClass);
        return getBatchQuery(parameters).list();
    }

    /**
     * Save compiler parameters. It would be used for creating queries with
     * *Saved* methods.
     * 
     * @param concreteClass
     *            Subclass of root persistent class to be loaded by query.
     * @param owners
     *            Collection of owners id (Long for example).
     * @param ownersDBPath
     *            HQL path from root object to calculate object owner (actorId
     *            for {@link Task} for example).
     * @param enablePaging
     *            Flag, equals true, if paging must be used in query; false
     *            otherwise.
     * @param user
     *            User which must has permission on queried objects.
     * @param permission
     *            Permission, which at least one executors must has on queried
     *            objects.
     * @param securedObjectTypes
     *            Type of secured object for queried objects.
     * @param idRestrictions
     *            Restrictions, applied to object identity. Must be HQL query
     *            string or null.
     */
    public void setParameters(Class<?> concreteClass, boolean enablePaging, User user, Permission permission, SecuredObjectType[] securedObjectTypes,
            String[] idRestrictions) {
        parameters = new HibernateCompilerParameters(null, null, enablePaging, false, user, permission, securedObjectTypes, concreteClass,
                idRestrictions);
    }

    public void setParameters(boolean enablePaging) {
        parameters = new HibernateCompilerParameters(null, null, enablePaging, false, null);
    }

    /**
     * Creates query to load data from database.
     * 
     * @param compilerParams
     *            {@link Query} creation parameters.
     * @return {@link Query} to load data from database.
     */
    private Query getBatchQuery(HibernateCompilerParameters compilerParams) {
        HibernateCompilerQueryBuilder builder = new HibernateCompilerQueryBuilder(batchPresentation, compilerParams);
        Query query = builder.build();
        Map<String, QueryParameter> placeholders = builder.getPlaceholders();
        if (compilerParams.getExecutorIdsToCheckPermission() != null) {
            query.setParameterList("securedOwnersIds", compilerParams.getExecutorIdsToCheckPermission());
            query.setParameter("securedPermission", compilerParams.getPermission().getMask());
            List<String> typeNames = Lists.newArrayList();
            for (SecuredObjectType type : compilerParams.getSecuredObjectTypes()) {
                typeNames.add(type.name());
            }
            query.setParameterList("securedTypes", typeNames);
            placeholders.remove("securedOwnersIds");
            placeholders.remove("securedPermission");
            placeholders.remove("securedTypes");
        }
        if (compilerParams.hasOwners()) {
            query.setParameterList("ownersIds", compilerParams.getOwners());
            placeholders.remove("ownersIds");
        }
        if (compilerParams.isPagingEnabled()) {
            query.setFirstResult((batchPresentation.getPageNumber() - 1) * batchPresentation.getRangeSize());
            query.setMaxResults(batchPresentation.getRangeSize());
        }
        for (Iterator<Map.Entry<String, QueryParameter>> iter = placeholders.entrySet().iterator(); iter.hasNext();) {
            QueryParameter queryParameter = iter.next().getValue();
            query.setParameter(queryParameter.getName(), queryParameter.getValue());
        }
        return query;
    }
}
