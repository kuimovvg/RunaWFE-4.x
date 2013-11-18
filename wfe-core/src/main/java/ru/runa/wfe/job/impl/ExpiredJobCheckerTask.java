package ru.runa.wfe.job.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDAO;

public class ExpiredJobCheckerTask extends JobTask {
    @Autowired
    private JobDAO jobDAO;
    private JobExecutor executor;

    @Required
    public void setExecutor(JobExecutor executor) {
        this.executor = executor;
    }

    @Override
    protected void execute() throws Exception {
        List<Job> jobs = jobDAO.getExpiredJobs();
        log.debug("Expired jobs: " + jobs.size());
        if (jobs.size() > 0) {
            for (Job job : jobDAO.getExpiredJobs()) {
                try {
                    // in new transaction
                    executor.executeJob(job.getId());
                } catch (Exception e) {
                    // exception is already logged in JobExecutor
                } finally {
                    CachingLogic.onTransactionComplete();
                }
            }
        }
    }

}
