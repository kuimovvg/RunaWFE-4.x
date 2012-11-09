package ru.runa.wfe.job.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDAO;

public class ExpiredJobCheckerTask extends TransactionalTaskBase {
    private static final Log log = LogFactory.getLog(ExpiredJobCheckerTask.class);

    @Autowired
    private JobDAO jobDAO;
    private JobExecutor executor;

    @Required
    public void setExecutor(JobExecutor executor) {
        this.executor = executor;
    }

    @Override
    protected boolean doPrerequsites() {
        List<Job> jobs = jobDAO.getExpiredJobs();
        log.debug("Expired jobs: " + jobs.size());
        return jobs.size() == 0;
    }

    @Override
    protected void doExecute() {
        for (Job job : jobDAO.getExpiredJobs()) {
            try {
                executor.executeJobs(job.getId());
            } catch (Exception e) {
                log.error("Error execute job " + job, e);
            }
        }
    }

}
