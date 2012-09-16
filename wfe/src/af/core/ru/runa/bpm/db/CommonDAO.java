package ru.runa.bpm.db;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public abstract class CommonDAO extends HibernateDaoSupport {
    protected static final Log log = LogFactory.getLog(ProcessDefinitionDAO.class);

    protected <T extends Object> T get(Class<T> clazz, Long id) {
        return getHibernateTemplate().get(clazz, id);
    }

    protected <T extends Object> T getFirstOrNull(List<T> list) {
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    protected <T extends Object> T findFirstOrNull(String hql, Object... parameters) {
        List<T> list = getHibernateTemplate().find(hql, parameters);
        return getFirstOrNull(list);
    }

}
