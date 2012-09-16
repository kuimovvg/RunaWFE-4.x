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

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ru.runa.af.ArgumentsCommons;
import ru.runa.af.Substitution;
import ru.runa.af.SubstitutionCriteria;
import ru.runa.af.dao.SubstitutionDAO;

import com.google.common.collect.Lists;

/**
 * Created on 27.01.2006
 * 
 * @author Semochkin_v
 * @author Gordienko_m
 */
public class SubstitutionDAOHibernateImpl extends HibernateDaoSupport implements SubstitutionDAO {

    @Override
    public List<Substitution> getAllSubstitutions() {
        return getHibernateTemplate().loadAll(Substitution.class);
    }

    @Override
    public Substitution getSubstitution(Long id) {
        return getHibernateTemplate().load(Substitution.class, id);
    }

    @Override
    public List<Substitution> getSubstitutions(final List<Long> substitutionIds) {
        if (substitutionIds.size() == 0) {
            return Lists.newArrayList();
        }
        return getHibernateTemplate().executeFind(new HibernateCallback<List<Substitution>>() {

            @Override
            public List<Substitution> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from Substitution where id in (:ids)");
                query.setParameterList("ids", substitutionIds);
                return query.list();
            }
        });
    }

    @Override
    public void storeSubstitution(Substitution substitution) {
        ArgumentsCommons.checkNotNull(substitution);
        getHibernateTemplate().saveOrUpdate(substitution);
    }

    @Override
    public void deleteSubstitution(Long id) {
        ArgumentsCommons.checkNotNull(id);
        Substitution substitution = getSubstitution(id);
        getHibernateTemplate().delete(substitution);
    }

    @Override
    public void deleteSubstitutions(List<Long> ids) {
        ArgumentsCommons.checkNotNull(ids);
        for (Long id : ids) {
            deleteSubstitution(id);
        }
    }

    @Override
    public List<Substitution> getActorSubstitutions(Long actorId) {
        return getHibernateTemplate().find("from Substitution where actorId=? order by position", actorId);
    }

    @Override
    public void createCriteria(SubstitutionCriteria substitutionCriteria) {
        getHibernateTemplate().save(substitutionCriteria);
    }

    @Override
    public SubstitutionCriteria getCriteria(Long id) {
        return getHibernateTemplate().load(SubstitutionCriteria.class, id);
    }

    @Override
    public List<SubstitutionCriteria> getAllCriterias() {
        return getHibernateTemplate().loadAll(SubstitutionCriteria.class);
    }

    @Override
    public void storeCriteria(SubstitutionCriteria substitutionCriteria) {
        ArgumentsCommons.checkNotNull(substitutionCriteria);
        getHibernateTemplate().saveOrUpdate(substitutionCriteria);
    }

    @Override
    public void storeCriterias(List<SubstitutionCriteria> substitutionCriterias) {
        ArgumentsCommons.checkNotNull(substitutionCriterias);
        getHibernateTemplate().saveOrUpdateAll(substitutionCriterias);
    }

    @Override
    public void deleteCriteria(Long id) {
        SubstitutionCriteria criteria = getCriteria(id);
            getHibernateTemplate().delete(criteria);
    }

    @Override
    public void deleteCriteria(SubstitutionCriteria criteria) {
        ArgumentsCommons.checkNotNull(criteria);
        getHibernateTemplate().delete(criteria);
    }

    @Override
    public List<Substitution> getSubstitutionsByCriteria(SubstitutionCriteria criteria) {
        return getHibernateTemplate().find("from Substitution where criteria=?", criteria);
    }
}
