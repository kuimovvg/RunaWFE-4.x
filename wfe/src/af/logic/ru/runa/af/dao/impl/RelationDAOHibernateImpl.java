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
package ru.runa.af.dao.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ru.runa.af.Executor;
import ru.runa.af.Relation;
import ru.runa.af.RelationDoesNotExistsException;
import ru.runa.af.RelationExistException;
import ru.runa.af.RelationPair;
import ru.runa.af.RelationPairDoesNotExistException;
import ru.runa.af.dao.RelationDAO;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationHibernateCompiler;
import ru.runa.af.presentation.filter.FilterCriteria;
import ru.runa.af.presentation.filter.StringFilterCriteria;
import ru.runa.commons.hibernate.HibernateSessionFactory;

/**
  * Relation dao implementation via Hibernate. 
  *
  * @author Konstantinov Aleksey 12.02.2012
  */
public class RelationDAOHibernateImpl extends HibernateDaoSupport implements RelationDAO {

    @Override
    public Relation createRelation(String name, String description) throws RelationExistException {
        Session session = HibernateSessionFactory.getSession();
        Criteria criteria = session.createCriteria(Relation.class);
        criteria.add(Restrictions.eq("name", name));
        if (!criteria.list().isEmpty()) {
            throw new RelationExistException(name);
        }
        Relation result = new Relation(name, description);
        session.save(result);
        return result;
    }

    @Override
    public List<Relation> getRelations(BatchPresentation batchPresentation) {
        return new BatchPresentationHibernateCompiler(batchPresentation).getBatch(false);
    }
    @Override

    public void removeRelation(Long id) throws RelationDoesNotExistsException {
        Session session = HibernateSessionFactory.getSession();
        Criteria criteria = session.createCriteria(Relation.class);
        criteria.add(Restrictions.eq("id", id));
        Relation relation = (Relation) criteria.uniqueResult();
        if (relation == null) {
            throw new RelationDoesNotExistsException(id);
        }
        for (RelationPair relationPair : getRelationPairs(id)) {
            session.delete(relationPair);
        }
        session.delete(relation);
    }
    @Override

    public RelationPair addRelationPair(String relationName, Executor left, Executor right) throws RelationDoesNotExistsException {
        Session session = HibernateSessionFactory.getSession();
        Criteria criteria = session.createCriteria(Relation.class);
        criteria.add(Restrictions.eq("name", relationName));
        Relation relation = (Relation) criteria.uniqueResult();
        if (relation == null) {
            throw new RelationDoesNotExistsException(relationName);
        }
        criteria = session.createCriteria(RelationPair.class);
        criteria.add(Restrictions.eq("relation", relation));
        criteria.add(Restrictions.eq("left", left));
        criteria.add(Restrictions.eq("right", right));
        RelationPair exists = (RelationPair) criteria.uniqueResult();
        if (exists != null) {
            return exists;
        }
        RelationPair result = new RelationPair(relation, left, right);
        session.save(result);
        return result;
    }
    @Override

    public void removeRelationPair(Long id) throws RelationPairDoesNotExistException {
        Session session = HibernateSessionFactory.getSession();
        Criteria criteria = session.createCriteria(RelationPair.class);
        criteria.add(Restrictions.eq("id", id));
        RelationPair relationPair = (RelationPair) criteria.uniqueResult();
        if (relationPair == null) {
            throw new RelationPairDoesNotExistException(id);
        }
        session.delete(relationPair);
    }

    /**
     * Return all {@link RelationPair}, from {@link Relation} with specified identity.
     * 
     * @param id
     *            {@link Relation} identity.
     * @return Lost of {@link RelationPair}.
     * @throws RelationDoesNotExistsException
     *             {@link Relation} with specified id does not exists.
     */
    private List<RelationPair> getRelationPairs(Long id) throws RelationDoesNotExistsException {
        Session session = HibernateSessionFactory.getSession();
        Criteria criteria = session.createCriteria(Relation.class);
        criteria.add(Restrictions.eq("id", id));
        Relation relation = (Relation) criteria.uniqueResult();
        if (relation == null) {
            throw new RelationDoesNotExistsException(id);
        }
        criteria = session.createCriteria(RelationPair.class);
        criteria.add(Restrictions.eq("relation", relation));
        return criteria.list();
    }
    @Override

    public List<RelationPair> getExecutorsRelationPairsRight(String relationName, Collection<Executor> from) throws RelationDoesNotExistsException {
        Criteria criteria = HibernateSessionFactory.getSession().createCriteria(RelationPair.class);
        criteria.add(Restrictions.in("right", from));
        if (relationName != null) {
            criteria.add(Restrictions.eq("relation", getRelation(relationName)));
        }
        return criteria.list();
    }
    @Override

    public List<RelationPair> getExecutorsRelationPairsLeft(String relationName, Collection<Executor> from) throws RelationDoesNotExistsException {
        Criteria criteria = HibernateSessionFactory.getSession().createCriteria(RelationPair.class);
        criteria.add(Restrictions.in("left", from));
        if (relationName != null) {
            criteria.add(Restrictions.eq("relation", getRelation(relationName)));
        }
        return criteria.list();
    }
    @Override

    public RelationPair getRelationPair(Long relationId) throws RelationPairDoesNotExistException {
        Criteria criteria = HibernateSessionFactory.getSession().createCriteria(RelationPair.class);
        criteria.add(Expression.eq("id", relationId));
        RelationPair result = (RelationPair) criteria.uniqueResult();
        if (result == null) {
            throw new RelationPairDoesNotExistException(relationId);
        }
        return result;
    }
    @Override

    public Relation getRelation(String name) throws RelationDoesNotExistsException {
        Criteria criteria = HibernateSessionFactory.getSession().createCriteria(Relation.class);
        criteria.add(Expression.eq("name", name));
        Relation relation = (Relation) criteria.uniqueResult();
        if (relation == null) {
            throw new RelationDoesNotExistsException(name);
        }
        return relation;
    }
    @Override

    public Relation getRelation(Long id) throws RelationDoesNotExistsException {
        Criteria criteria = HibernateSessionFactory.getSession().createCriteria(Relation.class);
        criteria.add(Expression.eq("id", id));
        Relation relation = (Relation) criteria.uniqueResult();
        if (relation == null) {
            throw new RelationDoesNotExistsException(id);
        }
        return relation;
    }
    @Override

    public List<RelationPair> getRelationPairs(String relationsGroupName, BatchPresentation batchPresentation) throws RelationDoesNotExistsException {
        Session session = HibernateSessionFactory.getSession();
        Map<Integer, FilterCriteria> filters = batchPresentation.getFilteredFieldsMap();
        try {
            Criteria criteria = session.createCriteria(Relation.class);
            criteria.add(Expression.eq("name", relationsGroupName));
            Relation relation = (Relation) criteria.uniqueResult();
            if (relation == null) {
                throw new RelationDoesNotExistsException(relationsGroupName);
            }
            filters.put(0, new StringFilterCriteria(new String[] { relationsGroupName }));
            batchPresentation.setFilteredFieldsMap(filters);
            List<RelationPair> result = new BatchPresentationHibernateCompiler(batchPresentation).getBatch(false);
            batchPresentation.setFilteredFieldsMap(filters);
            return result;
        } finally {
            filters.remove(0);
        }
    }
    @Override

    public List<RelationPair> getRelationPairs(Long id, BatchPresentation batchPresentation) throws RelationDoesNotExistsException {
        Session session = HibernateSessionFactory.getSession();
        Map<Integer, FilterCriteria> filters = batchPresentation.getFilteredFieldsMap();
        try {
            Criteria criteria = session.createCriteria(Relation.class);
            criteria.add(Expression.eq("id", id));
            Relation relation = (Relation) criteria.uniqueResult();
            if (relation == null) {
                throw new RelationDoesNotExistsException(id);
            }
            filters.put(0, new StringFilterCriteria(new String[] { relation.getName() }));
            batchPresentation.setFilteredFieldsMap(filters);
            List<RelationPair> result = new BatchPresentationHibernateCompiler(batchPresentation).getBatch(false);
            batchPresentation.setFilteredFieldsMap(filters);
            return result;
        } finally {
            filters.remove(0);
        }
    }

    @Override
    public void removeAllRelationPairs(Executor executor) {
        List<RelationPair> list = getHibernateTemplate().find("from RelationPair p where p.left=? or p.right=?", executor, executor);
        getHibernateTemplate().deleteAll(list);
    }
    
}
