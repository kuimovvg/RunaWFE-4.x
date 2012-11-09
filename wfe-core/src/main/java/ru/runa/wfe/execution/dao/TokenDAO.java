package ru.runa.wfe.execution.dao;

import java.util.List;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.Token;

/**
 * DAO for {@link Token}.
 * 
 * @author dofs
 * @since 4.0
 */
public class TokenDAO extends GenericDAO<Token> {

    @SuppressWarnings("unchecked")
    public List<Token> findAllActiveTokens() {
        return getHibernateTemplate().find("from Token where endDate is null");
    }

}
