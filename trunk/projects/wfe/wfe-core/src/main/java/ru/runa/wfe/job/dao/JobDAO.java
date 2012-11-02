package ru.runa.wfe.job.dao;

import java.util.Date;
import java.util.List;

import ru.runa.wfe.commons.dao.CommonDAO;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.Timer;

public class JobDAO extends CommonDAO {

    public Job getJob(Long id) {
        return getHibernateTemplate().get(Job.class, id);
    }

    public List<Job> getExpiredJobs() {
        return getHibernateTemplate().find("from Job where dueDate<=? order by dueDate", new Date());
    }

    public void saveJob(Job job) {
        getHibernateTemplate().saveOrUpdate(job);
    }

    public void deleteJob(Job job) {
        getHibernateTemplate().delete(job);
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
