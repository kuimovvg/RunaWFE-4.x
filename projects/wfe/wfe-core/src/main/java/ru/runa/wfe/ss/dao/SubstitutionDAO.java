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
package ru.runa.wfe.ss.dao;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.dao.CommonDAO;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.user.Actor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * DAO level interface for managing {@linkplain Substitution}'s.
 * 
 * @since 2.0
 */
public class SubstitutionDAO extends CommonDAO {

    /**
     * Load all {@linkplain Substitution}'s.
     * 
     * @return Array of {@linkplain Substitution}'s.
     */
    public List<Substitution> getAllSubstitutions() {
        return getHibernateTemplate().loadAll(Substitution.class);
    }

    /**
     * Load {@linkplain Substitution} by identity. Throws {@linkplain InternalApplicationException} if no substitution found.
     * 
     * @param id
     *            {@linkplain Substitution} identity to load.
     * @return Loaded {@linkplain Substitution}.
     */
    public Substitution getSubstitution(Long id) {
        return getHibernateTemplate().load(Substitution.class, id);
    }

    /**
     * Load {@linkplain Substitution}'s by identity. Throws {@linkplain InternalApplicationException} if at least one substitution not found. Result {@linkplain Substitution}'s
     * order is not specified.
     * 
     * @param ids
     *            {@linkplain Substitution}'s identity to load.
     * @return Loaded {@linkplain Substitution}'s.
     */
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

    /**
     * Save or update {@linkplain Substitution}.
     * 
     * @param substitution
     *            {@linkplain Substitution} to save/update.
     */
    public void storeSubstitution(Substitution substitution) {
        Preconditions.checkNotNull(substitution);
        getHibernateTemplate().saveOrUpdate(substitution);
    }

    /**
     * Remove {@linkplain Substitution}'s.
     * 
     * @param substitutionIds
     *            Removed {@linkplain Substitution}'s identity.
     */
    public void deleteSubstitution(Long id) {
        Preconditions.checkNotNull(id);
        Substitution substitution = getSubstitution(id);
        getHibernateTemplate().delete(substitution);
    }

    /**
     * Remove {@linkplain Substitution}'s.
     * 
     * @param ids
     *            Removed {@linkplain Substitution}'s identity.
     */
    public void deleteSubstitutions(List<Long> ids) {
        Preconditions.checkNotNull(ids);
        for (Long id : ids) {
            deleteSubstitution(id);
        }
    }

    /**
     * Loads all {@linkplain Substitution}'s for {@linkplain Actor}. Loaded {@linkplain Substitution}'s is ordered by substitution position.
     * 
     * @param actorId
     *            {@linkplain Actor} identity to load {@linkplain Substitution}'s.
     * @return {@linkplain Substitution}'s for {@linkplain Actor}.
     */
    public List<Substitution> getActorSubstitutions(Long actorId) {
        return getHibernateTemplate().find("from Substitution where actorId=? order by position", actorId);
    }

    public void createCriteria(SubstitutionCriteria substitutionCriteria) {
        getHibernateTemplate().save(substitutionCriteria);
    }

    public SubstitutionCriteria getCriteria(Long id) {
        return getHibernateTemplate().load(SubstitutionCriteria.class, id);
    }

    public List<SubstitutionCriteria> getAllCriterias() {
        return getHibernateTemplate().loadAll(SubstitutionCriteria.class);
    }

    public void storeCriteria(SubstitutionCriteria substitutionCriteria) {
        Preconditions.checkNotNull(substitutionCriteria);
        getHibernateTemplate().saveOrUpdate(substitutionCriteria);
    }

    public void storeCriterias(List<SubstitutionCriteria> substitutionCriterias) {
        Preconditions.checkNotNull(substitutionCriterias);
        getHibernateTemplate().saveOrUpdateAll(substitutionCriterias);
    }

    public void deleteCriteria(Long id) {
        SubstitutionCriteria criteria = getCriteria(id);
        getHibernateTemplate().delete(criteria);
    }

    public void deleteCriteria(SubstitutionCriteria criteria) {
        Preconditions.checkNotNull(criteria);
        getHibernateTemplate().delete(criteria);
    }

    public List<Substitution> getSubstitutionsByCriteria(SubstitutionCriteria criteria) {
        return getHibernateTemplate().find("from Substitution where criteria=?", criteria);
    }
}
