package ru.runa.wfe.ss.dao;

import java.util.List;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;

import com.google.common.base.Preconditions;

/**
 * DAO for {@link SubstitutionCriteria}.
 * 
 * @author dofs
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public class SubstitutionCriteriaDAO extends GenericDAO<SubstitutionCriteria> {

    public void store(SubstitutionCriteria substitutionCriteria) {
        Preconditions.checkNotNull(substitutionCriteria);
        getHibernateTemplate().saveOrUpdate(substitutionCriteria);
    }

    public void delete(SubstitutionCriteria criteria) {
        Preconditions.checkNotNull(criteria);
        delete(criteria.getId());
    }

    public List<Substitution> getSubstitutionsByCriteria(SubstitutionCriteria criteria) {
        return getHibernateTemplate().find("from Substitution where criteria=?", criteria);
    }

}
