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
package ru.runa.af.presentation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;

import ru.runa.af.Permission;
import ru.runa.af.util.QueryParameter;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.commons.ArraysCommons;

/**
 * Creates {@link Query} to load data according to {@link BatchPresentation}.
 */
public class BatchPresentationHibernateCompiler {

    /**
     * {@link BatchPresentation}, used to load data.
     */
    private final BatchPresentation batchPresentation;

    /**
     * Parameters, used to create last hibernate query or set explicitly to compiler.
     */
    private HibernateCompilerParameters parameters;
    
    /**
     * Creates component to build loading data {@link Query}.
     * @param batchPresentation {@link BatchPresentation}, used to load data.
     */
    public BatchPresentationHibernateCompiler(BatchPresentation batchPresentation) {
        this.batchPresentation = batchPresentation;
    }

    /**
     * Creates query to load data according to {@link BatchPresentation}.
     * @param enablePaging Flag, equals true, if paging must be used in query; false otherwise.
     * @return {@link Query} to load data.
     */
    public <T extends Object> List<T> getBatch() {
        return getBatchQuery(new HibernateCompilerParameters(parameters, false)).list();
    }

    /**
     * Creates query to load data count according to {@link BatchPresentation}.
     * @return {@link Query} to load data.
     */
    public int getCount() {
        Number number = (Number) getBatchQuery(new HibernateCompilerParameters(parameters, true)).uniqueResult();
        return number.intValue();
    }

    /**
     * Creates query to load data according to {@link BatchPresentation}.
     * @param session {@link Session} for query creation.
     * @param enablePaging Flag, equals true, if paging must be used in query; false otherwise.
     * @return {@link Query} to load data.
     */
    public <T extends Object> List<T> getBatch(boolean enablePaging) {
        parameters = new HibernateCompilerParameters(null, null, enablePaging, false, null);
        return getBatchQuery(parameters).list();
    }

    /**
     * Creates query to load data according to {@link BatchPresentation}.
     * Restrictions may not be set (if null).
     * @param owners Collection of owners id (Long for example).
     * @param ownersDBPath HQL path from root object to calculate object owner (actorId for {@link TaskInstance} for example).
     * @param enablePaging Flag, equals true, if paging must be used in query; false otherwise.
     * @return {@link Query} to load data.
     */
    public <T extends Object> List<T> getBatch(Collection<?> owners, String ownersDBPath, boolean enablePaging) {
        parameters = new HibernateCompilerParameters(owners, ownersDBPath, enablePaging, false, null);
        return getBatchQuery(parameters).list();
    }

    /**
     * Creates query to load data according to {@link BatchPresentation}.
     * Restrictions may not be set (if null).
     * @param owners Collection of owners id (Long for example).
     * @param ownersDBPath HQL path from root object to calculate object owner (actorId for {@link TaskInstance} for example).
     * @param enablePaging Flag, equals true, if paging must be used in query; false otherwise.
     * @return {@link Query} to load data.
     */
    public List<Number> getIdentities(Collection<?> owners, String ownersDBPath, boolean enablePaging) {
        parameters = new HibernateCompilerParameters(owners, ownersDBPath, enablePaging, false, null, true);
        return getBatchQuery(parameters).list();
    }

    /**
     * Creates query to load data according to {@link BatchPresentation} with owners and permission restriction.
     * @param enablePaging Flag, equals true, if paging must be used in query; false otherwise.
     * @param executorIds Executors, which must has permission on queried objects.
     * @param permission Permission, which at least one executors must has on queried objects.
     * @param securedObjectTypes Type of secured object for queried objects. 
     * @return {@link Query} to load data.
     */
    public <T extends Object> List<T> getBatch(boolean enablePaging, List<Long> executorIds, Permission permission, int[] securedObjectTypes) {
        parameters = new HibernateCompilerParameters(null, null, enablePaging, false, executorIds, permission, securedObjectTypes, null, null);
        return getBatchQuery(parameters).list();
    }

    /**
     * Creates query to load data count according to {@link BatchPresentation} with owners and permission restriction.
     * @param executorIds Executors, which must has permission on queried objects.
     * @param permission Permission, which at least one executors must has on queried objects.
     * @param securedObjectTypes Type of secured object for queried objects. 
     * @return {@link Query} to load data.
     */
    public int getCount(List<Long> executorIds, Permission permission, int[] securedObjectTypes) {
        parameters = new HibernateCompilerParameters(null, null, false, true, executorIds, permission, securedObjectTypes, null, null);
        Number number = (Number) getBatchQuery(parameters).uniqueResult();
        return number.intValue();
    }

    /**
     * Creates query to load data according to {@link BatchPresentation}.
     * Restrictions may not be set (if null).
     * @param concretteClass Subclass of root persistent class to be loaded by query.
     * @param enablePaging Flag, equals true, if paging must be used in query; false otherwise.
     * @return {@link Query} to load data.
     */
    public <T extends Object> List<T> getBatch(Class<T> concretteClass, boolean enablePaging) {
        parameters = new HibernateCompilerParameters(null, null, enablePaging, false, concretteClass);
        return getBatchQuery(parameters).list();
    }

    /**
     * Creates query to load data count according to {@link BatchPresentation}.
     * Restrictions may not be set (if null).
     * @param concretteClass Subclass of root persistent class to be loaded by query.
     * @return {@link Query} to load data.
     */
    public int getCount(Class<?> concretteClass) {
        parameters = new HibernateCompilerParameters(null, null, false, true, concretteClass);
        Number number = (Number) getBatchQuery(parameters).uniqueResult();
        return number.intValue();
    }

    /**
     * Creates query to load data according to {@link BatchPresentation} with owners and permission restriction.
     * Restrictions may not be set (if null).
     * This method got all query parameters.
     * @param concretteClass Subclass of root persistent class to be loaded by query.
     * @param owners Collection of owners id (Long for example).
     * @param ownersDBPath HQL path from root object to calculate object owner (actorId for {@link TaskInstance} for example).
     * @param enablePaging Flag, equals true, if paging must be used in query; false otherwise.
     * @param executorIds Executors, which must has permission on queried objects.
     * @param permission Permission, which at least one executors must has on queried objects.
     * @param securedObjectTypes Type of secured object for queried objects.
     * @param idRestrictions Restrictions, applied to object identity. Must be HQL query string or null.
     * @return {@link Query} to load data.
     */
    public <T extends Object> List<T> getBatch(Class<T> concretteClass, Collection<?> owners, String ownersDBPath, boolean enablePaging,
            List<Long> executorIds, Permission permission, int[] securedObjectTypes, String[] idRestrictions) {
        parameters = new HibernateCompilerParameters(owners, ownersDBPath, enablePaging, false, executorIds, permission, securedObjectTypes,
                concretteClass, idRestrictions);
        return getBatchQuery(parameters).list();
    }

    /**
     * Creates query to load data count according to {@link BatchPresentation} with owners and permission restriction.
     * Restrictions may not be set (if null).
     * This method got all query parameters.
     * @param concreteClass Subclass of root persistent class to be loaded by query.
     * @param owners Collection of owners id (Long for example).
     * @param ownersDBPath HQL path from root object to calculate object owner (actorId for {@link TaskInstance} for example).
     * @param executorIds Executors, which must has permission on queried objects.
     * @param permission Permission, which at least one executors must has on queried objects.
     * @param securedObjectTypes Type of secured object for queried objects. 
     * @param idRestrictions Restrictions, applied to object identity. Must be HQL query string or null.
     * @return {@link Query} to load data.
     */
    public int getCount(Class<?> concreteClass, Collection<?> owners, String ownersDBPath, List<Long> executorIds,
            Permission permission, int[] securedObjectTypes, String[] idRestrictions) {
        parameters = new HibernateCompilerParameters(owners, ownersDBPath, false, true, executorIds, permission, securedObjectTypes, concreteClass,
                idRestrictions);
        Number number = (Number) getBatchQuery(parameters).uniqueResult();
        return number.intValue();
    }

    /**
     * Save compiler parameters. It would be used for creating queries with *Saved* methods.
     * @param concretteClass Subclass of root persistent class to be loaded by query.
     * @param owners Collection of owners id (Long for example).
     * @param ownersDBPath HQL path from root object to calculate object owner (actorId for {@link TaskInstance} for example).
     * @param enablePaging Flag, equals true, if paging must be used in query; false otherwise.
     * @param executorIds Executors, which must has permission on queried objects.
     * @param permission Permission, which at least one executors must has on queried objects.
     * @param securedObjectTypes Type of secured object for queried objects. 
     * @param idRestrictions Restrictions, applied to object identity. Must be HQL query string or null.
     */
    public void setParameters(Class<?> concretteClass, Collection<?> owners, String ownersDBPath, boolean enablePaging, List<Long> executorIds,
            Permission permission, int[] securedObjectTypes, String[] idRestrictions) {
        parameters = new HibernateCompilerParameters(owners, ownersDBPath, enablePaging, false, executorIds, permission, securedObjectTypes,
                concretteClass, idRestrictions);
    }

    /**
     * Creates query to load data from database.
     * @param compilerParams {@link Query} creation parameters.
     * @return {@link Query} to load data from database.
     */
    private Query getBatchQuery(HibernateCompilerParameters compilerParams) {
        HibernateCompilerQueryBuilder builder = new HibernateCompilerQueryBuilder(batchPresentation, compilerParams);
        Query query = builder.build();
        Map<String, QueryParameter> placeholders = builder.getPlaceholders();
        if (compilerParams.isSequresQuery()) {
            query.setParameterList("securedOwnersIds", compilerParams.getExecutorIds());
            query.setParameter("securedPermission", compilerParams.getPermission().getMask());
            query.setParameterList("securedTypes", ArraysCommons.createIntegerList(compilerParams.getSecuredObjectTypes()));
            placeholders.remove("securedOwnersIds");
            placeholders.remove("securedPermission");
            placeholders.remove("securedTypes");
        }
        if (compilerParams.hasOwners()) {
            Collection<?> owners = compilerParams.getOwners();
            if (owners.isEmpty() || owners.size() > 50 || owners.iterator().next().getClass() != String.class) {
                query.setParameterList("ownersIds", owners);
                placeholders.remove("ownersIds");
            } else {
                int i = 1;
                for (Object o : owners) {
                    query.setParameter("ownersIds" + i, o);
                    placeholders.remove("ownersIds" + i);
                    ++i;
                }
            }
        }
        if (compilerParams.isPagingEnabled()) {
            query.setFirstResult((batchPresentation.getPageNumber() - 1) * batchPresentation.getRangeSize());
            query.setMaxResults(batchPresentation.getRangeSize());
        }
        for (Iterator<Map.Entry<String, QueryParameter>> iter = placeholders.entrySet().iterator(); iter.hasNext();) {
            QueryParameter queryParameter = iter.next().getValue();
            query.setParameter(queryParameter.getName(), queryParameter.getValue(), queryParameter.getType());
        }
        return query;
    }
}
