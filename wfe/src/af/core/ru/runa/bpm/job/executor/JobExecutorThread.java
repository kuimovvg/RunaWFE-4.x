package ru.runa.bpm.job.executor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.NotSupportedException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;

import ru.runa.bpm.db.JobDAO;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.job.Job;
import ru.runa.commons.ApplicationContextFactory;

public class JobExecutorThread extends Thread {
    public JobExecutorThread(String name, JobExecutor jobExecutor, int idleInterval, int maxIdleInterval, long maxLockTime, int maxHistory) {
        super(name);
        this.jobExecutor = jobExecutor;
        this.idleInterval = idleInterval;
        this.maxIdleInterval = maxIdleInterval;
        this.maxLockTime = maxLockTime;
    }

    final JobExecutor jobExecutor;
    final int idleInterval;
    final int maxIdleInterval;
    final long maxLockTime;
    private Set<Job> failed = new HashSet<Job>();
    int currentIdleInterval;
    volatile boolean isActive = true;

    @Override
    public void run() {
        currentIdleInterval = idleInterval;
        while (isActive) {
            try {
                Collection<Job> acquiredJobs = acquireJobs();
                if (!acquiredJobs.isEmpty()) {
                    Iterator<Job> iter = acquiredJobs.iterator();
                    while (iter.hasNext() && isActive) {
                        Job job = iter.next();
                        executeJob(job);
                    }
                } else { // no jobs acquired
                    if (isActive) {
                        long waitPeriod = getWaitPeriod();
                        if (waitPeriod > 0) {
                            synchronized (jobExecutor) {
                                jobExecutor.wait(waitPeriod);
                            }
                        }
                    }
                }
                // no exception so resetting the currentIdleInterval
                currentIdleInterval = idleInterval;
            } catch (InterruptedException e) {
                log.info((isActive ? "active" : "inactive") + " job executor thread '" + getName() + "' got interrupted");
            } catch (Exception e) {
                log.error("exception in job executor thread. waiting " + currentIdleInterval + " milliseconds", e);
                try {
                    synchronized (jobExecutor) {
                        jobExecutor.wait(currentIdleInterval);
                    }
                } catch (InterruptedException e2) {
                    log.debug("delay after exception got interrupted", e2);
                }
                // after an exception, the current idle interval is doubled to
                // prevent
                // continuous exception generation when e.g. the db is
                // unreachable
                currentIdleInterval <<= 1;
                if (currentIdleInterval > maxIdleInterval || currentIdleInterval < 0) {
                    currentIdleInterval = maxIdleInterval;
                }
            }
        }
        log.info(getName() + " leaves cyberspace");
    }

    protected Collection<Job> acquireJobs() {
        Collection<Job> acquiredJobs;
        synchronized (JobExecutorThread.class) {
            // log.debug("acquiring jobs for execution...");
            List<Job> jobsToLock = Collections.EMPTY_LIST;
            try {
                JobDAO jobDAO = ApplicationContextFactory.getJobSession();
                String lockOwner = getName();
                if (!failed.isEmpty()) {
                    Job fJob = jobDAO.getJob(failed.iterator().next().getId());
                    // fJob.setRetries(fJob.getRetries() - 1);
                    jobDAO.saveJob(fJob, false);
                    failed.remove(failed.iterator().next());
                    return jobsToLock;
                }
                // log.debug("querying for acquirable job...");
                Job job = jobDAO.getFirstAcquirableJob(lockOwner);
                if (job != null) {
                    // if (job.isExclusive()) {
                    // log.debug("found exclusive " + job);
                    // ProcessInstance processInstance =
                    // job.getProcessInstance();
                    // log.debug("finding other exclusive jobs for " +
                    // processInstance);
                    // jobsToLock = jobSession.findExclusiveJobs(lockOwner,
                    // processInstance);
                    // log.debug("trying to obtain exclusive locks on " +
                    // jobsToLock + " for " + processInstance);
                    // } else {
                    log.debug("trying to obtain lock on " + job);
                    jobsToLock = Collections.singletonList(job);
                    // }
                    // Date lockTime = new Date();
                    for (Iterator<Job> iter = jobsToLock.iterator(); iter.hasNext();) {
                        job = iter.next();
                        // job.setLockOwner(lockOwner);
                        // job.setLockTime(lockTime);
                        // jbpmContext.getSession().update(job);
                    }
                    // HACKY HACK : this is a workaround for a hibernate problem
                    // that is fixed in hibernate 3.2.1
                    // TODO remove this hack already?
                    // if (job instanceof Timer) {
                    // Hibernate.initialize(((Timer)job).getGraphElement());
                    // }
                }/*
                  * else { log.debug("no acquirable jobs in job table"); }
                  */
            } finally {
                acquiredJobs = jobsToLock;
                // log.debug("obtained lock on jobs: "+acquiredJobs);
            }
        }
        return acquiredJobs;
    }

    private static TransactionManager txManager = null;

    private TransactionManager getTransactionManager() {
        if (txManager == null) {
            InitialContext context;
            try {
                context = new InitialContext();
                txManager = (TransactionManager) context.lookup("java:/TransactionManager");
                log.debug("TransactionManager: " + txManager);
            } catch (NamingException e) {
                // This should never happen
            }
        }
        return txManager;
    }

    protected void executeJob(Job job) {
        int commit = -1;
        try {
            getTransactionManager().begin();
            try {
                getTransactionManager().getTransaction().registerSynchronization(
                        (Synchronization) Class.forName("ru.runa.commons.cache.CachingLogic").newInstance());
            } catch (Exception e) {
            }
            commit = 0;
            JobDAO jobDAO = ApplicationContextFactory.getJobSession();
            job = jobDAO.loadJob(job.getId());
            try {
                log.debug("executing " + job);
                ExecutionContext executionContext = new ExecutionContext(null /* TODO */, job.getToken());
                executionContext.setTaskInstance(job.getTaskInstance());
                if (job.execute(executionContext)) {
                    jobDAO.deleteJob(job);
                }
                ++commit;
            } catch (Exception e) {
                log.debug("exception while executing " + job, e);
                failed.add(job);
                if (!isPersistenceException(e)) {
                    StringWriter memoryWriter = new StringWriter();
                    e.printStackTrace(new PrintWriter(memoryWriter));
                    // job.setException(memoryWriter.toString());
                    // job.setRetries(job.getRetries() - 1);
                } else {
                    // allowing a transaction to proceed after a persistence
                    // exception is unsafe
                    // jbpmContext.setRollbackOnly();
                }
            }
            // if this job is locked too long
            // long totalLockTimeInMillis = System.currentTimeMillis() -
            // job.getLockTime().getTime();
            // if (totalLockTimeInMillis > maxLockTime) {
            // //jbpmContext.setRollbackOnly();
            // }
            ++commit;
        } catch (SystemException e) {
            log.error("Couldn't start transaction.", e);
        } catch (NotSupportedException e) {
            log.error("Couldn't start transaction.", e);
        } finally {
            try {
                ++commit;
            } catch (RuntimeException e) {
                // if this is a stale state exception, keep it quiet
                throw e;
            } finally {
                try {
                    if (commit == 3) {
                        getTransactionManager().commit();
                    } else {
                        getTransactionManager().rollback();
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    private static boolean isPersistenceException(Throwable throwable) {
        do {
            if (throwable instanceof HibernateException) {
                return true;
            }
            throwable = throwable.getCause();
        } while (throwable != null);
        return false;
    }

    protected Date getNextDueDate() {
        Date nextDueDate = null;
        JobDAO jobDAO = ApplicationContextFactory.getJobSession();
        Collection<Long> jobIdsToIgnore = jobExecutor.getMonitoredJobIds();
        Job job = jobDAO.getFirstDueJob(getName(), jobIdsToIgnore);
        if (job != null) {
            nextDueDate = job.getDueDate();
            jobExecutor.addMonitoredJobId(getName(), job.getId());
        }
        return nextDueDate;
    }

    protected long getWaitPeriod() {
        long interval = currentIdleInterval;
        Date nextDueDate = getNextDueDate();
        if (nextDueDate != null) {
            long currentTime = System.currentTimeMillis();
            long nextDueTime = nextDueDate.getTime();
            if (nextDueTime < currentTime + currentIdleInterval) {
                interval = nextDueTime - currentTime;
            }
        }
        if (interval < 0) {
            interval = 0;
        }
        return interval;
    }

    /**
     * @deprecated As of jBPM 3.2.3, replaced by {@link #deactivate()}
     */
    public void setActive(boolean isActive) {
        if (isActive == false) {
            deactivate();
        }
    }

    /**
     * Indicates that this thread should stop running. Execution will cease
     * shortly afterwards.
     */
    public void deactivate() {
        if (isActive) {
            isActive = false;
            interrupt();
        }
    }

    private static Log log = LogFactory.getLog(JobExecutorThread.class);
}
