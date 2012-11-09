package ru.runa.wfe.job.dao;

import java.util.Date;
import java.util.List;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.Timer;

/**
 * DAO for {@link Job} hierarchy.
 * 
 * @author dofs
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public class JobDAO extends GenericDAO<Job> {

    public List<Job> getExpiredJobs() {
        return getHibernateTemplate().find("from Job where dueDate<=? order by dueDate", new Date());
    }

    public void deleteTimersByName(String name, Token token) {
        log.debug("deleting timers by name '" + name + "' for " + token);
        List<Timer> timers = getHibernateTemplate().find("from Timer where token=? and name=?", token, name);
        getHibernateTemplate().deleteAll(timers);
        log.debug(timers.size() + " timers by name '" + name + "' for " + token + " were deleted");
    }

    public void deleteJobs(Process process) {
        log.debug("deleting jobs for process " + process.getId());
        List<Job> jobs = getHibernateTemplate().find("from Job where process=?", process);
        getHibernateTemplate().deleteAll(jobs);
    }

}
