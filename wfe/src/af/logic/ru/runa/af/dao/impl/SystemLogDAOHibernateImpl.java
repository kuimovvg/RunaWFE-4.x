package ru.runa.af.dao.impl;

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ru.runa.af.dao.SystemLogDAO;
import ru.runa.af.log.SystemLog;

public class SystemLogDAOHibernateImpl extends HibernateDaoSupport implements SystemLogDAO {

    @Override
    public void create(SystemLog log) {
        getHibernateTemplate().save(log);
    }

    @Override
    public List<SystemLog> getAllSystemLogs() {
        return getHibernateTemplate().loadAll(SystemLog.class);
    }
    
}
