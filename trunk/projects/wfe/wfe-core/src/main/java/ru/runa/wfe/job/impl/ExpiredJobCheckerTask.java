package ru.runa.wfe.job.impl;

import java.util.List;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDAO;

public class ExpiredJobCheckerTask extends TimerTask {
    private static final Log log = LogFactory.getLog(ExpiredJobCheckerTask.class);

    @Autowired
    private JobDAO jobDAO;
    private JobExecutor executor;

    @Required
    public void setExecutor(JobExecutor executor) {
        this.executor = executor;
    }

    @Override
    public final void run() {
        try {
            List<Job> jobs = jobDAO.getExpiredJobs();
            log.debug("Expired jobs: " + jobs.size());
            if (jobs.size() > 0) {
                for (Job job : jobDAO.getExpiredJobs()) {
                    try {
                        // in new transaction
                        executor.executeJob(job.getId());
                        CachingLogic.onTransactionComplete();
                    } catch (Exception e) {
                        // already logged
                    }
                }
            }
        } catch (Throwable th) {
            log.error("timer task error", th);
        }
    }

}
