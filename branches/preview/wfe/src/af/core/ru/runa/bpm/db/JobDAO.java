/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.bpm.db;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.graph.def.Action;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.job.Job;
import ru.runa.bpm.job.Timer;
import ru.runa.bpm.job.executor.JobExecutor;

public class JobDAO extends CommonDAO {
    private JobExecutor jobExecutor;

    @Required
    public void setJobExecutor(JobExecutor jobExecutor) {
        this.jobExecutor = jobExecutor;
    }

    public Job getFirstAcquirableJob(String lockOwner) {
        // TODO query.setMaxResults(1);
        return (Job) getHibernateTemplate().find("from ru.runa.bpm.job.Job where retries>0 and dueDate<=? order by dueDate", new Date()).get(0);
    }

    // public List<Job> findExclusiveJobs(String lockOwner, ProcessInstance
    // processInstance) {
    // return
    // getHibernateTemplate().find("from ru.runa.bpm.job.Job where retries>0 and dueDate<=? and processInstance=? and isExclusive=true order by dueDate",
    // new Date(), processInstance);
    // }

    public List<Job> findJobsByToken(Token token) {
        return getHibernateTemplate().find("from ru.runa.bpm.job.Job where token=?", token);
    }

    public Job getFirstDueJob(String lockOwner, Collection<Long> jobIdsToIgnore) {
        // TODO query.setMaxResults(1);
        if (jobIdsToIgnore == null || jobIdsToIgnore.isEmpty()) {
            return (Job) getHibernateTemplate().findByNamedQuery("JobDAO.getFirstDueJob", lockOwner).get(0);
        } else {
            return (Job) getHibernateTemplate().findByNamedQuery("JobDAO.getFirstDueJobExlcMonitoredJobs", lockOwner, jobIdsToIgnore).get(0);
        }
    }

    public void saveJob(Job job, boolean notifyExecutor) {
        getHibernateTemplate().saveOrUpdate(job);
        if (job instanceof Timer) {
            Timer timer = (Timer) job;
            Action action = timer.getAction();
            if (action != null) {
                log.debug("cascading timer save to action");
                getHibernateTemplate().save(action);
            }
        }
        if (notifyExecutor) {
            synchronized (jobExecutor) {
                jobExecutor.notify();
            }
        }
    }

    public void deleteJob(Job job) {
        getHibernateTemplate().delete(job);
    }

    public Job loadJob(long jobId) {
        try {
            return getHibernateTemplate().load(Job.class, new Long(jobId));
        } catch (Exception e) {
            log.error(e);
            throw new InternalApplicationException("couldn't load job '" + jobId + "'", e);
        }
    }

    public Job getJob(long jobId) {
        try {
            return getHibernateTemplate().get(Job.class, new Long(jobId));
        } catch (Exception e) {
            log.error(e);
            throw new InternalApplicationException("couldn't get job '" + jobId + "'", e);
        }
    }

    public void deleteTimersByName(String name, Token token) {
        log.debug("deleting timers by name '" + name + "' for " + token);
        // "delete from ru.runa.bpm.job.Timer timer where timer.token = :token and timer.name = :name and timer.repeat is not null"
        List<Timer> timers = getHibernateTemplate().find("from ru.runa.bpm.job.Timer where token=? and name=?", token, name);
        getHibernateTemplate().deleteAll(timers);
        log.debug(timers.size() + " timers by name '" + name + "' for " + token + " were deleted");
    }

    public void deleteJobsForProcessInstance(final ProcessInstance processInstance) {
        getHibernateTemplate().execute(new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                log.debug("deleting timers for " + processInstance);
                Query query = session
                        .createQuery("delete from ru.runa.bpm.job.Timer timer where timer.processInstance=? and timer.repeat is not null");
                query.setParameter(1, processInstance);
                int entityCount = query.executeUpdate();
                log.warn(entityCount + " remaining timers for " + processInstance + " were deleted");

                log.debug("deleting execute-node-jobs for " + processInstance);

                query = session.createQuery("delete from ru.runa.bpm.job.ExecuteNodeJob job where job.processInstance=?");
                query.setParameter(1, processInstance);
                entityCount = query.executeUpdate();
                log.warn(entityCount + " remaining execute-node-jobs for " + processInstance + " were deleted");
                return null;
            }
        });
    }

}
