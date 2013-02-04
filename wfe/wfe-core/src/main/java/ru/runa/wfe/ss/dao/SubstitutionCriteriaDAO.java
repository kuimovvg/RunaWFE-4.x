package ru.runa.wfe.ss.dao;

import java.util.List;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;

/**
 * DAO for {@link SubstitutionCriteria}.
 * 
 * @author dofs
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public class SubstitutionCriteriaDAO extends GenericDAO<SubstitutionCriteria> {

    public List<Substitution> getSubstitutionsByCriteria(SubstitutionCriteria criteria) {
        return getHibernateTemplate().find("from Substitution where criteria=?", criteria);
    }

}
