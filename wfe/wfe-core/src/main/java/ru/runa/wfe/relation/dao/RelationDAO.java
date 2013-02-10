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
package ru.runa.wfe.relation.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

import ru.runa.wfe.commons.dao.CommonDAO;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.presentation.hibernate.BatchPresentationHibernateCompiler;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationAlreadyExistException;
import ru.runa.wfe.relation.RelationDoesNotExistException;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.RelationPairDoesNotExistException;
import ru.runa.wfe.user.Executor;

import com.google.common.collect.Lists;

/**
 * Relation dao implementation via Hibernate.
 * 
 * @author Konstantinov Aleksey 12.02.2012
 * @since 3.3
 */
@SuppressWarnings("unchecked")
public class RelationDAO extends CommonDAO {

    /**
     * Create {@link Relation} with specified name and description or throws
     * {@link RelationAlreadyExistException} if relation with such name is
     * already exists.
     * 
     * @param name
     *            Relation name
     * @param description
     *            Relation description
     * @return Created relation.
     */
    public Relation createRelation(String name, String description) {
        Relation relation = getRelation(name);
        if (relation != null) {
            throw new RelationAlreadyExistException(name);
        }
        relation = new Relation(name, description);
        getHibernateTemplate().save(relation);
        return relation;
    }

    /**
     * Return {@link Relation} with specified identity or throws
     * {@link RelationDoesNotExistException} if relation with such identity does
     * not exists.
     * 
     * @param id
     *            Relation identity.
     * @return Relation with specified name.
     */
    public Relation getRelationNotNull(Long id) {
        Relation relation = getHibernateTemplate().get(Relation.class, id);
        if (relation == null) {
            throw new RelationDoesNotExistException(id);
        }
        return relation;
    }

    /**
     * Return {@link Relation} with specified name or throws
     * {@link RelationDoesNotExistException} if relation with such name does not
     * exists.
     * 
     * @param name
     *            Relation name
     * @return Relation with specified name.
     */
    public Relation getRelationNotNull(String name) {
        Relation relation = getRelation(name);
        if (relation == null) {
            throw new RelationDoesNotExistException(name);
        }
        return relation;
    }

    private Relation getRelation(String name) {
        return (Relation) getFirstOrNull(getHibernateTemplate().find("from Relation where name=?", name));
    }

    /**
     * Return list of {@link Relation}, according to specified
     * {@link BatchPresentation}.
     * 
     * @param batchPresentation
     *            Restrictions to get relations.
     * @return List of {@link Relation}.
     */
    public List<Relation> getRelations(BatchPresentation batchPresentation) {
        return new BatchPresentationHibernateCompiler(batchPresentation).getBatch(false);
    }

    /**
     * Remove {@link Relation} with specified identity.
     * 
     * @param id
     *            Relation identity.
     */
    public void removeRelation(Long id) {
        Relation relation = getRelationNotNull(id);
        if (relation == null) {
            throw new RelationDoesNotExistException(id);
        }
        for (RelationPair relationPair : getRelationPairs(relation, null, null)) {
            getHibernateTemplate().delete(relationPair);
        }
        getHibernateTemplate().delete(relation);
    }

    /**
     * Add {@link RelationPair} to {@link Relation} with specified name.
     * 
     * @param relationName
     *            Relation name.
     * @param left
     *            Left part of relation pair.
     * @param right
     *            Right part of relation pair.
     * @return Created relation pair.
     */
    public RelationPair addRelationPair(String relationName, Executor left, Executor right) {
        Relation relation = getRelationNotNull(relationName);
        List<RelationPair> exists = getRelationPairs(relation, Lists.newArrayList(left), Lists.newArrayList(right));
        if (exists.size() > 0) {
            return exists.get(0);
        }
        RelationPair result = new RelationPair(relation, left, right);
        getHibernateTemplate().save(result);
        return result;
    }

    /**
     * Removes {@link RelationPair} with specified identity.
     * 
     * @param id
     *            {@link RelationPair} identity.
     */
    public void removeRelationPair(Long id) {
        RelationPair relationPair = get(RelationPair.class, id);
        if (relationPair == null) {
            throw new RelationPairDoesNotExistException(id);
        }
        getHibernateTemplate().delete(relationPair);
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which right
     * part contains executor from 'right' parameter.
     * 
     * @param relationName
     *            {@link Relation} name. If null, when {@link RelationPair} for
     *            all {@link Relation} returned.
     * @param right
     *            Collection of {@link Executor}, which contains in right part
     *            of {@link RelationPair}.
     * @return List of {@link RelationPair}.
     */
    public List<RelationPair> getExecutorsRelationPairsRight(String relationName, Collection<Executor> from) {
        Relation relation = null;
        if (relationName != null) {
            relation = getRelationNotNull(relationName);
        }
        return getRelationPairs(relation, null, from);
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which left
     * part contains executor from 'left' parameter.
     * 
     * @param relationName
     *            {@link Relation} name. If null, when {@link RelationPair} for
     *            all {@link Relation} returned.
     * @param right
     *            Collection of {@link Executor}, which contains in left part of
     *            {@link RelationPair}.
     * @return List of {@link RelationPair}.
     */
    public List<RelationPair> getExecutorsRelationPairsLeft(String relationName, Collection<Executor> from) {
        Relation relation = null;
        if (relationName != null) {
            relation = getRelationNotNull(relationName);
        }
        return getRelationPairs(relation, from, null);
    }

    /**
     * Return {@link RelationPair} with specified identity.
     * 
     * @param id
     *            {@link RelationPair} identity.
     * @return {@link RelationPair} with specified identity.
     */
    public RelationPair getRelationPairNotNull(Long id) {
        RelationPair relationPair = getHibernateTemplate().get(RelationPair.class, id);
        if (relationPair == null) {
            throw new RelationPairDoesNotExistException(id);
        }
        return relationPair;
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, according to
     * specified {@link BatchPresentation}.
     * 
     * @param relationName
     *            Relation name
     * @param batchPresentation
     *            Restrictions to get {@link RelationPair}.
     * @return
     */
    public List<RelationPair> getRelationPairs(String relationsGroupName, BatchPresentation batchPresentation) {
        Map<Integer, FilterCriteria> filters = batchPresentation.getFilteredFields();
        try {
            // for check
            getRelationNotNull(relationsGroupName);
            filters.put(0, new StringFilterCriteria(new String[] { relationsGroupName }));
            batchPresentation.setFilteredFields(filters);
            List<RelationPair> result = new BatchPresentationHibernateCompiler(batchPresentation).getBatch(false);
            batchPresentation.setFilteredFields(filters);
            return result;
        } finally {
            filters.remove(0);
        }
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, according to
     * specified {@link BatchPresentation}.
     * 
     * @param id
     *            Relation identity.
     * @param batchPresentation
     *            Restrictions to get {@link RelationPair}.
     * @return
     */
    public List<RelationPair> getRelationPairs(Long id, BatchPresentation batchPresentation) {
        Map<Integer, FilterCriteria> filters = batchPresentation.getFilteredFields();
        try {
            Relation relation = getRelationNotNull(id);
            filters.put(0, new StringFilterCriteria(new String[] { relation.getName() }));
            batchPresentation.setFilteredFields(filters);
            List<RelationPair> result = new BatchPresentationHibernateCompiler(batchPresentation).getBatch(false);
            batchPresentation.setFilteredFields(filters);
            return result;
        } finally {
            filters.remove(0);
        }
    }

    /**
     * Deleted all relation pairs for executor.
     * 
     * @param executor
     */
    public void removeAllRelationPairs(Executor executor) {
        getHibernateTemplate().deleteAll(getRelationPairs(null, Lists.newArrayList(executor), null));
        getHibernateTemplate().deleteAll(getRelationPairs(null, null, Lists.newArrayList(executor)));
    }

    private List<RelationPair> getRelationPairs(final Relation relation, final Collection<Executor> left, final Collection<Executor> right) {
        return getHibernateTemplate().execute(new HibernateCallback<List<RelationPair>>() {

            @Override
            public List<RelationPair> doInHibernate(Session session) {
                Criteria criteria = session.createCriteria(RelationPair.class);
                if (relation != null) {
                    criteria.add(Restrictions.eq("relation", relation));
                }
                if (left != null) {
                    criteria.add(Restrictions.in("left", left));
                }
                if (right != null) {
                    criteria.add(Restrictions.in("right", right));
                }
                return criteria.list();
            }
        });
    }

}
